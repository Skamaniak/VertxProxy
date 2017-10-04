package cz.jskrabal.proxy.controller

import com.github.aesteve.vertx.nubes.annotations.Controller
import com.github.aesteve.vertx.nubes.annotations.params.Param
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody
import com.github.aesteve.vertx.nubes.annotations.routing.http.DELETE
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST
import com.github.aesteve.vertx.nubes.annotations.services.ServiceProxy
import cz.jskrabal.proxy.service.ProxyService
import cz.jskrabal.proxy.verticle.ProxyServiceVerticle
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

@Controller("/api/v1/proxy")
class ProxyController {

    @ServiceProxy(ProxyServiceVerticle.SERVICE_ADDRESS)
    private lateinit var proxyService: ProxyService

    @POST("/")
    fun deployProxy(rc: RoutingContext, @RequestBody proxy: JsonObject) {
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
                rc.fail(it.cause())
            }
        })
    }

    @GET("/:id")
    fun getProxy(rc: RoutingContext, @Param("id") id: String) {
        proxyService.getProxy(id, Handler {
            if (it.succeeded() && it.result() != null) {
                rc.response().endWithJson(it.result())
            } else {
                rc.fail(404)
            }
        })
    }

    @DELETE("/:id")
    fun deleteProxy(rc: RoutingContext, @Param("id") id: String) {
        proxyService.undeployProxy(id, Handler {
            if (it.succeeded()) {
                rc.response().end()
            } else {
                rc.fail(it.cause())
            }
        })
    }

    private fun HttpServerResponse.endWithJson(obj: Any) {
        this.putHeader("Content-Type", "application/json").end(Json.encode(obj))
    }
}