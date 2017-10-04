package cz.jskrabal.proxy.verticle

import com.github.aesteve.vertx.nubes.VertxNubes
import cz.jskrabal.proxy.config.RestConfig
import cz.jskrabal.proxy.loggerFor
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

class RestVerticle(private val config: RestConfig) : AbstractVerticle() {


    override fun start(startFuture: Future<Void>) {
        with(VertxNubes(vertx, JsonObject().put("controller-packages", JsonArray().add("cz.jskrabal.proxy.controller")))) {
            bootstrap({ event ->
                if (event.succeeded()) {
                    val router = event.result()
                    vertx.executeBlocking(Handler { blocking ->
                        vertx.createHttpServer()
                                .requestHandler(router::accept)
                                .listen(config.port, config.host) { result ->
                                    if (result.succeeded()) {
                                        logger.info("Started ${RestVerticle::class.simpleName} instance. DeploymentID: ${deploymentID()}")
                                        blocking.complete()
                                    } else {
                                        blocking.fail(result.cause())
                                    }
                                }
                    }, startFuture.completer())
                } else {
                    startFuture.fail(event.cause())
                }
            })
        }
    }

    companion object {
        private val logger = loggerFor<RestVerticle>()
    }

}
