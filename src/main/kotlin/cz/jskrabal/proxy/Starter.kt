package cz.jskrabal.proxy

import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Launcher
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.Json

class Starter : Launcher() {

    override fun beforeStartingVertx(options: VertxOptions) {
        Json.mapper.registerModule(AfterburnerModule())
                .registerKotlinModule()
        options.preferNativeTransport = true
    }

    override fun afterStartingVertx(vertx: Vertx) {
        vertx.exceptionHandler {
            logger.error("Uncaught exception", it)
        }
    }

}