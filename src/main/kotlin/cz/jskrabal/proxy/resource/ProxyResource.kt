package cz.jskrabal.proxy.resource

import cz.jskrabal.proxy.service.ProxyService
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class ProxyResource(private val proxyService: ProxyService) {

    val deleteProxy: (RoutingContext) -> Unit = { rc ->
        proxyService.undeployProxy(rc.request().getParam("id"), Handler {
            if (it.succeeded()) {
                rc.response().end()
            } else {
                rc.response()
                        .setStatusCode(500)
                        .end()
            }
        })
    }


    val getProxy: (RoutingContext) -> Unit = { rc ->
        proxyService.getProxy(rc.request().getParam("id"), Handler {
            if (it.succeeded() && it.result() != null) {
                rc.response().end(it.result().toBuffer())
            } else {
                rc.response()
                        .setStatusCode(404)
                        .end()
            }
        })
    }

    val deployProxy: (RoutingContext) -> Unit = { rc ->
        val proxy = rc.bodyAsJson
        proxyService.deployProxy(proxy, Handler {
            if (it.succeeded()) {
                if (it.result() != null) {
                    rc.response()
                            .setStatusCode(201)
                            .putHeader("Location", "/proxy/${it.result()}")
                            .end()
                } else {
                    rc.response()
                            .setStatusCode(200)
                            .end()
                }
            } else {
                rc.response()
                        .setStatusCode(500)
                        .end()
            }
        })
    }
}