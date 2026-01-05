package cn.lin037.nexus.application.agent.enums;

import lombok.Getter;

/**
 * 工具项状态枚举
 * 用于标识工具列表中每个工具的执行状态
 *
 * @author Lin037
 */
@Getter
public enum ToolItemStatus {
    /**
     * 未执行 - 工具尚未开始执行
     */
    NOT_EXECUTED("未执行"),

    /**
     * 执行中 - 工具正在执行
     */
    EXECUTING("执行中"),

    /**
     * 执行成功 - 工具执行完成且成功
     */
    SUCCESS("执行成功"),

    /**
     * 执行失败 - 工具执行过程中发生错误
     */
    FAILED("执行失败"),

    /**
     * 等待授权 - 工具需要用户授权才能执行
     */
    WAITING_AUTHORIZATION("等待授权"),

    /**
     * 用户拒绝 - 用户拒绝执行该工具
     */
    USER_REJECTED("用户拒绝"),

    /**
     * 跳过执行 - 用户选择跳过该工具
     */
    SKIPPED("跳过执行");

    private final String description;

    ToolItemStatus(String description) {
        this.description = description;
    }
}