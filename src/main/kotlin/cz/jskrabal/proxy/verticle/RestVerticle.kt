package cz.jskrabal.proxy.verticle

import cz.jskrabal.proxy.config.RestConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.service.ProxyServiceFactory
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler

class RestVerticle(private val config: RestConfig) : AbstractVerticle() {

    private val router = Router.router(vertx)

    override fun start(startFuture: Future<Void>) {
        val proxyService = ProxyServiceFactory.createProxy(vertx, ProxyServiceVerticle.SERVICE_ADDRESS)
        router.route().handler(BodyHandler.create())

        router.post("/proxy").handler { rc ->
            val proxy = rc.bodyAsJson
            proxyService.deployProxy(proxy, Handler {
                rc.response().end()
            })
        }
        router.delete("/proxy/:id").handler { rc ->
            proxyService.undeployProxy(rc.request().getParam("id"), Handler {
                rc.response().end()
            })
        }

        vertx.executeBlocking(Handler { event ->
            vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config.port, config.host) { result ->
                        if (result.succeeded()) {
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
