package cn.lin037.nexus.application.user.port;

import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;

/**
 * @author LinSanQi
 */
public interface UserNotificationPort {

    /**
     * 发送邮箱验证码
     *
     * @param email            邮箱
     * @param code             验证码
     * @param verificationType 验证码类型
     */
    void sendEmailVerification(String email, String code, VerificationTypeEnum verificationType);

    /**
     * 发送欢迎邮件
     *
     * @param email 邮箱
     */
    void sendWelcomeEmail(String email, String username);
}
