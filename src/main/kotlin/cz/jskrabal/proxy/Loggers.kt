package cz.jskrabal.proxy

import io.vertx.core.logging.LoggerFactory
import kotlin.reflect.KClass

internal inline fun <reified T : Any> loggerFor() = LoggerFactory.getLogger(T::class.java)
internal fun loggerFor(clazz: KClass<*>) = loggerFor(clazz.java)
internal fun loggerFor(clazz: Class<*>) = LoggerFactory.getLogger(clazz)
