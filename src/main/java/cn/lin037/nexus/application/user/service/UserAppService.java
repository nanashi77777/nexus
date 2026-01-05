package cn.lin037.nexus.application.user.service;


import cn.lin037.nexus.application.user.vo.LoginVO;
import cn.lin037.nexus.web.rest.v1.user.req.*;


/**
 * 用户应用服务接口
 *
 * @author LinSanQi
 */
public interface UserAppService {

    /**
     * 注册用户
     *
     * @param request 注册请求
     * @return 用户ID
     */
    Long registerUser(RegisterUserReq request);

    /**
     * 发送验证码
     *
     * @param request 发送验证码请求
     */
    void sendRegisterVerification(SendVerificationReq request);

    /**
     * 修改用户密码
     *
     * @param request 修改密码请求
     */
    void changePassword(ChangePasswordReq request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginVO login(LoginReq request);

    /**
     * 忘记密码
     *
     * @param request 忘记密码请求
     */
    void forgotPassword(ForgotPasswordReq request);

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     */
    void resetPassword(ResetPasswordReq request);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 用户账号注销
     *
     * @param request 注销请求
     */
    void deleteUserAccount(DeactivateAccountReq request);

    /**
     * 发送修改邮箱验证码
     *
     * @param request 发送验证码请求
     */
    void sendUpdateEmailVerification(SendVerificationReq request);

    /**
     * 修改用户邮箱
     *
     * @param request 修改邮箱请求
     */
    void updateEmail(ChangeEmailReq request);
}
