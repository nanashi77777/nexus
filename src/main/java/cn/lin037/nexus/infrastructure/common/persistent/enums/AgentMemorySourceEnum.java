package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

@Getter
public enum AgentMemorySourceEnum {

    /**
     * 记忆来源
     */
    CHAT("chat", "聊天"),
    LEARNING("learning", "学习"),
    MANUAL("manual", "手动添加");

    private final String code;
    private final String description;

    AgentMemorySourceEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
