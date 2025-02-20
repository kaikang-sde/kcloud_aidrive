package com.kang.kcloud_aidrive.config;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * Snowflake ID Generator
 * @author Kai Kang
 */
@Configuration
public class SnowflakeConfig implements IdentifierGenerator {
    private final Snowflake snowflake = new Snowflake();

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return snowflake.nextId();
    }

    // Expose ID generation method for service layer
    public Long generateId() {
        return snowflake.nextId();
    }

    // Example Snowflake implementation
    static class Snowflake {
        private long workerId = 1L; // Set worker ID
        private long datacenterId = 1L; // Set datacenter ID
        private long sequence = 0L;

        private final long twepoch = 1622520000000L; // Custom epoch
        private final long workerIdBits = 5L;
        private final long datacenterIdBits = 5L;
        private final long maxWorkerId = ~(-1L << workerIdBits);
        private final long maxDatacenterId = ~(-1L << datacenterIdBits);
        private final long sequenceBits = 12L;
        private final long workerIdShift = sequenceBits;
        private final long datacenterIdShift = sequenceBits + workerIdBits;
        private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
        private final long sequenceMask = ~(-1L << sequenceBits);
        private long lastTimestamp = -1L;

        public synchronized long nextId() {
            long timestamp = System.currentTimeMillis();
            if (timestamp < lastTimestamp) {
                throw new RuntimeException("Clock moved backwards. Refusing to generate id");
            }
            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            lastTimestamp = timestamp;
            return ((timestamp - twepoch) << timestampLeftShift) |
                    (datacenterId << datacenterIdShift) |
                    (workerId << workerIdShift) |
                    sequence;
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }
}
