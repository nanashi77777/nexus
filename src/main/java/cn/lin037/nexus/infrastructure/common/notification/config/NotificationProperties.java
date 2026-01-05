package cn.lin037.nexus.infrastructure.common.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通知服务配置属性
 *
 * @author LinSanQi
 */
@Data
@Component
@ConfigurationProperties(prefix = "nexus.notification")
public class NotificationProperties {

    /**
     * 邮件相关配置
     */
    private Email email = new Email();

    /**
     * 频率控制配置
     */
    private RateLimiter rateLimiter = new RateLimiter();

    @Data
    public static class Email {
        /**
         * 默认的发件人邮箱地址
         */
        private String from;
    }

    @Data
    public static class RateLimiter {
        /**
         * 是否启用全局频率限制
         */
        private boolean enabled = true;
        /**
         * 每分钟允许发送的通知总数
         */
        private long permitsPerMinute = 60;
        /**
         * 最大等待线程数
         * 当请求速率超过 permitsPerMinute 时，允许在队列中等待的请求数。
         * 超过此数量的请求将被立即拒绝。
         */
        private int maxWaiters = 100;
    }
} 