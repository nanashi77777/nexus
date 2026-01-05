package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于讲解文档生成的知识点关系信息
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelationForExplanation {
    /**
     * 关系ID
     */
    private Long relationId;

    /**
     * 源知识点ID
     */
    private Long sourcePointId;

    /**
     * 目标知识点ID
     */
    private Long targetPointId;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系描述
     */
    private String description;

    public static KnowledgeRelationForExplanation fromKnowledgePointRelationEntity(KnowledgePointRelationEntity entity) {
        return new KnowledgeRelationForExplanation(
                entity.getKprId(),
                entity.getKprSourcePointId(),
                entity.getKprTargetPointId(),
                entity.getKprRelationType(),
                entity.getKprDescription()
        );
    }
}