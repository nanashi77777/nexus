package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

/**
 * 工具调用状态枚举
 *
 * @author Lin037
 */
@Getter
public enum ToolCallStatusEnum {

    /**
     * 等待授权 - 工具调用请求已发起，等待用户授权
     */
    PENDING_AUTHORIZATION(0, "等待授权"),

    /**
     * 已拒绝 - 用户拒绝了工具调用请求
     */
    REJECTED(1, "已拒绝"),

    /**
     * 已允许 - 用户允许了工具调用请求
     */
    AUTHORIZED(2, "已允许"),

    /**
     * 执行中 - 工具正在执行
     */
    EXECUTING(3, "执行中"),

    /**
     * 已完成 - 工具执行成功完成
     */
    COMPLETED(4, "已完成"),

    /**
     * 执行失败 - 工具执行过程中发生错误
     */
    ERROR(5, "执行失败"),

    /**
     * 执行超时 - 工具执行超时
     */
    TIMEOUT(6, "执行超时");

    private final Integer code;
    private final String description;

    ToolCallStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static ToolCallStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ToolCallStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的工具调用状态码: " + code);
    }

    /**
     * 根据状态码获取描述
     *
     * @param code 状态码
     * @return 对应的描述
     */
    public static String getDescByCode(Integer code) {
        for (ToolCallStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status.description;
            }
        }
        return "未知";
    }
}