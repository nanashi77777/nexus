package cn.lin037.nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author LinSanQi
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 原有的线程池，用于教程相关的异步任务
     * 
     * @return ExecutorService 线程池执行器
     */
    @Bean("tutorialTaskExecutor")
    public ExecutorService tutorialTaskExecutor() {
        return new ThreadPoolExecutor(
                // 核心线程数5，最大线程数10
                5, 10,
                // 空闲线程存活时间60秒
                60L, TimeUnit.SECONDS,
                // 使用有界队列，容量100
                new LinkedBlockingQueue<>(100),
                // 默认线程工厂创建线程
                Executors.defaultThreadFactory(),
                // 等待策略：由调用线程处理任务（即提交任务的线程自己执行任务）
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    /**
     * 专门用于Spring MVC异步处理的线程池
     * 用于替代Spring MVC默认的SimpleAsyncTaskExecutor
     * 避免生产环境警告并提供更好的线程池管理
     * 针对二核四线程服务器优化配置
     * 
     * @return AsyncTaskExecutor 异步任务执行器
     */
    @Bean("mvcAsyncTaskExecutor")
    public AsyncTaskExecutor mvcAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：适合二核四线程服务器
        executor.setCorePoolSize(2);
        // 最大线程数：不超过服务器线程数
        executor.setMaxPoolSize(4);
        // 队列容量：适中的队列大小
        executor.setQueueCapacity(50);
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名称前缀
        executor.setThreadNamePrefix("mvc-async-");
        // 拒绝策略：由调用线程处理任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化线程池
        executor.initialize();
        
        return executor;
    }
}
