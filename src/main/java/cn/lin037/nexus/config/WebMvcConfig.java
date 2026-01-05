package cn.lin037.nexus.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.fun.strategy.SaCorsHandleFunction;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.infrastructure.common.file.util.FileHandlingUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;

/**
 * @author LinSanQi
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${nexus.file.storage.path}")
    private String storageBasePath;

    private final AsyncTaskExecutor asyncTaskExecutor;

    /**
     * 构造函数注入专门用于MVC异步处理的任务执行器
     *
     * @param asyncTaskExecutor MVC异步任务执行器
     */
    public WebMvcConfig(@Qualifier("mvcAsyncTaskExecutor") AsyncTaskExecutor asyncTaskExecutor) {
        this.asyncTaskExecutor = asyncTaskExecutor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path storagePath = FileHandlingUtil.getPhysicalBasePath(storageBasePath, true);
        String absolutePath = storagePath.toAbsolutePath().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许所有请求路径跨域访问
        registry.addMapping("/**")
                // 允许哪些域名进行跨域访问
                .allowedOriginPatterns("*")
                // 允许的请求方法类型
                .allowedMethods("*")
                // 是否携带Cookie，默认false
                .allowCredentials(true)
                // 预检请求的缓存时间（单位：秒）
                .maxAge(3600)
                // 允许的请求头类型
                .allowedHeaders("*");
    }

    /**
     * CORS 跨域处理策略
     */
    @Bean
    public SaCorsHandleFunction corsHandle() {
        return (req, res, sto) -> {
            res.
                    // 允许指定域访问跨域资源
            setHeader("Access-Control-Allow-Origin", "*")
                    // 允许所有请求方式
           .setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
                    // 有效时间
//         .setHeader("Access-Control-Max-Age", "3600")
                    // 允许的header参数
            .setHeader("Access-Control-Allow-Headers", "*");

            // 如果是预检请求，则立即返回到前端
            SaRouter.match(SaHttpMethod.OPTIONS)
                    .free(r -> log.info("--------OPTIONS预检请求，不做处理"))
                    .back();
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/api/v1/user/login",
                        "/api/v1/user/register",
                        "/api/v1/user/send-register-code",
                        "/api/v1/user/forgot-password",
                        "/api/v1/user/reset-password",
                        "/test.html",
                        "/api/v1/agent/chat/stream"
                );
    }

    /**
     * 配置异步请求支持
     * 仅对流式接口（如Agent聊天的Flux接口）使用自定义线程池
     * 其他接口继续使用Spring Boot默认的虚拟线程以获得更好的性能
     *
     * @param configurer 异步支持配置器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 设置异步请求超时时间15分钟（900秒）
        configurer.setDefaultTimeout(15 * 60 * 1000);

        // 创建条件性的任务执行器，只对特定路径使用自定义线程池
        configurer.setTaskExecutor(new ConditionalAsyncTaskExecutor());
    }

    /**
     * 条件性异步任务执行器
     * 根据请求路径决定使用自定义线程池还是默认虚拟线程
     */
    private class ConditionalAsyncTaskExecutor implements AsyncTaskExecutor {

        @Override
        public void execute(@NotNull Runnable task) {
            // 检查当前请求是否为流式接口
            if (isStreamingRequest()) {
                // 流式接口使用自定义线程池
                asyncTaskExecutor.execute(task);

            } else {
                // 其他接口使用默认执行（虚拟线程）
                task.run();
            }
        }

        /**
         * 检查当前请求是否为流式接口
         * 主要针对Agent聊天的流式响应接口
         */
        private boolean isStreamingRequest() {
            try {
                // 获取当前HTTP请求
                var request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                String requestURI = request.getRequestURI();

                // 只对Agent聊天的流式接口使用自定义线程池
                return requestURI.contains("/api/v1/agent/chat/stream") ||
                        requestURI.contains("/api/v1/agent/chat/allow-tools");
            } catch (Exception e) {
                // 如果无法获取请求信息，默认使用虚拟线程
                return false;
            }
        }
    }
}
