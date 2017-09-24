package cz.jskrabal.proxy.transfer

import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.isChunked
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Vertx
import io.vertx.core.http.*
import java.nio.channels.UnresolvedAddressException
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.HashMap

/**
 * Created by janskrabal on 01/06/16.
 */
abstract class Transfer protected constructor(protected val vertx: Vertx,
                                              protected val client: HttpClient,
                                              protected val configuration: ProxyConfig,
                                              protected val upstreamRequest: HttpServerRequest) {

    protected val id: String = configuration.idGenerator.generateId()

    abstract fun start()

    protected fun configureServerResponseByClientResponse(serverResponse: HttpServerResponse,
                                                          clientResponse: HttpClientResponse): HttpServerResponse {
        serverResponse
                .setStatusCode(clientResponse.statusCode())
                .setStatusMessage(clientResponse.statusMessage()).isChunked = clientResponse.isChunked()

        val headers = serverResponse.headers()
                .setAll(clientResponse.headers())
                .addAll(customResponseHeaders)


        headers.removeAll { blockedResponseHeaders.contains(it.key) }

        return serverResponse
    }

    protected fun configureClientRequestByServerRequest(clientRequest: HttpClientRequest,
                                                        serverRequest: HttpServerRequest): HttpClientRequest {
        clientRequest.isChunked = serverRequest.isChunked()

        val headers = clientRequest.headers()
                .setAll(serverRequest.headers())
                .addAll(customRequestHeaders)

        headers.removeAll { blockedRequestHeaders.contains(it.key) }

        return clientRequest
    }

    protected fun addRequestTimeout(request: HttpClientRequest): HttpClientRequest {
        val requestTimeout = configuration.stream.downstream.httpRequestTimeoutMillis.toLong()
        if (requestTimeout > 0) {
            request.setTimeout(requestTimeout)
        }
        return request
    }

    protected fun respondConnectionFailed(throwable: Throwable) {
        if (!upstreamRequest.response().headWritten()) {
            val errorMessage = getErrorMessage(throwable)

            val status = exceptionToHttpStatus(throwable)
            upstreamRequest.response()
                    .setStatusCode(status.code())
                    .setStatusMessage(errorMessage)
                    .headers()
                    .addAll(customResponseHeaders)

            upstreamRequest.response().end()
        }
    }

    private fun exceptionToHttpStatus(throwable: Throwable): HttpResponseStatus {
        val status: HttpResponseStatus
        if (throwable is UnresolvedAddressException) {
            status = HttpResponseStatus.NOT_FOUND
        } else if (throwable is TimeoutException) {
            status = HttpResponseStatus.GATEWAY_TIMEOUT
        } else {
            status = HttpResponseStatus.BAD_GATEWAY
        }
        return status
    }

    private val customResponseHeaders: Map<String, String>
        get() {
            val customHeaders = HashMap(configuration.customHeaders.appendToResponse)
            addResponseDynamicHeaders(customHeaders)

            return customHeaders
        }

    private val customRequestHeaders: Map<String, String>
        get() {
            val customHeaders = HashMap(configuration.customHeaders.appendToRequest)
            addRequestDynamicHeaders(customHeaders)

            return customHeaders
        }

    private val blockedRequestHeaders: Set<String>
        get() = HashSet(configuration.customHeaders.removeFromRequest)

    private val blockedResponseHeaders: Set<String>
        get() = HashSet(configuration.customHeaders.removeFromResponse)

    private fun addCommonDynamicHeaders(headers: MutableMap<String, String>) {
        if (configuration.customHeaders.addTransferIdHeader) {
            headers.put("X-Transfer-Id", id)
        }

        if (configuration.customHeaders.addForwardedByHeaders) {
            val localAddress = upstreamRequest.localAddress()
            headers.put("X-Forwarded-By-Ip", localAddress.host().toString())
            headers.put("X-Forwarded-By-Port", localAddress.port().toString())
        }
    }

    private fun addRequestDynamicHeaders(headers: MutableMap<String, String>) {
        addCommonDynamicHeaders(headers)

        if (configuration.customHeaders.addForwardedForHeaders) {
            val remoteAddress = upstreamRequest.remoteAddress()
            headers.put("X-Forwarded-For-Ip", remoteAddress.host().toString())
            headers.put("X-Forwarded-For-Port", remoteAddress.port().toString())
        }
    }

    private fun addResponseDynamicHeaders(headers: MutableMap<String, String>) {
        addCommonDynamicHeaders(headers)
    }

    //FIXME possible security issue - revealing implementation details to the proxy client.
    private fun getErrorMessage(throwable: Throwable?): String {
        var errorMessage = "Connection to remote server has failed due to "
        errorMessage += if (throwable == null) {
            "unknown error"
        } else {
            val throwableMessage = throwable.message
            if (throwableMessage == null) {
                "'${throwable.javaClass}' with no message"
            } else {
                "nested exception $throwableMessage"
            }
        }
        return errorMessage
    }
}
