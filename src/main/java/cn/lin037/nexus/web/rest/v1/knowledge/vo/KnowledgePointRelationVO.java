package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点关系视图对象
 *
 * @author LinSanQi
 */
@Data
@Builder
public class KnowledgePointRelationVO {
    private Long id;
    private Long sourcePointId;
    private Long targetPointId;
    private String relationType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgePointRelationVO fromEntity(KnowledgePointRelationEntity entity) {
        return KnowledgePointRelationVO.builder()
                .id(entity.getKprId())
                .sourcePointId(entity.getKprSourcePointId())
                .targetPointId(entity.getKprTargetPointId())
                .relationType(entity.getKprRelationType())
                .description(entity.getKprDescription())
                .createdAt(entity.getKprCreatedAt())
                .updatedAt(entity.getKprUpdatedAt())
                .build();
    }
}