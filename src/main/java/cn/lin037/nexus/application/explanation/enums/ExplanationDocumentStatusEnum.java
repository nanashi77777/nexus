package cn.lin037.nexus.application.explanation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 讲解文档状态枚举
 *
 * @author LinSanQi
 */
@Getter
@RequiredArgsConstructor
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
     * AI生成完成
     */
    AI_GENERATION_COMPLETED(2, "AI生成完成"),

    /**
     * AI生成失败
     */
    AI_GENERATION_FAILED(3, "AI生成失败"),

    /**
     * 已发布
     */
    PUBLISHED(4, "已发布"),

    /**
     * 已归档
     */
    ARCHIVED(5, "已归档");

    private final Integer code;
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值
     */
    public static ExplanationDocumentStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ExplanationDocumentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}