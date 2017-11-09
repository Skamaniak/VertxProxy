package cz.jskrabal.proxy

import io.vertx.core.logging.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

internal inline fun <reified T : Any> loggerFor() = LoggerFactory.getLogger(T::class.java)
internal fun loggerFor(clazz: KClass<*>) = loggerFor(clazz.java)
internal fun loggerFor(clazz: Class<*>) = LoggerFactory.getLogger(clazz)
internal val <T : Any> T.logger
    inline get() = LoggerFactory.getLogger(unwrapCompanionClass(this::class).java)


private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

private fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}
