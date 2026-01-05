package cn.lin037.nexus.application.user.enums;

import lombok.Getter;

/**
 * @author LinSanQi
 */

@Getter
public enum VerificationTypeEnum {
    // 用户注册，有效期5分钟
    REGISTRATION("registration", 5, "账号注册"),
    // 重置密码，有效期5分钟
    PASSWORD_RESET("password_reset", 5, "重置密码"),
    // 更新邮箱，有效期5分钟
    EMAIL_CHANGE("email_change", 5, "更新邮箱"),
    // 重置手机号，有效期5分钟
    PHONE_CHANGE("phone_change", 5, "重置手机号");

    private final String value;
    private final int expiryMinutes;
    private final String description;

    VerificationTypeEnum(String value, int expiryMinutes, String description) {
        this.value = value;
        this.expiryMinutes = expiryMinutes;
        this.description = description;
    }
}
