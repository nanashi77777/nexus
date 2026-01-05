package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

/**
 * Agent聊天会话类型枚举
 *
 * @author Lin037
 */
@Getter
public enum AgentChatSessionTypeEnum {

    /**
     * 普通聊天
     */
    CHAT(1, "普通聊天"),

    /**
     * 学习会话
     */
    LEARNING(2, "学习会话");

    private final Integer code;
    private final String description;

    AgentChatSessionTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 对应的枚举值
     */
    public static AgentChatSessionTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AgentChatSessionTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的会话类型码: " + code);
    }

    /**
     * 判断是否为学习会话
     *
     * @return 是否为学习会话
     */
    public boolean isLearning() {
        return this == LEARNING;
    }
}