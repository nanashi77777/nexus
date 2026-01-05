package cn.lin037.nexus.web.rest.v1.user;

import cn.lin037.nexus.application.user.service.UserAppService;
import cn.lin037.nexus.application.user.vo.LoginVO;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.user.req.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * 用户操作接口
 * @author LinSanQi
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserAppService userAppService;

    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /**
     * 用户注册
     * COMPLETED
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    public ResultVO<Long> registerUser(
            @Valid @RequestBody RegisterUserReq request) {
        Long userId = userAppService.registerUser(request);
        return ResultVO.success(userId);
    }

    /**
     * 发送验证码
     * COMPLETED
     * @param request 发送验证码请求
     * @return 发送结果
     */
    @PostMapping("/send-register-code")
    public ResultVO<Void> sendVerification(
            @Valid @RequestBody SendVerificationReq request) {
        userAppService.sendRegisterVerification(request);
        return ResultVO.success();
    }

    /**
     * 修改密码
     * COMPLETED
     * @param request 修改密码请求
     * @return 修改结果
     */
    @PutMapping("/change-password")
    public ResultVO<String> changePassword(
            @Valid @RequestBody ChangePasswordReq request) {
        userAppService.changePassword(request);
        return ResultVO.success();
    }

    /**
     * 忘记密码
     * COMPLETED
     * @param request 忘记密码请求
     * @return 忘记密码结果
     */
    @PostMapping("/forgot-password")
    public ResultVO<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordReq request) {
        userAppService.forgotPassword(request);
        return ResultVO.success();
    }

    /**
     * 重置密码
     * COMPLETED
     * @param request 重置密码请求
     * @return 重置密码结果
     */
    @PutMapping("/reset-password")
    public ResultVO<String> resetPassword(
            @Valid @RequestBody ResetPasswordReq request) {
        userAppService.resetPassword(request);
        return ResultVO.success();
    }

    /**
     * 用户登录
     * COMPLETED
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginReq request) {
        LoginVO loginResult = userAppService.login(request);
        return ResultVO.success(loginResult);
    }

    /**
     * 用户退出登录
     * COMPLETED
     * @return 退出登录响应
     */
    @PostMapping("/logout")
    public ResultVO<Void> logout() {
        userAppService.logout();
        return ResultVO.success();
    }

    /**
     * 用户账号注销
     * COMPLETED
     * @param request 注销请求
     * @return 注销结果
     */
    @DeleteMapping("/deactivate")
    public ResultVO<String> deactivateAccount(
            @Valid @RequestBody DeactivateAccountReq request) {
        userAppService.deleteUserAccount(request);
        return ResultVO.success();
    }

    /**
     * 发送邮箱更新验证码
     * COMPLETED
     * @param request 发送邮箱更新验证码请求
     * @return 发送结果
     */
    @PostMapping("/send-update-email-code")
    public ResultVO<String> sendUpdateEmailVerification(
            @Valid @RequestBody SendVerificationReq request) {
        userAppService.sendUpdateEmailVerification(request);
        return ResultVO.success();
    }

    /**
     * 修改用户邮箱
     * COMPLETED
     * @param request 修改邮箱请求
     * @return 修改结果
     */
    @PutMapping("/update-email")
    public ResultVO<String> updateEmail(
            @Valid @RequestBody ChangeEmailReq request) {
        userAppService.updateEmail(request);
        return ResultVO.success();
    }
}
