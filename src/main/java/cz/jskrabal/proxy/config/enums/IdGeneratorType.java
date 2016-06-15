package cz.jskrabal.proxy.config.enums;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by janskrabal on 08/06/16.
 */
public enum IdGeneratorType implements IdGenerator<String>{

    /**
     * Generates UUIDs.
     * Warning: Generating of UUIDs is slow and may degrade the performance of proxy.
     */
    UUID {
        @Override
        public String generateId() {
            return java.util.UUID.randomUUID().toString();
        }
    },
    /**
     * This generator is much faster than UUID generator and do not block as the SEQUENCE generator.
     * Warning: The probability of generating the same id is much higher than for UUID generator.
     */
    RANDOM {
        @Override
        public String generateId() {
            return Long.toHexString(ThreadLocalRandom.current().nextLong());
        }
    },
    /**
     * Returns the increasing sequence of numbers.
     * Warning: The generator is synchronized thus it may block.
     */
    SEQUENCE {
        private final AtomicLong counter = new AtomicLong();

        @Override
        public String generateId() {
            return String.valueOf(counter.incrementAndGet());
        }
    }
}
