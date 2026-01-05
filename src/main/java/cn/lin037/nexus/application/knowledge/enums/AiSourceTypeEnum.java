package cn.lin037.nexus.application.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI生成内容来源类型枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum AiSourceTypeEnum {
    /**
     * 来自资源
     */
    FROM_RESOURCE(1, "来自资源"),

    /**
     * 来自Prompt
     */
    FROM_PROMPT(2, "来自Prompt");

    private final Integer code;
    private final String description;
} 