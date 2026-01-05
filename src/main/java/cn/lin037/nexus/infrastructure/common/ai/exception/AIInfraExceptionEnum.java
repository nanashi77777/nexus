package cn.lin037.nexus.infrastructure.common.ai.exception;

import cn.lin037.nexus.infrastructure.common.exception.InfraExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 基础设施模块异常代码枚举
 *
 * @author lin037
 */
@Getter
@AllArgsConstructor
public enum AIInfraExceptionEnum implements InfraExceptionCode {

    UNSUPPORTED_AI_MODULE_TYPE("INFRA_AI_UNSUPPORTED_AI_MODULE_TYPE", "所选AI模块类型不支持"),
    PROVIDER_NOT_FOUND("INFRA_AI_PROVIDER_NOT_FOUND", "找不到指定的服务商配置"),
    PROVIDER_NOT_ACTIVE("INFRA_AI_PROVIDER_NOT_ACTIVE", "指定的服务商未激活"),
    MODEL_INSTANTIATION_FAILED("INFRA_AI_MODEL_INSTANTIATION_FAILED", "模型实例化失败"),
    NO_AVAILABLE_MODEL_FOUND("INFRA_AI_NO_AVAILABLE_MODEL_FOUND", "未找到可用的模型实例"),
    AI_RESPONSE_PARSE_ERROR("INFRA_AI_RESPONSE_PARSE_ERROR", "AI响应解析失败");

    private final String code;
    private final String message;

}
