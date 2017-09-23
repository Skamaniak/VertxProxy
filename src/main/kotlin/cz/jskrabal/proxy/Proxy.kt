package cz.jskrabal.proxy

import cz.jskrabal.proxy.acceptor.Acceptor
import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.config.ProxyConfiguration
import cz.jskrabal.proxy.transfer.HttpTransfer
import cz.jskrabal.proxy.transfer.TunnelTransfer
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.http.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

class Proxy : TypedConfigurationVerticle<ProxyConfig>() {

    private lateinit var httpClient: HttpClient

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {
        vertx.executeBlocking(Handler { event ->
            httpClient = vertx.createHttpClient(createHttpClientOptions())
            vertx.createHttpServer(createHttpServerOptions())
                    .requestHandler(this::transfer)
                    .connectionHandler(this::connect)
                    .listen(config.network.port, config.network.host) { result ->
                        if (result.succeeded()) {
                            event.complete()
                        } else {
                            event.fail(result.cause())
                        }
                    }
        }, startFuture.completer())
    }

    override val configClass: KClass<ProxyConfig>
        get() = ProxyConfig::class

    private fun createHttpServerOptions() = HttpServerOptions()
            .setLogActivity(config.stream.upstream.debugLogging)
            .setIdleTimeout(config.stream.upstream.idleTimeoutMillis)

    private fun createHttpClientOptions() = HttpClientOptions()
            .setLogActivity(config.stream.downstream.debugLogging)
            .setConnectTimeout(config.stream.downstream.connectionTimeoutMillis)
            .setIdleTimeout(TimeUnit.MILLISECONDS.toSeconds(config.stream.downstream.idleTimeoutMillis.toLong()).toInt())


    private fun connect(httpConnection: HttpConnection) {
        Acceptor(vertx, httpClient, ProxyConfiguration(config()), httpConnection).start()
    }

    private fun transfer(upstreamRequest: HttpServerRequest) {
        if (upstreamRequest.method() == HttpMethod.CONNECT) {
            TunnelTransfer(vertx, httpClient, config, upstreamRequest)
        } else {
            HttpTransfer(vertx, httpClient, config, upstreamRequest)
        }.start()
    }
}
