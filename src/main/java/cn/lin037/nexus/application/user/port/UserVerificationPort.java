package cn.lin037.nexus.application.user.port;

import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;

/**
 * 用户验证服务接口
 *
 * @author LinSanQi
 */
public interface UserVerificationPort {

    /**
     * 创建用户验证令牌并存入缓存
     *
     * @param identify 用户标识
     * @param type     验证类型
     * @return 验证码
     */
    String createVerificationToken(String identify, VerificationTypeEnum type);

    /**
     * 获取用户验证令牌
     *
     * @param identify 用户ID
     * @param type     验证类型
     * @return 验证令牌，如果不存在则返回null
     */
    String getVerificationToken(String identify, VerificationTypeEnum type);


    /**
     * 验证用户验证码
     *
     * @param identify 用户ID
     * @param code     验证码
     * @param type     验证类型
     * @return 验证结果，true为验证通过
     */
    boolean verifyCode(String identify, String code, VerificationTypeEnum type);


    /**
     * 删除用户验证令牌
     *
     * @param identify 用户ID
     * @param type     验证类型
     */
    void deleteVerificationToken(String identify, VerificationTypeEnum type);


    /**
     * 检查操作频率限制是否已达到
     *
     * @param identify 用户标识
     * @param type     验证类型
     * @return 是否已达到限制
     */
    boolean hasRateLimitReached(String identify, VerificationTypeEnum type);
}