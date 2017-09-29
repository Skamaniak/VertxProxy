package cz.jskrabal.proxy

import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.verticle.ProxyVerticle
import cz.jskrabal.proxy.verticle.TypedConfigurationVerticle
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.logging.Logger


class Node : TypedConfigurationVerticle<ProxyConfig>() {
    override val configClass = ProxyConfig::class

    override fun start(startFuture: Future<Void>) {
        val futures = ArrayList<Future<Void>>()
        for (port in 5000..8000) {
            val proxyFuture = Future.future<Void>()
            vertx.deployVerticle(ProxyVerticle(port, config)) {
                if (it.succeeded()) {
                    logger.info("Deployed proxy for port: $port")
                    proxyFuture.complete()
                } else {
                    proxyFuture.fail(it.cause())
                }
            }
            futures.add(proxyFuture)
        }

        CompositeFuture.all(futures.toList()).setHandler {
            startFuture.completer()
        }
    }

    companion object {
        val logger: Logger = loggerFor<Node>()
    }
}