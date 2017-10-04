package cz.jskrabal.proxy.verticle

import com.github.aesteve.vertx.nubes.VertxNubes
import cz.jskrabal.proxy.config.RestConfig
import cz.jskrabal.proxy.loggerFor
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.Json
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.obj

class RestVerticle(private val config: RestConfig) : AbstractVerticle() {


    override fun start(startFuture: Future<Void>) {
        with(VertxNubes(vertx, Json.obj(CONTROLLER_PACKAGES_PARAM to Json.array(CONTROLLERS_PACKAGE)))) {
            bootstrap({ event ->
                if (event.succeeded()) {
                    deployHttpServer(event.result())
                            .setHandler(startFuture.completer())
                } else {
                    startFuture.fail(event.cause())
                }
            })
        }
    }

    private fun deployHttpServer(router: Router): Future<Void> {
        val serverFuture = Future.future<Void>()
        vertx.executeBlocking(Handler { blocking ->
            vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config.port, config.host) { result ->
                        if (result.succeeded()) {
                            loggerFor<RestVerticle>().info(
                                    "Started ${RestVerticle::class.simpleName} instance." +
                                            " DeploymentID: ${deploymentID()}")
                            blocking.complete()
                        } else {
                            blocking.fail(result.cause())
                        }
                    }
        }, serverFuture.completer())
        return serverFuture
    }

    companion object {
        private const val CONTROLLER_PACKAGES_PARAM = "controller-packages"
        private const val CONTROLLERS_PACKAGE = "cz.jskrabal.proxy.controller"
    }

}
