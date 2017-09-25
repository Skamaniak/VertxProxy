package cz.jskrabal.proxy.transfer

import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.pump.DataPump
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.*
import java.util.function.Consumer

/**
 * Created by janskrabal on 01/06/16.
 */
class HttpTransfer(vertx: Vertx, client: HttpClient, configuration: ProxyConfig,
                   upstreamRequest: HttpServerRequest) : Transfer(vertx, client, configuration, upstreamRequest) {

    override fun start() {
        val method = upstreamRequest.method()
        val uri = upstreamRequest.uri()

        val nextProxy = configuration.nextHttpProxy

        val downstreamRequest: HttpClientRequest
        downstreamRequest = if (nextProxy != null) {
            logger.debug("'{}' proxying request '{}' '{}' to next HTTP proxy {}", id, method, uri, nextProxy)
            client.request(method, nextProxy.port, nextProxy.host, uri)
        } else {
            logger.debug("'{}' proxying request '{}' '{}'", id, method, uri)
            client.requestAbs(method, uri)
        }

        downstreamRequest.handler(downstreamResponseHandler())
        downstreamRequest.exceptionHandler(downstreamExceptionHandler(method, uri))
        addRequestTimeout(downstreamRequest)

        configureClientRequestByServerRequest(downstreamRequest, upstreamRequest)
        createRequestHandler(downstreamRequest)
    }

    private fun downstreamExceptionHandler(method: HttpMethod, uri: String): Handler<Throwable> {
        return Handler { throwable ->
            logger.warn("{} request '{}' '{}' has failed. Responding by error to the client's HTTP request",
                    id, method, uri)

            respondConnectionFailed(throwable)
        }
    }

    private fun downstreamResponseHandler(): Handler<HttpClientResponse> {
        return Handler { downstreamResponse ->
            val responseCode = downstreamResponse.statusCode()
            logger.debug("'{}' proxying response with code '{}'", id, responseCode)

            configureServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse)
            createResponseHandlers(downstreamResponse)
        }
    }

    private fun createResponseHandlers(downstreamResponse: HttpClientResponse) {
        DataPump.create(downstreamResponse, upstreamRequest.response(), Consumer { data ->
            logger.debug("'{}' proxying response data (length '{}')", id, data.length())
        }).start()

        downstreamResponse.endHandler { _ ->
            logger.debug("'{}' ended (downstream)", id)
            upstreamRequest.response().end()
        }
    }

    private fun createRequestHandler(downstreamRequest: HttpClientRequest) {
        DataPump.create(upstreamRequest, downstreamRequest, Consumer { data ->
            logger.debug("'{}' proxying request data (length '{}')", id, data.length())
        }).start()

        upstreamRequest.endHandler { _ ->
            logger.debug("'{}' ended (upstream)", id)
            downstreamRequest.end()
        }
    }

    companion object {
        private val logger = loggerFor<HttpTransfer>()
    }
}
