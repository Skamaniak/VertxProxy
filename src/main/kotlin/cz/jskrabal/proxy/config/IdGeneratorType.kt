package cz.jskrabal.proxy.config

import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by janskrabal on 08/06/16.
 */
enum class IdGeneratorType : IdGenerator<String> {

    /**
     * Generates UUIDs.
     * Warning: Generating of UUIDs is slow and may degrade the performance of proxy.
     */
    UUID {
        override fun generateId(): String {
            return java.util.UUID.randomUUID().toString()
        }
    },
    /**
     * This generator is much faster than UUID generator and do not block as the SEQUENCE generator.
     * Warning: The probability of generating the same id is much higher than for UUID generator.
     */
    RANDOM {
        override fun generateId(): String {
            return java.lang.Long.toHexString(ThreadLocalRandom.current().nextLong())
        }
    },
    /**
     * Returns the increasing sequence of numbers.
     * Warning: The generator is synchronized thus it may block.
     */
    SEQUENCE {
        private val counter = AtomicLong()

        override fun generateId(): String {
            return counter.incrementAndGet().toString()
        }
    }
}
