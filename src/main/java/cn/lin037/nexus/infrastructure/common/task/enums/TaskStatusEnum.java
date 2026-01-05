package cn.lin037.nexus.infrastructure.common.task.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 异步任务的状态枚举
 *
 * @author LinSanQi
 */
@Getter
public enum TaskStatusEnum {
    /**
     * 等待中
     * 任务已提交，正在等待调度器分配资源执行。
     */
    WAITING(0, "等待中"),

    /**
     * 运行中
     * 任务已由调度器接管，正在虚拟线程中执行。
     */
    RUNNING(1, "运行中"),

    /**
     * 已完成
     * 任务成功执行完毕。
     */
    COMPLETED(2, "已完成"),

    /**
     * 已取消
     * 任务在开始执行前被用户取消，或在执行期间被请求取消并成功中止。
     */
    CANCELLED(3, "已取消"),

    /**
     * 已失败
     * 任务在执行过程中遇到异常，未能完成。
     */
    FAILED(4, "已失败");

    private final Integer code;
    private final String description;

    TaskStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TaskStatusEnum fromCode(Integer code) {
        for (TaskStatusEnum value : values()) {
            if (Objects.equals(value.code, code)) {
                return value;
            }
        }
        return null;
    }
} 