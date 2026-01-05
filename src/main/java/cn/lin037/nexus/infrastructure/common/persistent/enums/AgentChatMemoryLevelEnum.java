package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

@Getter
public enum AgentChatMemoryLevelEnum {
    DISABLED(0, "不启用"),
    SESSION(1, "会话级"),
    GLOBAL(2, "全局");

    private final int code;
    private final String description;

    AgentChatMemoryLevelEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AgentChatMemoryLevelEnum fromCode(int code) {
        for (AgentChatMemoryLevelEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }
}
