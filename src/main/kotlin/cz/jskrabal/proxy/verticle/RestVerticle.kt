package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.config.RestConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.resource.ProxyResource
import cz.jskrabal.proxy.service.ProxyServiceFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RestVerticle(private val config: RestConfig) : AbstractVerticle() {

    private val router = Router.router(vertx)

    override fun start(startFuture: Future<Void>) {
        val proxyResource = ProxyResource(ProxyServiceFactory.createProxy(vertx, ProxyServiceVerticle.SERVICE_ADDRESS))
        with(router) {
            route().handler(BodyHandler.create())
            get("/proxy/:id").handler(proxyResource.getProxy)
            delete("/proxy/:id").handler(proxyResource.deleteProxy)
            post("/proxy").handler(proxyResource.deployProxy)
        }

        vertx.executeBlocking(Handler { event ->
            vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config.port, config.host) { result ->
                        if (result.succeeded()) {
                            logger.info("Started ${RestVerticle::class.simpleName} instance. DeploymentID: ${deploymentID()}")
                            event.complete()
                        } else {
                            event.fail(result.cause())
                        }
                    }
        }, startFuture.completer())
    }

    companion object {
        private val logger = loggerFor<RestVerticle>()
    }

}
