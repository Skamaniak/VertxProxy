package cz.jskrabal.proxy.verticle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import cz.jskrabal.proxy.loggerFor
import cz.jskrabal.proxy.model.InvalidConfigurationException
import cz.jskrabal.proxy.model.ValidationFailedException
import io.vertx.core.AbstractVerticle
import java.io.IOException

import javax.validation.Validation
import kotlin.reflect.KClass


abstract class TypedConfigurationVerticle<T : Any> : AbstractVerticle() {

    protected abstract val configClass: KClass<T>

    val config: T by lazy {
        try {
            val configuration = ObjectMapper().registerKotlinModule().readValue(config().toString(), configClass.javaObjectType)
            val violations = Validation.buildDefaultValidatorFactory().validator.validate(configuration)
            if (!violations.isEmpty()) {
                throw ValidationFailedException(violations)
            }
            logger.info("Loaded configuration: $configuration")
            configuration
        } catch (ex: IOException) {
            throw InvalidConfigurationException(ex)
        }
    }

    companion object {
        private val logger = loggerFor<TypedConfigurationVerticle<*>>()
    }

}
