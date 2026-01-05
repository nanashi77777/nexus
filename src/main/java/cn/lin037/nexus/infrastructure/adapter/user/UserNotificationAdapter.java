package cn.lin037.nexus.infrastructure.adapter.user;

import cn.lin037.nexus.application.user.enums.VerificationTypeEnum;
import cn.lin037.nexus.application.user.port.UserNotificationPort;
import cn.lin037.nexus.infrastructure.common.notification.enums.NotificationType;
import cn.lin037.nexus.infrastructure.common.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Lin037
 **/
@Service
public class UserNotificationAdapter implements UserNotificationPort {

    private final NotificationService emailNotificationService;

    public UserNotificationAdapter(List<NotificationService> notificationServices) {
        this.emailNotificationService = notificationServices.stream()
                .filter(s -> s.getType() == NotificationType.EMAIL)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("EmailNotificationService not found"));
    }

    @Override
    public void sendEmailVerification(String email, String code, VerificationTypeEnum verificationType) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", email);
        variables.put("purpose", verificationType.getDescription());
        variables.put("verificationCode", code);
        variables.put("expiry", verificationType.getExpiryMinutes());

        emailNotificationService.send(email, "【Nexus】邮箱验证码", "verification", variables);
    }

    @Override
    public void sendWelcomeEmail(String email, String username) {
        Map<String, Object> variables = Map.of("username", Optional.ofNullable(username).orElse(email));
        emailNotificationService.send(email, "【Nexus】欢迎加入我们", "welcome", variables);
    }
}
