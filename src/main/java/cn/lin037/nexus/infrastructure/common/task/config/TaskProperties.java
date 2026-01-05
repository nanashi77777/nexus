package cn.lin037.nexus.infrastructure.common.task.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 任务模块的配置属性。
 * <p>
 * 从 `application-task.yml` 文件中加载配置。
 *
 * @author LinSanQi
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nexus.task")
public class TaskProperties {

    /**
     * 是否启用任务调度器。
     */
    private boolean enabled = true;

    /**
     * 调度器轮询数据库以查找新任务的配置。
     */
    private PollingConfig polling = new PollingConfig();

    /**
     * 短时间运行任务的执行器配置（使用虚拟线程）。
     */
    private ShortRunningExecutorConfig shortRunning = new ShortRunningExecutorConfig();

    /**
     * 长时间运行任务的执行器配置（使用平台线程池）。
     */
    private LongRunningExecutorConfig longRunning = new LongRunningExecutorConfig();

    /**
     * 将特定的 taskType 映射到长时间运行的执行器。
     * <p>
     * 未在此处列出的任何 taskType 都将默认使用短时执行器。
     * <p>
     * 示例 (application.yml):
     * <pre>
     * nexus:
     *   task:
     *     mapping:
     *       long-running:
     *         - "video-processing"
     *         - "data-report-generation"
     * </pre>
     */
    private Map<String, List<String>> mapping = Collections.singletonMap("long-running", Collections.emptyList());

    /**
     * 任务超时配置
     */
    private TimeoutConfig timeout = new TimeoutConfig();


    @Data
    public static class PollingConfig {
        /**
         * 轮询之间的时间间隔。
         */
        private long interval = 5;
        /**
         * 轮询间隔的时间单位。
         */
        private TimeUnit unit = TimeUnit.SECONDS;
        /**
         * 每次轮询从数据库获取的最大任务数。
         */
        private int batchSize = 10;
    }

    @Data
    public static class ShortRunningExecutorConfig {
        /**
         * 短时任务执行器的最大并发数。
         */
        private int concurrency = 200;
    }

    @Data
    public static class LongRunningExecutorConfig {
        /**
         * 长时任务执行器的核心线程数。
         */
        private int corePoolSize = 4;
        /**
         * 长时任务执行器的最大线程数。
         */
        private int maxPoolSize = 16;
        /**
         * 线程空闲时的存活时间（秒）。
         */
        private long keepAliveTime = 60;
        /**
         * 任务队列的容量。
         */
        private int queueCapacity = 100;
    }

    @Data
    public static class TimeoutConfig {
        /**
         * 是否启用超时监控
         */
        private boolean enabled = true;

        /**
         * 默认超时时间
         */
        private long defaultDuration = 30;

        /**
         * 超时时间的单位
         */
        private TimeUnit defaultUnit = TimeUnit.MINUTES;

        /**
         * 超时监控轮询间隔（秒）
         */
        private long checkIntervalSeconds = 60;
    }
} 