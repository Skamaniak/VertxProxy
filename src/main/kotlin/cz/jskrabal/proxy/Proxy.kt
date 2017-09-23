package cz.jskrabal.proxy

import cz.jskrabal.proxy.acceptor.Acceptor
import cz.jskrabal.proxy.config.ProxyConfiguration
import cz.jskrabal.proxy.transfer.HttpTransfer
import cz.jskrabal.proxy.transfer.TunnelTransfer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.*
import io.vertx.core.logging.Logger
import java.util.concurrent.TimeUnit

class Proxy : AbstractVerticle() {

    companion object {
        private val LOGGER: Logger = loggerFor<Proxy>()
    }

    private lateinit var configuration: ProxyConfiguration
    private lateinit var httpClient: HttpClient

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {
        vertx.executeBlocking(Handler { event ->
            configuration = loadConfig()
            httpClient = vertx.createHttpClient(createHttpClientOptions())
            vertx.createHttpServer(createHttpServerOptions())
                    .requestHandler({ this.transfer(it) })
                    .connectionHandler({ this.connect(it) })
                    .listen(configuration.proxyPort, configuration.proxyHost) { result ->
                        if (result.succeeded()) {
                            event.complete()
                        } else {
                            event.fail(result.cause())
                        }
                    }
        }, startFuture.completer())
    }

    private fun createHttpServerOptions() = HttpServerOptions()
            .setLogActivity(configuration.isUpstreamDebugLoggingEnabled)
            .setIdleTimeout(configuration.upstreamIdleTimeout)

    private fun createHttpClientOptions() = HttpClientOptions()
            .setLogActivity(configuration.isDownstreamDebugLoggingEnabled)
            .setConnectTimeout(configuration.downstreamConnectionTimeout)
            .setIdleTimeout(TimeUnit.MILLISECONDS.toSeconds(configuration.downstreamIdleTimeout.toLong()).toInt())

    private fun loadConfig(): ProxyConfiguration {
        LOGGER.info("Loaded configuration '{}'", config().encodePrettily())
        return ProxyConfiguration(config())
    }

    private fun connect(httpConnection: HttpConnection) {
        Acceptor(vertx, httpClient, configuration, httpConnection).start()
    }

    private fun transfer(upstreamRequest: HttpServerRequest) {
        if (upstreamRequest.method() == HttpMethod.CONNECT) {
            TunnelTransfer(vertx, httpClient, configuration, upstreamRequest)
        } else {
            HttpTransfer(vertx, httpClient, configuration, upstreamRequest)
        }.start()
    }
}
