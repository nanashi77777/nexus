package cn.lin037.nexus.infrastructure.common.notification.service.impl;

import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.notification.config.NotificationProperties;
import cn.lin037.nexus.infrastructure.common.notification.enums.NotificationType;
import cn.lin037.nexus.infrastructure.common.notification.exception.NotificationExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.notification.service.NotificationService;
import cn.lin037.nexus.infrastructure.common.notification.template.TemplateService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 邮件通知服务实现
 *
 * @author LinSanQi
 */
@Slf4j
@Service
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateService templateService;
    private final NotificationProperties properties;
    private final RedissonClient redissonClient;
    private final AtomicInteger waitingThreads = new AtomicInteger(0);
    private RRateLimiter rateLimiter;
    private volatile int maxWaitingThreads;

    public EmailNotificationService(JavaMailSender mailSender, TemplateService templateService, NotificationProperties properties, RedissonClient redissonClient) {
        this.mailSender = mailSender;
        this.templateService = templateService;
        this.properties = properties;
        this.redissonClient = redissonClient;
    }

    @PostConstruct
    public void init() {
        if (properties.getRateLimiter().isEnabled()) {
            this.maxWaitingThreads = properties.getRateLimiter().getMaxWaiters();
            this.rateLimiter = redissonClient.getRateLimiter("NEXUS:notification:global-rate-limiter");
            // 设置速率，每分钟N次
            boolean rateSet = this.rateLimiter.trySetRate(
                    org.redisson.api.RateType.OVERALL,
                    properties.getRateLimiter().getPermitsPerMinute(),
                    java.time.Duration.ofMinutes(1)
            );
            if (!rateSet) {
                log.warn("速率限制器已存在配置，未重新设置");
            }
        }
    }

    /**
     * 动态更新最大等待队列容量
     *
     * @param newMaxWaiters 新的容量值
     */
    public void updateMaxWaiters(int newMaxWaiters) {
        log.info("通知服务最大等待队列容量从 {} 更新为 {}", this.maxWaitingThreads, newMaxWaiters);
        this.maxWaitingThreads = newMaxWaiters;
    }

    @Override
    public void send(String to, String subject, String content) {
        // 如果未启用频率控制，则直接发送
        if (!properties.getRateLimiter().isEnabled()) {
            doSend(to, subject, content);
            return;
        }

        // 检查等待队列是否已满
        if (waitingThreads.incrementAndGet() > this.maxWaitingThreads) {
            waitingThreads.decrementAndGet();
            log.warn("通知等待队列已满 (超过 {} 个)，已拒绝发送请求", this.maxWaitingThreads);
            throw new InfrastructureException(NotificationExceptionCodeEnum.WAIT_QUEUE_FULL);
        }

        try {
            // 获取令牌，此操作会阻塞直到获取成功
            rateLimiter.acquire();
            // 执行发送
            doSend(to, subject, content);
        } finally {
            // 确保等待计数器被正确释放
            waitingThreads.decrementAndGet();
        }
    }

    @Override
    public void send(String to, String subject, String templateId, Map<String, Object> variables) {
        String content = templateService.renderTemplate(templateId, variables);
        send(to, subject, content);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    /**
     * 实际执行邮件发送的核心逻辑
     */
    private void doSend(String to, String subject, String content) {
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, "UTF-8");
            String fromEmail = properties.getEmail().getFrom();
            if (!StringUtils.hasText(fromEmail)) {
                throw new InfrastructureException(NotificationExceptionCodeEnum.INVALID_CONFIGURATION, "发件人邮箱未配置");
            }
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            // true表示支持HTML
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
            log.info("邮件发送成功 -> To: {}, Subject: {}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败 -> To: {}, Subject: {}", to, subject, e);
            throw new InfrastructureException(NotificationExceptionCodeEnum.SENDING_ERROR, e);
        }
    }
} 