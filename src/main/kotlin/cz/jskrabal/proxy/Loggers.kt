package cz.jskrabal.proxy

import io.vertx.core.logging.LoggerFactory

internal inline fun <reified T : Any> loggerFor() = LoggerFactory.getLogger(T::class.java)
