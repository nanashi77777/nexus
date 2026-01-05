package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import lombok.Data;

/**
 * 图谱边视图对象
 *
 * @author LinSanQi
 */
@Data
public class GraphEdgeVO {
    private Long id;
    private Long graphId;
    private Long sourceVirtualNodeId;
    private Long targetVirtualNodeId;
    private Long sourceProjectionNodeId;
    private Long targetProjectionNodeId;
    private Boolean isProjection;
    private Long relationEntityRelationId;
    private String relationType;
    private String description;
    private String styleConfig;

    public static GraphEdgeVO fromEntity(GraphEdgeEntity entity) {
        GraphEdgeVO vo = new GraphEdgeVO();
        vo.setId(entity.getGeId());
        vo.setGraphId(entity.getGeGraphId());
        vo.setSourceVirtualNodeId(entity.getGeSourceVirtualNodeId());
        vo.setTargetVirtualNodeId(entity.getGeTargetVirtualNodeId());
        vo.setSourceProjectionNodeId(entity.getGeSourceProjectionNodeId());
        vo.setTargetProjectionNodeId(entity.getGeTargetProjectionNodeId());
        vo.setIsProjection(entity.getGeIsProjection());
        vo.setRelationEntityRelationId(entity.getGeRelationEntityRelationId());
        vo.setRelationType(entity.getGeRelationType());
        vo.setDescription(entity.getGeDescription());
        vo.setStyleConfig(entity.getGeStyleConfig());
        return vo;
    }
}
