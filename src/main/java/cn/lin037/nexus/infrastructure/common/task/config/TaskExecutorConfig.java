package cn.lin037.nexus.infrastructure.common.task.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * 异步任务模块的配置类
 *
 * @author LinSanQi
 */
@Configuration
@ConditionalOnProperty(name = "nexus.task.enabled", havingValue = "true", matchIfMissing = true)
public class TaskExecutorConfig {

    public static final String SHORT_RUNNING_TASK_EXECUTOR = "shortRunningTaskExecutor";
    public static final String LONG_RUNNING_TASK_EXECUTOR = "longRunningTaskExecutor";
    public static final String TASK_SCHEDULER_EXECUTOR = "taskSchedulerExecutor";

    /**
     * 定义用于执行短时I/O密集型任务的虚拟线程池。
     *
     * @return ExecutorService bean for short-running tasks
     */
    @Bean(name = SHORT_RUNNING_TASK_EXECUTOR)
    public ExecutorService shortRunningTaskExecutor() {
        // 使用 JDK 21 的虚拟线程，为每个任务创建一个轻量级线程。
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 定义用于执行长时CPU密集型任务的平台线程池。
     *
     * @param properties 任务配置属性
     * @return ExecutorService bean for long-running tasks
     */
    @Bean(name = LONG_RUNNING_TASK_EXECUTOR)
    public ExecutorService longRunningTaskExecutor(TaskProperties properties) {
        TaskProperties.LongRunningExecutorConfig config = properties.getLongRunning();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds((int) config.getKeepAliveTime());
        executor.setThreadNamePrefix("long-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor.getThreadPoolExecutor();
    }

    /**
     * 定义用于轮询任务的单线程调度器。
     *
     * @return ScheduledExecutorService for the poller
     */
    @Bean(name = TASK_SCHEDULER_EXECUTOR, destroyMethod = "shutdown")
    public ScheduledExecutorService taskSchedulerExecutor() {
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "task-scheduler-poller");
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
} 