package cz.jskrabal.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.AbstractVerticle
import java.io.IOException
import javax.validation.ConstraintViolation
import javax.validation.Validation
import kotlin.reflect.KClass


abstract class TypedConfigurationVerticle<T : Any> : AbstractVerticle() {

    abstract val configClass: KClass<T>

    val config: T by lazy {
        try {
            val tmp = ObjectMapper().registerKotlinModule().readValue(config().toString(), configClass.javaObjectType)
            val violations = Validation.buildDefaultValidatorFactory().validator.validate(tmp)
            if (!violations.isEmpty()) {
                throw ValidationFailedException(violations)
            }
            tmp
        } catch (ex: IOException) {
            throw InvalidConfigurationException(ex)
        }
    }

}

class ValidationFailedException(violations: Set<ConstraintViolation<*>>) : Throwable() {

    @Transient
    private val msg = ConstraintValidationMsg()

    init {
        violations.forEach(msg::append)
    }

    override val message: String?
        get() {
            var message = super.message
            if (!msg.isEmpty) {
                message += msg.toString()
            }
            return message
        }

    private inner class ConstraintValidationMsg internal constructor() {

        private val sb: StringBuilder = StringBuilder()

        override fun toString(): String {
            return sb.toString()
        }

        internal val isEmpty: Boolean
            get() = sb.isEmpty()

        internal fun append(constraintViolation: ConstraintViolation<*>) {
            sb.append("\tConstraint violation on field ")
                    .append(constraintViolation.propertyPath)
                    .append(" with value ")
                    .append(constraintViolation.invalidValue)
                    .append(". Error is: ")
                    .append(constraintViolation.message).append(". ")
        }

    }
}

class InvalidConfigurationException(ioe: IOException) : Throwable(ioe)
