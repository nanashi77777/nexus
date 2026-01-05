package cn.lin037.nexus.infrastructure.common.notification.service;

import cn.lin037.nexus.infrastructure.common.notification.enums.NotificationType;

import java.util.Map;

/**
 * 通知服务接口
 *
 * @author LinSanQi
 */
public interface NotificationService {

    /**
     * 发送简单文本通知
     *
     * @param to      接收者地址(例如，邮箱地址)
     * @param subject 标题
     * @param content 内容
     */
    void send(String to, String subject, String content);

    /**
     * 使用模板发送通知
     *
     * @param to         接收者地址
     * @param subject    标题
     * @param templateId 模板ID
     * @param variables  模板变量
     */
    void send(String to, String subject, String templateId, Map<String, Object> variables);

    /**
     * 获取此服务支持的通知类型
     *
     * @return 通知类型
     */
    NotificationType getType();
} 