package cn.lin037.nexus.application.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI生成内容状态枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum AiGeneratedKnowledgeStatusEnum {
    /**
     * 已采纳
     */
    ADOPTED(1, "已采纳"),

    /**
     * 已丢弃
     */
    DISCARDED(2, "已丢弃");

    private final Integer code;
    private final String description;
} 