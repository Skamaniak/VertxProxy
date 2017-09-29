package cz.jskrabal.proxy.model

import java.io.IOException
import javax.validation.ConstraintViolation

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
            sb.append(
                    "\tConstraint violation on field ${constraintViolation.propertyPath} " +
                            "with value ${constraintViolation.invalidValue}. " +
                            "Error is: ${constraintViolation.message}."
            )
        }

    }
}

class InvalidConfigurationException(ioe: IOException) : Throwable(ioe)
