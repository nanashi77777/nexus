package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 讲解知识点DTO
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationPointDto {
    /**
     * 知识点ID（临时ID，用于内部关联）
     */
    private Long pointId;

    /**
     * 知识点标题
     */
    private String title;

    /**
     * 知识点定义
     */
    private String definition;

    /**
     * 知识点解释
     */
    private String explanation;

    /**
     * 公式或代码
     */
    private String formulaOrCode;

    /**
     * 示例
     */
    private String example;
}