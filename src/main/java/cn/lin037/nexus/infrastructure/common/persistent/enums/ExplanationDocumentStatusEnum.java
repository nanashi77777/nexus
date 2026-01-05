package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 讲解文档状态枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum ExplanationDocumentStatusEnum {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    /**
     * AI生成中
     */
    AI_GENERATING(1, "AI生成中"),
    /**
     * AI生成失败
     */
    AI_GENERATE_FAILED(2, "AI生成失败"),
    /**
     * 正常
     */
    NORMAL(3, "正常");

    private final int code;
    private final String description;
}