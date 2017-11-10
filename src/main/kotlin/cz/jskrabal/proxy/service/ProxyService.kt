package cz.jskrabal.proxy.service

import cz.jskrabal.proxy.config.ProxyConfig
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.model.Proxy
import cz.jskrabal.proxy.verticle.PersistenceServiceVerticle
import cz.jskrabal.proxy.verticle.ProxyVerticle
import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.LocalMap
import io.vertx.serviceproxy.ProxyHelper
import kotlin.collections.set

@ProxyGen
interface ProxyService {

    @Fluent
    fun deployProxy(proxy: JsonObject, result: Handler<AsyncResult<String>>): ProxyService

    @Fluent
    fun undeployProxy(proxyId: String, result: Handler<AsyncResult<Void>>): ProxyService

    @Fluent
    fun getProxy(proxyId: String, result: Handler<AsyncResult<JsonObject>>): ProxyService

    @Fluent
    fun getAllProxies(result: Handler<AsyncResult<List<JsonObject>>>): ProxyService

    @Fluent
    fun reload(result: Handler<AsyncResult<List<JsonObject>>>): ProxyService

}

class ProxyServiceImpl(private val vertx: Vertx,
                       private val config: ProxyConfig,
                       private val proxyRegistry: LocalMap<String, JsonObject> = vertx.sharedData().getLocalMap<String, JsonObject>("proxy-registry"),
                       private val persistenceService: PersistenceService = PersistenceServiceFactory.createProxy(vertx, PersistenceServiceVerticle.SERVICE_ADDRESS)) : ProxyService {

    override fun deployProxy(proxy: JsonObject, result: Handler<AsyncResult<String>>): ProxyService {
        val mapped = proxy.mapTo(Proxy::class.java)
        if (!proxyRegistry.containsKey(mapped.id)) {
            if (proxyRegistry.none { it.value.getInteger("port") == mapped.port }) {
                persistenceService.save(proxyKey(mapped.id), proxy, Handler {
                    if (it.succeeded()) {
                        vertx.deployVerticle(ProxyVerticle(mapped, config)) {
                            proxyRegistry[mapped.id] = mapped
                                    .copy(deploymentId = it.result())
                                    .toJson()
                            logger.info("Deployed proxy ID: ${mapped.id}")
                            result.handle(Future.succeededFuture(mapped.id))
                        }
                    } else {
                        result.handle(Future.failedFuture(it.cause()))
                    }
                })
            } else {
                result.handle(Future.failedFuture(IllegalArgumentException("Port ${mapped.port} already assigned.")))
            }
        } else {
            result.handle(Future.succeededFuture())
        }
        return this
    }


    override fun undeployProxy(proxyId: String, result: Handler<AsyncResult<Void>>): ProxyService {
        if (proxyRegistry.containsKey(proxyId)) {
            vertx.undeploy(proxyRegistry[proxyId]?.getString("deploymentId")) {
                persistenceService.delete(proxyKey(proxyId), Handler {
                    proxyRegistry.remove(proxyId)
                    logger.info("Undeployed proxy ID: $proxyId")
                    result.handle(Future.succeededFuture())
                })
            }
        } else {
            result.handle(Future.failedFuture("Proxy with ID: $proxyId not found in the registry."))
        }
        return this
    }

    override fun getProxy(proxyId: String, result: Handler<AsyncResult<JsonObject>>): ProxyService {
        result.handle(Future.succeededFuture(proxyRegistry[proxyId]))
        return this
    }

    override fun getAllProxies(result: Handler<AsyncResult<List<JsonObject>>>): ProxyService {
        result.handle(Future.succeededFuture(ArrayList(proxyRegistry.values)))
        return this
    }

    override fun reload(result: Handler<AsyncResult<List<JsonObject>>>): ProxyService {
        persistenceService.all(".*-proxy", Handler {
            if (it.succeeded()) {
                CompositeFuture.all(it.result().map {
                    Future.future<String>().apply {
                        deployProxy(it, this.completer())
                    }
                }.toList()).setHandler {
                    if (it.succeeded()) {
                        result.handle(Future.succeededFuture())
                    } else {
                        result.handle(Future.failedFuture(it.cause()))
                    }
                }
            }
        })
        return this
    }

    private fun proxyKey(key: String) = "$key-proxy"

    companion object {
        private val logger = loggerFor<ProxyService>()
    }

}

object ProxyServiceFactory {
    fun create(vertx: Vertx, config: ProxyConfig): ProxyService {
        return ProxyServiceImpl(vertx, config)
    }

    fun createProxy(vertx: Vertx, address: String): ProxyService {
        return ProxyHelper.createProxy(ProxyService::class.java, vertx, address)
    }
}