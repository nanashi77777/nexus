package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

@Getter
public enum AgentChatMessageRoleEnum {
    USER("USER"),
    ASSISTANT("ASSISTANT"),
    SYSTEM("SYSTEM");
    private final String role;

    AgentChatMessageRoleEnum(String role) {
        this.role = role;
    }

    public static AgentChatMessageRoleEnum fromRole(String role) {
        for (AgentChatMessageRoleEnum value : values()) {
            if (value.role.equals(role)) {
                return value;
            }
        }
        return null;
    }
}
