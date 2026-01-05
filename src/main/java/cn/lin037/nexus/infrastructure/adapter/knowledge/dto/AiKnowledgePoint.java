package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的知识点模型
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgePoint {
    /**
     * 知识点ID
     */
    private Long id;
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

    public static AiKnowledgePoint fromKnowledgePointVersionEntity(KnowledgePointVersionEntity entity) {
        return new AiKnowledgePoint(
                entity.getKpvKnowledgePointId(),
                entity.getKpvTitle(),
                entity.getKpvDefinition(),
                entity.getKpvExplanation(),
                entity.getKpvFormulaOrCode(),
                entity.getKpvExample()
        );
    }

    public static AiKnowledgePoint fromGraphNodeEntity(GraphNodeEntity entity) {
        return new AiKnowledgePoint(
                entity.getGnEntityId(),
                entity.getGnTitle(),
                entity.getGnDefinition(),
                entity.getGnExplanation(),
                entity.getGnFormulaOrCode(),
                entity.getGnExample()
        );
    }
}
