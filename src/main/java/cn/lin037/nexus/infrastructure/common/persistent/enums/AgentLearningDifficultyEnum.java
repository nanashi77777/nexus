package cn.lin037.nexus.infrastructure.common.persistent.enums;

import cn.lin037.nexus.common.enums.JsonSerializableEnum;
import lombok.Getter;

/**
 * Agent学习任务难度评估枚举
 *
 * @author Lin037
 */
@Getter
public enum AgentLearningDifficultyEnum implements JsonSerializableEnum<AgentLearningDifficultyEnum> {

    /**
     * 初级
     */
    BEGINNER(1, "初级"),

    /**
     * 中级
     */
    INTERMEDIATE(2, "中级"),

    /**
     * 高级
     */
    ADVANCED(3, "高级"),

    /**
     * 专家级
     */
    EXPERT(4, "专家级");

    private final Integer code;
    private final String description;

    AgentLearningDifficultyEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据难度码获取枚举
     *
     * @param code 难度码
     * @return 对应的枚举值
     */
    public static AgentLearningDifficultyEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AgentLearningDifficultyEnum difficulty : values()) {
            if (difficulty.code.equals(code)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("未知的学习难度码: " + code);
    }

    /**
     * 判断是否为高级难度（高级或专家级）
     *
     * @return 是否为高级难度
     */
    public boolean isAdvanced() {
        return this == ADVANCED || this == EXPERT;
    }

    /**
     * 反序列化方法：根据名称返回对应枚举对象
     *
     * @param value 序列化的值
     * @return 对应的枚举对象，如果找不到则返回null
     */
    public static AgentLearningDifficultyEnum fromSerializationValue(Object value) {
        return JsonSerializableEnum.fromSerializationValue(value, AgentLearningDifficultyEnum.class);
    }

    /**
     * 获取下一个难度级别
     *
     * @return 下一个难度级别，如果已是最高级则返回当前级别
     */
    public AgentLearningDifficultyEnum getNextLevel() {
        return switch (this) {
            case BEGINNER -> INTERMEDIATE;
            case INTERMEDIATE -> ADVANCED;
            case ADVANCED, EXPERT -> EXPERT;
        };
    }

    /**
     * 获取上一个难度级别
     *
     * @return 上一个难度级别，如果已是最低级则返回当前级别
     */
    public AgentLearningDifficultyEnum getPreviousLevel() {
        return switch (this) {
            case BEGINNER, INTERMEDIATE -> BEGINNER;
            case ADVANCED -> INTERMEDIATE;
            case EXPERT -> ADVANCED;
        };
    }

    /**
     * 序列化方法：返回枚举名称
     *
     * @return 枚举名称
     */
    @Override
    public Object getSerializationValue() {
        return this.name();
    }
}