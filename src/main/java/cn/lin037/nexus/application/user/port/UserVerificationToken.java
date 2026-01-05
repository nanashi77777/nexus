package cn.lin037.nexus.application.user.port;


import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;

import java.time.Instant;

/**
 * @param identify    用户标识
 * @param code        验证码
 * @param createdTime 创建时间
 * @param type        验证类型
 * @author LinSanQi
 * 封装用户标识、验证码、创建时间和验证类型
 */
public record UserVerificationToken(String identify, String code, Instant createdTime,
                                    VerificationTypeEnum type) {

    /**
     * 验证 token 是否有效，并增加一个阈值缓冲时间
     *
     * @param code             用户输入的验证码
     * @param now              当前时间戳
     * @param thresholdSeconds 缓冲时间（秒），防止时钟误差或网络延迟
     * @return 是否有效
     */
    public boolean isValid(String code, Instant now, long thresholdSeconds) {
        return this.code.equals(code)
                && !now.isBefore(createdTime)
                && now.isBefore(createdTime.plusSeconds(type.getExpiryMinutes() * 60L + thresholdSeconds));
    }

    /**
     * 默认使用 10 秒缓冲时间进行验证
     */
    public boolean isValid(String code, Instant now) {
        return isValid(code, now, 10);
    }

    /**
     * 使用当前时间和默认10秒缓冲进行验证
     */
    public boolean isValid(String code) {
        return isValid(code, Instant.now(), 10);
    }
}
