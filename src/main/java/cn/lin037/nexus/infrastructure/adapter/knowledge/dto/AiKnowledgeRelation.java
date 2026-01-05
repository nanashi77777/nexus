package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成的知识点关系模型
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiKnowledgeRelation {

    /**
     * 源知识点ID
     */
    private Long sourceKnowledgeId;

    /**
     * 目标知识点ID
     */
    private Long targetKnowledgeId;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系描述
     */
    private String relationDescription;

    public static AiKnowledgeRelation fromKnowledgePointRelationEntity(KnowledgePointRelationEntity entity) {
        AiKnowledgeRelation generate = new AiKnowledgeRelation();
        generate.setSourceKnowledgeId(entity.getKprSourcePointId());
        generate.setTargetKnowledgeId(entity.getKprTargetPointId());
        generate.setRelationType(entity.getKprRelationType());
        generate.setRelationDescription(entity.getKprDescription());
        return generate;
    }

    public static AiKnowledgeRelation fromGraphEdgeEntity(GraphEdgeEntity entity) {
        AiKnowledgeRelation generate = new AiKnowledgeRelation();
        generate.setSourceKnowledgeId(entity.getGeSourceVirtualNodeId());
        generate.setTargetKnowledgeId(entity.getGeTargetVirtualNodeId());
        generate.setRelationType(entity.getGeRelationType());
        generate.setRelationDescription(entity.getGeDescription());
        return generate;
    }
}
