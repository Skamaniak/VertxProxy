package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.acceptor.Acceptor
import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.model.Proxy
import cz.jskrabal.proxy.transfer.HttpTransfer
import cz.jskrabal.proxy.transfer.TunnelTransfer
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.*
import java.util.concurrent.TimeUnit

class ProxyVerticle(private val proxy: Proxy, private val config: ProxyConfig) : AbstractVerticle() {

    private lateinit var httpClient: HttpClient

    private val httpServerOptions
        get() = HttpServerOptions()
                .setLogActivity(config.stream.upstream.debugLogging)
                .setIdleTimeout(config.stream.upstream.idleTimeoutMillis)

    private val httpClientOptions
        get() = HttpClientOptions()
                .setLogActivity(config.stream.downstream.debugLogging)
                .setConnectTimeout(config.stream.downstream.connectionTimeoutMillis)
                .setIdleTimeout(TimeUnit.MILLISECONDS.toSeconds(config.stream.downstream.idleTimeoutMillis.toLong()).toInt())

    @Throws(Exception::class)
    override fun start(startFuture: Future<Void>) {
        httpClient = vertx.createHttpClient(httpClientOptions)
        vertx.createHttpServer(httpServerOptions)
                .requestHandler(this::transfer)
                .connectionHandler(this::connect)
                .listen(proxy.port, config.host) { result ->
                    if (result.succeeded()) {
                        logger.info("Started ${ProxyVerticle::class.simpleName} instance. DeploymentID: ${deploymentID()}")
                        startFuture.complete()
                    } else {
                        startFuture.fail(result.cause())
                    }
                }
    }

    private fun connect(httpConnection: HttpConnection) {
        Acceptor(vertx, httpClient, config, httpConnection).start()
    }

    private fun transfer(upstreamRequest: HttpServerRequest) {
        if (upstreamRequest.method() == HttpMethod.CONNECT) {
            TunnelTransfer(vertx, httpClient, config, upstreamRequest)
        } else {
            HttpTransfer(vertx, httpClient, config, upstreamRequest)
        }.start()
    }

    companion object {
        private val logger = loggerFor<ProxyVerticle>()
    }

}
