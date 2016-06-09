package cz.jskrabal.proxy.config.enums;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by janskrabal on 08/06/16.
 */
public enum IdGeneratorType implements IdGenerator<String>{
    UUID {
        @Override
        public String generateId() {
            return java.util.UUID.randomUUID().toString();
        }
    },
    RANDOM {
        @Override
        public String generateId() {
            return Long.toHexString(ThreadLocalRandom.current().nextLong());
        }
    },
    SEQUENCE {
        private final AtomicLong counter = new AtomicLong();

        @Override
        public String generateId() {
            return String.valueOf(counter.incrementAndGet());
        }
    }
}
