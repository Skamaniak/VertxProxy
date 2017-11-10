package cz.jskrabal.proxy.service

import cz.jskrabal.proxy.config.PersistenceConfig
import cz.jskrabal.proxy.loggerFor
import io.vertx.codegen.annotations.Fluent
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.serviceproxy.ProxyHelper
import java.io.File


@ProxyGen
interface PersistenceService {
    @Fluent
    fun save(key: String, value: JsonObject, result: Handler<AsyncResult<Void>>): PersistenceService

    @Fluent
    fun load(key: String, result: Handler<AsyncResult<JsonObject>>): PersistenceService

    @Fluent
    fun all(filter: String, result: Handler<AsyncResult<List<JsonObject>>>): PersistenceService

    @Fluent
    fun delete(key: String, result: Handler<AsyncResult<Void>>): PersistenceService
}

class PersistenceServiceImpl(vertx: Vertx, private val config: PersistenceConfig) : PersistenceService {
    private val fs = vertx.fileSystem()

    init {
        with(File(config.path)) {
            if (exists() && isDirectory) {
                logger.info("Mounted persistence storage from $path")
            } else {
                fs.mkdirBlocking(config.path)
                logger.info("Created persistence storage on $path")
            }
        }
    }

    override fun save(key: String, value: JsonObject, result: Handler<AsyncResult<Void>>): PersistenceService {
        fs.writeFile(pathForKey(key), value.toBuffer()) { event ->
            if (event.succeeded()) {
                logger.info("Saved file for key: $key")
                result.handle(Future.succeededFuture(event.result()))
            } else {
                logger.warn("Failed to save file for key: $key", event.cause())
                result.handle(Future.failedFuture(event.cause()))
            }
        }
        return this
    }

    override fun load(key: String, result: Handler<AsyncResult<JsonObject>>): PersistenceService {
        loadFile(pathForKey(key), Handler { event ->
            if (event.succeeded()) {
                logger.info("Loaded file for key: $key")
                result.handle(Future.succeededFuture(event.result()))
            } else {
                logger.warn("Failed to load file for key: $key", event.cause())
                result.handle(Future.failedFuture(event.cause()))
            }
        })
        return this
    }

    private fun loadFile(path: String, result: Handler<AsyncResult<JsonObject>>): PersistenceService {
        fs.readFile(path) { event ->
            if (event.succeeded()) {
                logger.info("Loaded file: $path")
                result.handle(Future.succeededFuture(event.result().toJsonObject()))
            } else {
                logger.warn("Failed to load file: $path", event.cause())
                result.handle(Future.failedFuture(event.cause()))
            }
        }
        return this
    }

    override fun all(filter: String, result: Handler<AsyncResult<List<JsonObject>>>): PersistenceService {
        fs.readDir(config.path, "$filter\\.json") {
            if (it.succeeded()) {
                CompositeFuture.all(it.result().map {
                    val future = Future.future<JsonObject>()
                    loadFile(it, future.completer())
                    future
                }.toList()).setHandler {
                    if (it.succeeded()) {
                        result.handle(Future.succeededFuture(it.result().list<JsonObject>()))
                    } else {
                        result.handle(Future.succeededFuture(emptyList()))
                    }
                }
            } else {
                result.handle(Future.succeededFuture(emptyList()))
            }
        }
        return this
    }

    override fun delete(key: String, result: Handler<AsyncResult<Void>>): PersistenceService {
        fs.delete(pathForKey(key)) { event ->
            if (event.succeeded()) {
                logger.info("Deleted file for key: $key")
                result.handle(Future.succeededFuture(event.result()))
            } else {
                logger.warn("Failed to delete file for key: $key", event.cause())
                result.handle(Future.failedFuture(event.cause()))
            }
        }
        return this
    }

    private fun pathForKey(key: String) = "${config.path}/$key.json"

    companion object {
        private val logger = loggerFor<PersistenceService>()
    }

}

object PersistenceServiceFactory {
    fun create(vertx: Vertx, config: PersistenceConfig): PersistenceService {
        return PersistenceServiceImpl(vertx, config)
    }

    fun createProxy(vertx: Vertx, address: String): PersistenceService {
        return ProxyHelper.createProxy(PersistenceService::class.java, vertx, address)
    }
}

