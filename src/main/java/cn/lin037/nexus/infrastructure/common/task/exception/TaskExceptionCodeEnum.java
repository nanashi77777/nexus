package cn.lin037.nexus.infrastructure.common.task.exception;

import cn.lin037.nexus.infrastructure.common.exception.InfraExceptionCode;
import lombok.Getter;

/**
 * 任务模块异常码枚举
 *
 * @author LinSanQi
 */
@Getter
public enum TaskExceptionCodeEnum implements InfraExceptionCode {

    INFRA_TASK_NOT_FOUND("INFRA_TASK_NOT_FOUND", "任务不存在"),
    TASK_INTERRUPTED("TASK_INTERRUPTED", "任务被中断"),
    INFRA_TASK_CREATE_OR_UPDATE_FAILED("INFRA_TASK_CREATE_OR_UPDATE_FAILED", "任务创建或修改失败"),
    INFRA_TASK_EXECUTOR_NOT_FOUND("INFRA_TASK_EXECUTOR_NOT_FOUND", "找不到对应的任务执行器"),
    INFRA_TASK_ALREADY_RUNNING("INFRA_TASK_ALREADY_RUNNING", "任务已在运行中，无法重复启动"),
    INFRA_TASK_CANNOT_BE_CANCELLED("INFRA_TASK_CANNOT_BE_CANCELLED", "任务状态不正确，无法取消"),
    INFRA_TASK_SERIALIZATION_ERROR("INFRA_TASK_SERIALIZATION_ERROR", "任务参数或结果序列化/反序列化失败"),
    TASK_CANCELLED("TASK_CANCELLED", "任务被取消"),
    INFRA_TASK_CONFIG_ERROR("INFRA_TASK_CONFIG_ERROR", "任务模块配置错误"),
    INFRA_TASK_UNKNOWN_STATUS("INFRA_TASK_UNKNOWN_STATUS", "未知的任务状态"),
    INFRA_TASK_UNEXPECTED_ERROR("INFRA_TASK_UNEXPECTED_ERROR", "任务执行期间发生未知错误，请联系管理员");

    private final String code;
    private final String message;

    TaskExceptionCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
} 