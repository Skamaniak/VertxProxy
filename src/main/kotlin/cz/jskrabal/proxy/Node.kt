package cz.jskrabal.proxy

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import cz.jskrabal.proxy.config.NodeConfig
import cz.jskrabal.proxy.verticle.PersistenceServiceVerticle
import cz.jskrabal.proxy.verticle.ProxyServiceVerticle
import cz.jskrabal.proxy.verticle.RestVerticle
import cz.jskrabal.proxy.verticle.TypedConfigurationVerticle
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.json.Json


class Node : TypedConfigurationVerticle<NodeConfig>() {

    override val configClass = NodeConfig::class

    override fun start(startFuture: Future<Void>) {
        Json.mapper.registerKotlinModule()
        vertx.exceptionHandler {
            loggerFor<Node>().error("Uncaught exception", it)
        }

        val persistenceFuture = vertx.deployVerticleFuture(PersistenceServiceVerticle(config.persistence))
        val proxyFuture = vertx.deployVerticleFuture(ProxyServiceVerticle(config.proxy))
        val restFuture = vertx.deployVerticleFuture(RestVerticle(config.admin))

        CompositeFuture.all(persistenceFuture, proxyFuture, restFuture).setHandler {
            if (it.succeeded()) {
                vertx.eventBus().sendAction(ProxyServiceVerticle.SERVICE_ADDRESS, "reload")
                startFuture.succeeded()
            } else {
                startFuture.fail(it.cause())
            }
        }
    }
}