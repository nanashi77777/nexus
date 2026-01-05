package cn.lin037.nexus.infrastructure.common.notification;

import cn.lin037.nexus.infrastructure.common.notification.service.NotificationService;
import cn.lin037.nexus.infrastructure.common.notification.template.TemplateService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 邮件通知服务测试类
 * <p>
 * 该测试类主要验证 EmailNotificationService 的邮件发送功能。
 * 使用 Mockito 模拟 JavaMailSender，避免真实邮件发送。
 */
@Slf4j
@SpringBootTest
// 为测试环境提供必要的属性，解决“发件人邮箱未配置”的错误
@TestPropertySource(properties = "nexus.notification.email.from=test-sender@example.com")
class EmailNotificationServiceTest {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private NotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        // 当需要创建 MimeMessage 时，返回一个空的 MimeMessage 实例
        // EmailNotificationService 内部会用 MimeMessageHelper 包装它
        MimeMessage mimeMessage = new MimeMessage((jakarta.mail.Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    /**
     * 测试邮件发送功能
     * <p>
     * 验证邮件主题、收件人和正文内容是否正确
     */
    @Test
    void testSendEmail() throws Exception {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String templateName = "welcome";
        Map<String, Object> templateModel = Map.of("username", "TestUser");

        // When
        emailNotificationService.send(to, subject, templateName, templateModel);

        // Then
        // 捕获最终要发送的 MimeMessage
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();

        // 验证邮件元数据（收件人、发件人、主题）
        assertThat(sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString()).isEqualTo(to);
        assertThat(sentMessage.getFrom()[0].toString()).isEqualTo("test-sender@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo(subject);
    }

    /**
     * 使用 @TestConfiguration 提供模拟 Bean，替代已弃用的 @MockBean
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        public TemplateService templateService() {
            // 模拟 TemplateService，返回固定的HTML内容
            TemplateService mockTemplateService = mock(TemplateService.class);
            when(mockTemplateService.renderTemplate(anyString(), any()))
                    .thenReturn("<html><body>Welcome, TestUser!</body></html>");
            return mockTemplateService;
        }
    }

}