package cz.jskrabal.proxy.integration

import cz.jskrabal.proxy.support.ResponseType
import cz.jskrabal.proxy.support.TestServerRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.ext.unit.TestContext
import org.junit.Test
import java.util.*

/**
 * Created by janskrabal on 08/07/16.
 */
class HttpIntegrationTest : AbstractProxyTest("/config/basicConfiguration.json") {

    @Test
    fun testProxyHttpRequest(context: TestContext) {
        val notTestedMethods = EnumSet.of(HttpMethod.CONNECT, HttpMethod.OTHER)
        val httpMethods = EnumSet.complementOf(notTestedMethods)

        for (httpMethod in httpMethods) {
            sendRequestWithExpectedResponseCode(context, httpMethod, 200)
            sendRequestWithExpectedResponseCode(context, httpMethod, 301)
            sendRequestWithExpectedResponseCode(context, httpMethod, 404)
            sendRequestWithExpectedResponseCode(context, httpMethod, 500)
        }
    }

    @Test
    fun testProxyHttpRequestWhenServerNotResponding(context: TestContext) {
        val async = context.async()

        val request = TestServerRequest.create(context, HttpMethod.GET)
                .withResponseType(ResponseType.DO_NOT_RESPOND)
                .build()

        executeRequest(request,
                Handler { response ->
                    assertErrorStatus(context, response, HttpResponseStatus.GATEWAY_TIMEOUT)
                    response.endHandler { _ -> async.complete() }
                },
                Handler { context.fail(it) })

    }

    @Test
    @Throws(InterruptedException::class)
    fun testProxyHttpRequestWhenServerSnapsConnection(context: TestContext) {
        val async = context.async()
        val request = TestServerRequest.create(context, HttpMethod.GET)
                .withResponseType(ResponseType.SNAP_CONNECTION)
                .build()

        executeRequest(request,
                Handler { response ->
                    assertErrorStatus(context, response, HttpResponseStatus.BAD_GATEWAY)
                    response.endHandler { _ -> async.complete() }
                },
                Handler { context.fail(it) })
    }

    private fun assertErrorStatus(context: TestContext, response: HttpClientResponse,
                                  status: HttpResponseStatus) {

        context.assertTrue(response.statusCode() == status.code())
    }

    private fun sendRequestWithExpectedResponseCode(context: TestContext, method: HttpMethod, responseCode: Int) {
        val async = context.async()
        val request = TestServerRequest.create(context, method)
                .withResponse(TEST_MESSAGE)
                .withResponseCode(responseCode)
                .build()

        executeRequest(request, Handler { response ->
            context.assertEquals(response.statusCode(), responseCode)
            response.handler { body -> context.assertTrue(body.toString().contains(TEST_MESSAGE)) }
            response.endHandler { _ -> async.complete() }
        })
    }

    companion object {
        private const val TEST_MESSAGE = "Everything is awesome!"
    }

}
