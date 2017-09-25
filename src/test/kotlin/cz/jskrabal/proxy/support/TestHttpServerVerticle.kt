package cz.jskrabal.proxy.support

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.streams.Pump

/**
 * Created by janskrabal on 06/07/16.
 */
class TestHttpServerVerticle : AbstractVerticle() {

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {
        vertx.executeBlocking(Handler { event ->
            vertx.createHttpServer().requestHandler { upstreamRequest ->
                val responseType = getExpectedResponseType(upstreamRequest)

                when (responseType) {
                    ResponseType.RESPOND -> processRequest(upstreamRequest)
                    ResponseType.DO_NOT_RESPOND -> {
                    }
                    ResponseType.SNAP_CONNECTION -> upstreamRequest.response().close()
                }
            }.listen(ProxyTestUtils.HTTP_SERVER_PORT, { result ->
                if (result.succeeded()) {
                    event.complete()
                } else {
                    event.fail(result.cause())
                }
            })
        }, startFuture.completer())
    }

    private fun processRequest(upstreamRequest: HttpServerRequest) {
        if (ProxyTestUtils.canHaveBody(upstreamRequest.method())) {
            respondWithBody(upstreamRequest)
        } else {
            respond(upstreamRequest)
        }
    }

    private fun getExpectedResponseType(request: HttpServerRequest): ResponseType {
        val expectedResponseType = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE_TYPE)

        return if (expectedResponseType == null) ResponseType.RESPOND else ResponseType.valueOf(expectedResponseType)
    }

    private fun respondWithBody(upstreamRequest: HttpServerRequest) {
        val httpCode = getExpectedResponseCode(upstreamRequest)

        upstreamRequest.response()
                .setStatusCode(httpCode).isChunked = true

        Pump.pump<Buffer>(upstreamRequest, upstreamRequest.response()).start()
        upstreamRequest.endHandler { _ -> upstreamRequest.response().end() }
    }

    private fun respond(request: HttpServerRequest) {
        var response: String? = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE)
        response = if (response == null) "" else response
        val httpCode = getExpectedResponseCode(request)

        request.response()
                .setStatusCode(httpCode)
                .end(response)
    }

    private fun getExpectedResponseCode(request: HttpServerRequest): Int {
        val responseCode = request.getParam(ProxyTestUtils.URL_PARAM_RESPONSE_CODE)
        return if (responseCode == null) 200 else Integer.parseInt(responseCode)
    }
}
