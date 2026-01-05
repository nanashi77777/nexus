package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

/**
 * Agent聊天会话状态枚举
 *
 * @author Lin037
 */
@Getter
public enum AgentChatSessionStatusEnum {

    /**
     * 正常状态 - 会话可以接收新的消息
     */
    NORMAL(1, "正常可以对话"),

    /**
     * 响应中状态 - AI正在处理和响应用户消息
     */
    RESPONDING(2, "响应中"),

    /**
     * 取消状态 - 会话已被取消
     */
    CANCELLED(3, "取消"),

    /**
     * 工具调用中状态 - 正在调用工具
     */
    TOOL_CALLING(4, "工具调用中"),

    /**
     * 等待工具授权状态 - 等待用户对工具调用进行授权
     */
    WAITING_TOOL_AUTHORIZATION(5, "等待工具授权"),

    /**
     * 错误状态 - 会话处理过程中发生错误
     */
    ERROR(6, "错误");

    private final Integer code;
    private final String description;

    AgentChatSessionStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static AgentChatSessionStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AgentChatSessionStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

}