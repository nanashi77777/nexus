package cn.lin037.nexus.infrastructure.adapter.knowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI生成知识的类型枚举
 *
 * @author LinSanQi
 */
@Getter
@RequiredArgsConstructor
public enum AiGenerationTypeEnum {

    /**
     * 生成知识
     */
    GENERATE_KNOWLEDGE("GENERATE_KNOWLEDGE", "生成知识"),

    /**
     * 拓展知识
     */
    EXPAND_KNOWLEDGE("EXPAND_KNOWLEDGE", "拓展知识"),

    /**
     * 连接知识
     */
    CONNECT_KNOWLEDGE("CONNECT_KNOWLEDGE", "连接知识");

    private final String code;
    private final String description;
}
