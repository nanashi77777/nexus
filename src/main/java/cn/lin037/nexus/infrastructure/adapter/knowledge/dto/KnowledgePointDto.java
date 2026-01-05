package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识点DTO
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointDto {
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
     * 知识点公式或代码
     */
    private String formulaOrCode;

    /**
     * 知识点示例
     */
    private String example;
}