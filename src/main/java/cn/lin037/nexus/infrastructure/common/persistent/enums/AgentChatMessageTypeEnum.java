package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

@Getter
public enum AgentChatMessageTypeEnum {
    /**
     * 普通消息
     */
    NORMAL(0, "普通消息"),
    /**
     * 工具请求
     */
    TOOL_REQUEST(1, "工具请求"),
    /**
     * 工具响应
     */
    TOOL_RESPONSE(2, "工具响应"),
    /**
     * 思考内容
     */
    THINKING_CONTENT(3, "思考内容"),
    /**
     * 工具列表
     */
    TOOL_LIST(4, "工具列表");

    private final Integer code;
    private final String description;

    AgentChatMessageTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

}
