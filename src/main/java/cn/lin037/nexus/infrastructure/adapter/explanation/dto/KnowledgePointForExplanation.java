package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于讲解文档生成的知识点信息
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointForExplanation {
    /**
     * 知识点ID
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
     * 知识点讲解
     */
    private String explanation;

    /**
     * 公式或代码示例
     */
    private String formulaOrCode;

    /**
     * 知识点的使用示例
     */
    private String example;

    public static KnowledgePointForExplanation fromKnowledgePointVersionEntity(KnowledgePointVersionEntity entity) {
        return new KnowledgePointForExplanation(
                entity.getKpvKnowledgePointId(),
                entity.getKpvTitle(),
                entity.getKpvDefinition(),
                entity.getKpvExplanation(),
                entity.getKpvFormulaOrCode(),
                entity.getKpvExample()
        );
    }
}