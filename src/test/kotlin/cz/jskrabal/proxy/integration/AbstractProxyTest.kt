package cz.jskrabal.proxy.integration

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.model.Proxy
import cz.jskrabal.proxy.support.ProxyTestUtils
import cz.jskrabal.proxy.support.ResponseType
import cz.jskrabal.proxy.support.TestHttpServerVerticle
import cz.jskrabal.proxy.support.TestServerRequest
import cz.jskrabal.proxy.verticle.ProxyVerticle
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.json.Json
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.apache.commons.io.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * Created by janskrabal on 15/06/16.
 */
@RunWith(VertxUnitRunner::class)
abstract class AbstractProxyTest(private val configPath: String) {
    private lateinit var vertx: Vertx

    @Before
    @Throws(IOException::class)
    fun setUp(context: TestContext) {
        Json.mapper.registerKotlinModule()

        vertx = Vertx.vertx()

        val async = context.async(2)

        val configuration = readTestConfig(configPath)
        val proxyVerticle = ProxyVerticle(Proxy("testProxyVerticle"), configuration)
        vertx.deployVerticle(proxyVerticle) { result ->
            context.asyncAssertSuccess<String>().handle(result)
            async.countDown()
        }
        vertx.deployVerticle(TestHttpServerVerticle::class.java.name) { result ->
            context.asyncAssertSuccess<String>().handle(result)
            async.countDown()
        }

        async.awaitSuccess()
    }

    @After
    fun tearDown(context: TestContext) {
        vertx.close(context.asyncAssertSuccess())
    }

    @JvmOverloads protected fun executeRequest(request: TestServerRequest, responseHandler: Handler<HttpClientResponse>,
                                               exceptionHandler: Handler<Throwable> = Handler { event -> request.context.fail(event) }) {

        if (ProxyTestUtils.canHaveBody(request.httpMethod)) {
            executeWithBody(request, responseHandler, exceptionHandler)
        } else {
            executeWithoutBody(request, responseHandler, exceptionHandler)
        }
    }

    @Throws(IOException::class)
    private fun readTestConfig(path: String): ProxyConfig {
        val configStream = AbstractProxyTest::class.java.getResourceAsStream(path)
        val configurationJson = IOUtils.toString(configStream)

        return Json.mapper.readValue(configurationJson, ProxyConfig::class.java)
    }

    private fun executeWithBody(request: TestServerRequest, responseHandler: Handler<HttpClientResponse>,
                                exceptionHandler: Handler<Throwable>) {
        val uriWithoutResponse = uriWithoutResponse(request.responseCode, request.responseType)

        val client = vertx.createHttpClient()
        client.request(request.httpMethod, ProxyTestUtils.PROXY_PORT, ProxyTestUtils.PROXY_HOST,
                uriWithoutResponse, responseHandler)
                .exceptionHandler(exceptionHandler)
                .end(request.response)
    }

    private fun executeWithoutBody(request: TestServerRequest, responseHandler: Handler<HttpClientResponse>,
                                   exceptionHandler: Handler<Throwable>) {
        val uriWithResponse = uriWithResponse(request.response, request.responseCode,
                request.responseType)

        val client = vertx.createHttpClient()
        client.request(request.httpMethod, ProxyTestUtils.PROXY_PORT, ProxyTestUtils.PROXY_HOST,
                uriWithResponse, responseHandler)
                .exceptionHandler(exceptionHandler)
                .end()
    }


    private fun uriWithResponse(response: String?, responseCode: Int, responseType: ResponseType): String {
        val uriWithoutResponse = uriWithoutResponse(responseCode, responseType)
        if (response == null) {
            return uriWithoutResponse
        }

        val urlEncodedResponse: String
        try {
            urlEncodedResponse = URLEncoder.encode(response, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Can't url-encode response due to nested exception", e)
        }

        return uriWithoutResponse + '&' + ProxyTestUtils.URL_PARAM_RESPONSE + '=' + urlEncodedResponse
    }

    private fun uriWithoutResponse(responseCode: Int, responseType: ResponseType): String {
        return ProxyTestUtils.BASE_REQUEST_URI +
                '?' + ProxyTestUtils.URL_PARAM_RESPONSE_CODE + '=' + responseCode +
                '&' + ProxyTestUtils.URL_PARAM_RESPONSE_TYPE + '=' + responseType
    }

}
