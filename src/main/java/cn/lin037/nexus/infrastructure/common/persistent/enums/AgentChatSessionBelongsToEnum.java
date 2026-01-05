package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

/**
 * Agent聊天会话所属枚举
 *
 * @author Lin037
 */
@Getter
public enum AgentChatSessionBelongsToEnum {

    /**
     * 图谱
     */
    GRAPH(1, "图谱"),

    /**
     * 讲解
     */
    EXPLANATION(2, "讲解"),

    /**
     * 笔记
     */
    NOTE(3, "笔记"),

    /**
     * 学习
     */
    LEARNING(4, "学习");

    private final Integer code;
    private final String description;

    AgentChatSessionBelongsToEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据所属码获取枚举
     *
     * @param code 所属码
     * @return 对应的枚举值
     */
    public static AgentChatSessionBelongsToEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AgentChatSessionBelongsToEnum belongsTo : values()) {
            if (belongsTo.code.equals(code)) {
                return belongsTo;
            }
        }
        throw new IllegalArgumentException("未知的会话所属码: " + code);
    }
}