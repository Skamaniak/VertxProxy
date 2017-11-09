package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.config.NodeConfig
import cz.jskrabal.proxy.deployVerticleFuture
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.sendAction
import io.vertx.core.CompositeFuture
import io.vertx.core.Future


class NodeVerticle : TypedConfigurationVerticle<NodeConfig>() {

    override val configClass = NodeConfig::class

    override fun start(startFuture: Future<Void>) {
        val logger = loggerFor<NodeVerticle>()

        logger.info("Starting Proxy Node")

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