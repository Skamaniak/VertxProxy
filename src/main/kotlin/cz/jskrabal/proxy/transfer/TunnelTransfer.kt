package cz.jskrabal.proxy.transfer

import cz.jskrabal.proxy.config.NetworkConfig
import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.pump.DataPump
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.net.NetClientOptions
import io.vertx.core.net.NetSocket
import java.util.function.Consumer

/**
 * Created by janskrabal on 31/05/16.
 */
class TunnelTransfer(vertx: Vertx, client: HttpClient, configuration: ProxyConfig,
                     connectRequest: HttpServerRequest) : Transfer(vertx, client, configuration, connectRequest) {

    private val netClientOptions: NetClientOptions = NetClientOptions().setIdleTimeout(configuration.stream.downstream.idleTimeoutMillis)
    private val nextTunnelProxySettings: NetworkConfig? = configuration.nextTunnelProxy

    override fun start() {
        logger.debug("'{}' connect request from '{}' to '{}' received", id, upstreamRequest.remoteAddress(),
                upstreamRequest.uri())

        val nextTunnelProxy = nextTunnelProxySettings
        if (nextTunnelProxy != null) {
            createTunnelThroughAnotherProxy(nextTunnelProxy)
        } else {
            createDirectTunnel()
        }
    }

    private fun createTunnelThroughAnotherProxy(nextTunnelProxy: NetworkConfig) {
        resendConnect(nextTunnelProxy, Handler { downstreamResponse ->
            val responseStatusCode = downstreamResponse.statusCode()

            if (responseStatusCode == HttpResponseStatus.OK.code()) {
                logger.debug("'{}' remote proxy '{}' has established the connection to '{}'", id, nextTunnelProxy,
                        upstreamRequest.uri())
                tunnelTo(downstreamResponse.netSocket())
            } else {
                logger.debug("{} connection to the remote proxy {} has failed ({} {})", id, nextTunnelProxy,
                        downstreamResponse.statusCode(), downstreamResponse.statusMessage())

                configureServerResponseByClientResponse(upstreamRequest.response(), downstreamResponse).end()
            }
        }, Handler { throwable ->
            logger.warn("{} connection to the remote proxy {} has failed. Responding by error to the client's " + "connect request", id, nextTunnelProxy, throwable)

            respondConnectionFailed(throwable)
        })
    }

    private fun resendConnect(nextProxySettings: NetworkConfig, responseHandler: Handler<HttpClientResponse>,
                              exceptionHandler: Handler<Throwable>) {
        logger.debug("'{}' resending connect request from '{}' to '{}' to the next proxy in chain '{}'", id,
                upstreamRequest.remoteAddress(), upstreamRequest.uri(), nextProxySettings)

        val downstreamConnectRequest = client.request(HttpMethod.CONNECT, nextProxySettings.port,
                nextProxySettings.host, upstreamRequest.uri(), responseHandler)
        addRequestTimeout(downstreamConnectRequest)

        configureClientRequestByServerRequest(downstreamConnectRequest, upstreamRequest)
                .exceptionHandler(exceptionHandler)
                .end()
    }


    private fun createDirectTunnel() {
        val uri = upstreamRequest.uri()
        val (host, port) = uri.split(HOST_PORT_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        vertx.createNetClient(netClientOptions).connect(Integer.parseInt(port), host) { event ->
            if (event.succeeded()) {
                tunnelTo(event.result())
            } else {
                logger.warn("'{}' connection to the remote server has failed. " + "Responding by error to the client's connect request ({})", id, event.cause())
                respondConnectionFailed(event.cause())
            }
        }
    }

    private fun tunnelTo(downstreamSocket: NetSocket) {
        respondConnectionEstablished()

        val upstreamSocket = upstreamRequest.netSocket()
        upstreamSocket.pause()
        createUpstreamHandlers(upstreamSocket, downstreamSocket)
        createDownstreamHandlers(upstreamSocket, downstreamSocket)
        upstreamSocket.resume()

        logger.debug("'{}' tunnel between '{}' and '{}' was established", id, upstreamSocket.remoteAddress(),
                downstreamSocket.remoteAddress())
    }

    private fun respondConnectionEstablished() {
        upstreamRequest.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .setStatusMessage(STATUS_CONNECTION_ESTABLISHED)
                .end()
    }

    private fun createUpstreamHandlers(upstreamSocket: NetSocket, downstreamSocket: NetSocket) {
        DataPump.create(upstreamSocket, downstreamSocket, Consumer { data ->
            logger.debug("'{}' proxying upstream data (length '{}')", id, data.length())
        }).start()

        upstreamSocket.closeHandler { _ ->
            logger.debug("'{}' closed (upstream)", id)
            downstreamSocket.close()
        }

        upstreamSocket.endHandler { _ ->
            logger.trace("'{}' ended (upstream)", id)
            downstreamSocket.end()
        }
    }

    private fun createDownstreamHandlers(upstreamSocket: NetSocket, downstreamSocket: NetSocket) {
        DataPump.create(downstreamSocket, upstreamSocket, Consumer { data ->
            logger.debug("'{}' proxying downstream data (length '{}')", id, data.length())
        }).start()

        downstreamSocket.closeHandler { _ ->
            logger.debug("'{}' closed (downstream)", id)
            upstreamSocket.close()
        }

        downstreamSocket.endHandler { _ ->
            logger.trace("'{}' ended (downstream)", id)
            upstreamSocket.end()
        }
    }

    companion object {
        private val logger = loggerFor<TunnelTransfer>()
        private const val STATUS_CONNECTION_ESTABLISHED = "Connection established"
        private const val HOST_PORT_SEPARATOR = ":"
    }
}
