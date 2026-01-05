package cn.lin037.nexus.infrastructure.common.notification.exception;

import cn.lin037.nexus.infrastructure.common.exception.InfraExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知服务相关的异常码枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum NotificationExceptionCodeEnum implements InfraExceptionCode {

    RATE_LIMIT_EXCEEDED("INFRA_NOTIFICATION_RATE_LIMIT_EXCEEDED", "通知发送频率超出限制"),
    WAIT_QUEUE_FULL("INFRA_NOTIFICATION_WAIT_QUEUE_FULL", "通知发送等待队列已满"),
    TEMPLATE_NOT_FOUND("INFRA_NOTIFICATION_TEMPLATE_NOT_FOUND", "通知模板未找到"),
    TEMPLATE_RENDERING_ERROR("INFRA_NOTIFICATION_TEMPLATE_RENDERING_ERROR", "通知模板渲染失败"),
    SENDING_ERROR("INFRA_NOTIFICATION_SENDING_ERROR", "通知发送失败"),
    INVALID_CONFIGURATION("INFRA_NOTIFICATION_INVALID_CONFIGURATION", "通知服务配置无效");

    private final String code;
    private final String message;
} 