package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * AI 模块类型枚举
 *
 * @author Lin Ant
 */
@Getter
@AllArgsConstructor
public enum AiModuleTypeEnum {
    /**
     * OpenAI 模型
     */
    OPEN_AI("openai"),

    /**
     * 阿里云通义千问模型
     */
    DASH_SCOPE("dashscope");

    private final String value;

    public static AiModuleTypeEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
} 