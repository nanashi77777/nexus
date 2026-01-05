package cn.lin037.nexus.infrastructure.adapter.user;

import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;
import cn.lin037.nexus.application.user.port.UserVerificationPort;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.cache.service.CacheService;
import cn.lin037.nexus.infrastructure.common.cache.util.CacheKeyBuilder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class UserVerificationAdapter implements UserVerificationPort {

    // 频率限制配置
    private static final long SINGLE_SEND_INTERVAL_SECONDS = 60; // 1分钟内只允许发送一次
    private static final long RATE_LIMIT_WINDOW_MINUTES = 30;    // 30分钟的速率限制窗口
    private static final int RATE_LIMIT_MAX_REQUESTS = 7;       // 窗口内最大请求次数
    private static final long BLOCK_DURATION_HOURS = 12;         // 达到限制后封禁12小时
    private final CacheService cacheService;
    private final SecureRandom random = new SecureRandom();

    public UserVerificationAdapter(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public String createVerificationToken(String identify, VerificationTypeEnum type) {
        if (hasRateLimitReached(identify, type)) {
            throw new ApplicationException(CommonResultCodeEnum.TOO_MANY_REQUESTS);
        }

        // 设置1分钟冷却
        String minuteKey = getRateLimitMinuteKey(identify, type);
        cacheService.set(minuteKey, "1", SINGLE_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 更新30分钟内的计数器
        String windowKey = getRateLimitCounterKey(identify, type);
        long count = cacheService.increment(windowKey);
        if (count == 1) {
            cacheService.expire(windowKey, RATE_LIMIT_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        // 生成并存储验证码
        String code = generateCode();
        String codeKey = getVerificationCodeKey(identify, type);
        cacheService.set(codeKey, code, type.getExpiryMinutes(), TimeUnit.MINUTES);

        return code;
    }

    @Override
    public String getVerificationToken(String identify, VerificationTypeEnum type) {
        String codeKey = getVerificationCodeKey(identify, type);
        return cacheService.get(codeKey);
    }

    @Override
    public boolean verifyCode(String identify, String code, VerificationTypeEnum type) {
        String storedCode = getVerificationToken(identify, type);
        return code != null && code.equals(storedCode);
    }

    @Override
    public void deleteVerificationToken(String identify, VerificationTypeEnum type) {
        String codeKey = getVerificationCodeKey(identify, type);
        cacheService.delete(codeKey);
    }

    @Override
    public boolean hasRateLimitReached(String identify, VerificationTypeEnum type) {
        // 检查是否被封禁
        if (cacheService.exists(getRateLimitBlockKey(identify, type))) {
            return true;
        }

        // 检查1分钟冷却
        if (cacheService.exists(getRateLimitMinuteKey(identify, type))) {
            return true;
        }

        // 检查30分钟内的发送次数
        String windowKey = getRateLimitCounterKey(identify, type);
        Long count = cacheService.get(windowKey);
        if (count != null && count >= RATE_LIMIT_MAX_REQUESTS) {
            // 达到限制，设置封禁并清理计数器
            String blockKey = getRateLimitBlockKey(identify, type);
            cacheService.set(blockKey, "1", BLOCK_DURATION_HOURS, TimeUnit.HOURS);
            cacheService.delete(windowKey);
            return true;
        }

        return false;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    // Key构建辅助方法
    private String getVerificationCodeKey(String identify, VerificationTypeEnum type) {
        return CacheKeyBuilder.buildKey("user", "verification", "code", identify, type.name().toLowerCase());
    }

    private String getRateLimitMinuteKey(String identify, VerificationTypeEnum type) {
        return CacheKeyBuilder.buildKey("user", "verification", "rate_limit", "minute", identify, type.name().toLowerCase());
    }

    private String getRateLimitCounterKey(String identify, VerificationTypeEnum type) {
        return CacheKeyBuilder.buildKey("user", "verification", "rate_limit", "counter", identify, type.name().toLowerCase());
    }

    private String getRateLimitBlockKey(String identify, VerificationTypeEnum type) {
        return CacheKeyBuilder.buildKey("user", "verification", "rate_limit", "block", identify, type.name().toLowerCase());
    }
}
