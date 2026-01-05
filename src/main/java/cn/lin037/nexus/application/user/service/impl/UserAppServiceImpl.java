package cn.lin037.nexus.application.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;
import cn.lin037.nexus.application.user.port.UserAccountRepository;
import cn.lin037.nexus.application.user.port.UserNotificationPort;
import cn.lin037.nexus.application.user.port.UserPresenceCheckerPort;
import cn.lin037.nexus.application.user.port.UserVerificationPort;
import cn.lin037.nexus.application.user.service.UserAppService;
import cn.lin037.nexus.application.user.vo.LoginVO;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.common.util.PasswordEncryptionUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.UserStatusEnum;
import cn.lin037.nexus.web.rest.v1.user.req.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * @author LinSanQi
 */
@Service
public class UserAppServiceImpl implements UserAppService {

    private final UserPresenceCheckerPort userPresenceCheckerPort;
    private final UserAccountRepository userAccountRepository;
    private final UserVerificationPort userVerificationPort;
    private final UserNotificationPort userNotificationPort;

    public UserAppServiceImpl(UserPresenceCheckerPort userPresenceCheckerPort, UserAccountRepository userAccountRepository,
                              UserVerificationPort userVerificationPort, UserNotificationPort userNotificationPort) {
        this.userPresenceCheckerPort = userPresenceCheckerPort;
        this.userAccountRepository = userAccountRepository;
        this.userVerificationPort = userVerificationPort;
        this.userNotificationPort = userNotificationPort;
    }

    /**
     * 注册用户
     * INFO：分布式之后需要加锁改逻辑，目前已有数据库唯一键作为防线
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @Override
    public Long registerUser(RegisterUserReq request) {
        // 1. 检查用户名和邮箱是否已存在
        if (userPresenceCheckerPort.existsByUsername(request.getUsername())) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "用户名已存在");
        }
        if (userPresenceCheckerPort.existsByEmail(request.getEmail())) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "该电子邮箱已存在");
        }

        // 2. 验证邮箱验证码
        if (request.getVerificationCode() == null || request.getVerificationCode().isBlank()) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "验证码为必填项");
        }
        boolean isCodeValid = userVerificationPort.verifyCode(
                request.getEmail(),
                request.getVerificationCode(),
                VerificationTypeEnum.REGISTRATION
        );
        if (!isCodeValid) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "邮箱验证码无效");
        }
        // 验证通过后删除验证码
        userVerificationPort.deleteVerificationToken(
                request.getEmail(),
                VerificationTypeEnum.REGISTRATION
        );

        // 3. 验证邀请码（如果有）
        Long inviterId = null;
        if (request.getInviteCode() != null && !request.getInviteCode().isBlank()) {
            inviterId = userAccountRepository.findUserIdByInviteCode(request.getInviteCode())
                    .orElseThrow(() -> new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "邀请码无效"));
        }

        // 4. 创建新用户
        UserAccountEntity newUser = new UserAccountEntity();
        newUser.setUaUsername(request.getUsername());
        newUser.setUaEmail(request.getEmail());
        newUser.setUaInviteCode(userAccountRepository.generateUniqueInviteCode());
        newUser.setUaInviterId(inviterId); // 可能为 null 或有效ID
        newUser.setUaPassword(PasswordEncryptionUtil.encrypt(request.getPassword()));
        newUser.setUaStatus(UserStatusEnum.ACTIVE.getCode());

        // 5. 保存新用户（repository中已经自动将相关数据添加用户数据到布隆过滤器即UserPresenceChecker中）
        userAccountRepository.save(newUser);
        // 判断一下是否保存成功了，按理说不应该会失败，但是为了保险起见，这里再次检查
        if (newUser.getUaId() == null) {
            throw new ApplicationException(CommonResultCodeEnum.ERROR, "用户创建失败");
        }

        // 6. 发送欢迎邮件
        userNotificationPort.sendWelcomeEmail(request.getEmail(), request.getUsername());

        // 7. 返回新用户ID
        return newUser.getUaId();
    }

    /**
     * 发送注册验证码
     *
     * @param request 发送验证码请求
     */
    @Override
    public void sendRegisterVerification(SendVerificationReq request) {
        // 1. 检查邮箱是否已存在
        if (userPresenceCheckerPort.existsByEmail(request.getEmail())) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "该电子邮箱已存在");
        }

        // 2. 创建验证码
        String verificationToken = userVerificationPort.createVerificationToken(request.getEmail(), VerificationTypeEnum.REGISTRATION);

        // 3. 发送验证码
        userNotificationPort.sendEmailVerification(request.getEmail(), verificationToken, VerificationTypeEnum.REGISTRATION);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     */
    @Override
    public void changePassword(ChangePasswordReq request) {

        // 1. 获取当前用户ID
        long userId = StpUtil.getLoginIdAsLong();
        UserAccountEntity foundUser = userAccountRepository.findById(userId, List.of(
                UserAccountEntity::getUaId,
                UserAccountEntity::getUaPassword
        )).orElseThrow(
                // 但其实是不可能执行得到这里的，因为用户必须登录后才能访问此接口
                () -> new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在")
        );

        // 2. 验证旧密码
        if (!PasswordEncryptionUtil.verify(request.getOldPassword(), foundUser.getUaPassword())) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "密码格式无效");
        }

        // 3. 更新密码
        userAccountRepository.updateById(foundUser.getUaId(), userEntity -> userEntity.setUaPassword(PasswordEncryptionUtil.encrypt(request.getNewPassword())));

        // 4. 登出
        StpUtil.logout(userId);
    }

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录信息
     */
    @Override
    public LoginVO login(LoginReq request) {
        // 1. 根据用户名或邮箱查询用户（因为是允许用户名或者邮箱的，所以就没必要通过布隆过滤器来额外地发送SQL查询了）
        Optional<UserAccountEntity> userOptional = userAccountRepository.findByAccount(request.getAccount(), List.of(
                UserAccountEntity::getUaId,
                UserAccountEntity::getUaUsername,
                UserAccountEntity::getUaPassword
        ));

        // 2. 验证用户
        UserAccountEntity user = userOptional.orElseThrow(() ->
                new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在"));
        // 3. 验证密码
        if (user.getUaPassword() == null || !PasswordEncryptionUtil.verify(request.getPassword(), user.getUaPassword())) {
            throw new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在");
        }

        // 3. 登录
        StpUtil.login(user.getUaId());
        return LoginVO.builder()
                .userId(user.getUaId())
                .username(user.getUaUsername())
                .tokenName(StpUtil.getTokenName())
                .tokenValue(StpUtil.getTokenValue())
                .build();
    }

    /**
     * 忘记密码
     *
     * @param request 忘记密码请求
     */
    @Override
    public void forgotPassword(ForgotPasswordReq request) {

        // 1. 根据邮箱查询用户是否存在
        if (!userPresenceCheckerPort.existsByEmail(request.getEmail())) {
            throw new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在");
        }

        // 2. 创建验证码
        String verificationToken = userVerificationPort.createVerificationToken(request.getEmail(), VerificationTypeEnum.PASSWORD_RESET);

        // 3. 发送验证码
        userNotificationPort.sendEmailVerification(request.getEmail(), verificationToken, VerificationTypeEnum.PASSWORD_RESET);
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     */
    @Override
    public void resetPassword(ResetPasswordReq request) {
        // 1. 验证验证码
        boolean isCodeValid = userVerificationPort.verifyCode(
                request.getEmail(),
                request.getVerificationCode(),
                VerificationTypeEnum.PASSWORD_RESET
        );
        if (!isCodeValid) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "邀请码无效");
        }
        // 2. 验证通过后删除验证码
        userVerificationPort.deleteVerificationToken(
                request.getEmail(),
                VerificationTypeEnum.PASSWORD_RESET
        );
        // 3. 修改密码
        UserAccountEntity user = userAccountRepository.findByEmail(request.getEmail(), List.of(
                UserAccountEntity::getUaId
        )).orElseThrow(() -> new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在"));
        if (user.getUaId() == null) {
            throw new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在");
        }
        // 4. 密码哈希处理，并设置需要更新的字段
        userAccountRepository.updateById(user.getUaId(), updater -> updater.setUaPassword(PasswordEncryptionUtil.encrypt(request.getNewPassword())));
        // 5. 退出登录
        StpUtil.logout(user.getUaId());
    }

    @Override
    public void logout() {
        long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout(userId);
    }

    @Override
    @Transactional
    public void deleteUserAccount(DeactivateAccountReq request) {
        // 1. 验证密码
        long userId = StpUtil.getLoginIdAsLong();
        if (!PasswordEncryptionUtil.verify(
                request.getPassword(),
                userAccountRepository.findById(userId, List.of(UserAccountEntity::getUaPassword))
                        .orElseThrow(
                                // 但其实是不可能执行得到这里的，因为用户必须登录后才能访问此接口
                                () -> new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "用户不存在")
                        )
                        .getUaPassword())
        ) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "密码不匹配");
        }
        // 2. 退出登录
        StpUtil.logout(userId);
        // 3. TODO：删除用户的其他资源
        // 4. 删除用户
        userAccountRepository.deleteUser(userId);
    }

    /**
     * 发送更新邮箱验证码
     *
     * @param request 发送更新邮箱验证码请求
     */
    @Override
    public void sendUpdateEmailVerification(SendVerificationReq request) {
        // 1. 检查邮箱是否存在
        boolean isEmailExisted = userPresenceCheckerPort.existsByEmail(request.getEmail());
        if (isEmailExisted) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "该电子邮箱已存在");
        }
        // 2. 创建验证码
        String verificationToken = userVerificationPort.createVerificationToken(request.getEmail(), VerificationTypeEnum.EMAIL_CHANGE);
        // 3. 发送验证码
        userNotificationPort.sendEmailVerification(request.getEmail(), verificationToken, VerificationTypeEnum.EMAIL_CHANGE);
    }

    /**
     * 更新邮箱
     *
     * @param request 更新邮箱请求
     */
    @Override
    public void updateEmail(ChangeEmailReq request) {
        // 1. 检查新邮箱是否已被占用
        boolean isEmailExisted = userPresenceCheckerPort.existsByEmail(request.getNewEmail());
        if (isEmailExisted) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_ERROR, "该电子邮箱已存在");
        }
        // 2. 验证新邮箱的验证码是否正确
        if (!userVerificationPort.verifyCode(
                request.getNewEmail(),
                request.getVerificationCode(),
                VerificationTypeEnum.EMAIL_CHANGE
        )) {
            throw new ApplicationException(CommonResultCodeEnum.PARAM_VALIDATION_ERROR, "邀请码无效");
        }
        // 3. 删除已使用的验证码
        userVerificationPort.deleteVerificationToken(
                request.getNewEmail(),
                VerificationTypeEnum.EMAIL_CHANGE
        );
        // 4. 更新用户邮箱
        long userId = StpUtil.getLoginIdAsLong();
        userAccountRepository.updateById(userId, updater -> updater.setUaEmail(request.getNewEmail()));
    }

}
