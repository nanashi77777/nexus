package cn.lin037.nexus.infrastructure.common.id;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author LinSanQi
 */
@Slf4j
@Component
@Deprecated
public class SnowflakeIdGenerator {
    // 开始时间戳（2024-01-01 00:00:00）- 写死，永不改变
    private static final long EPOCH = 1704038400000L;

    // 位移常量 - 写死，永不改变
    private static final int SEQUENCE_BITS = 12;
    private static final int WORKER_ID_BITS = 5;
    private static final int DATACENTER_ID_BITS = 5;
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final int DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;

    // 最大值常量 - 写死，永不改变
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    // 默认值 - 单体应用使用
    private static final int DEFAULT_DATACENTER_ID = 0;
    private static final int DEFAULT_WORKER_ID = 0;
    // 服务器标识
    // 服务器ID（0-31）
    private final long datacenterId;
    // 服务ID（0-31）
    private final long workerId;
    // 运行时状态
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public SnowflakeIdGenerator() {
        this(DEFAULT_DATACENTER_ID, DEFAULT_WORKER_ID);
    }

    public SnowflakeIdGenerator(
            @Value("${snowflake.datacenter-id:" + DEFAULT_DATACENTER_ID + "}") int datacenterId,
            @Value("${snowflake.worker-id:" + DEFAULT_WORKER_ID + "}") int workerId) {
        // 验证参数
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID must be between 0 and %d", MAX_DATACENTER_ID));
        }
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }

        this.datacenterId = datacenterId;
        this.workerId = workerId;

        log.info("Snowflake initialized with datacenterId={}, workerId={}, epoch={}",
                datacenterId, workerId, EPOCH);
    }

    public synchronized String generateId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    TimeUnit.MILLISECONDS.sleep(offset);
                    timestamp = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Clock moved backwards, interrupted while waiting");
                }
            } else {
                throw new RuntimeException("Clock moved backwards too much: " + offset + "ms");
            }
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return String.valueOf(
                ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                        | (datacenterId << DATACENTER_ID_SHIFT)
                        | (workerId << WORKER_ID_SHIFT)
                        | sequence
        );
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}