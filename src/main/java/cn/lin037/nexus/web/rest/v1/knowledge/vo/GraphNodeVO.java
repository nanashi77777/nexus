package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.dto.NodeStyleConfig;
import lombok.Data;

/**
 * 图谱节点视图对象
 *
 * @author LinSanQi
 */
@Data
public class GraphNodeVO {
    private Long id;
    private Long graphId;
    private Boolean isProjection;
    private Long entityId;
    private String title;
    private String definition;
    private String explanation;
    private String formulaOrCode;
    private String example;
    private NodeStyleConfig styleConfig;

    public static GraphNodeVO fromEntity(GraphNodeEntity entity) {
        GraphNodeVO vo = new GraphNodeVO();
        vo.setId(entity.getGnId());
        vo.setGraphId(entity.getGnGraphId());
        vo.setIsProjection(entity.getGnIsProjection());
        vo.setEntityId(entity.getGnEntityId());
        vo.setTitle(entity.getGnTitle());
        vo.setDefinition(entity.getGnDefinition());
        vo.setExplanation(entity.getGnExplanation());
        vo.setFormulaOrCode(entity.getGnFormulaOrCode());
        vo.setExample(entity.getGnExample());
        vo.setStyleConfig(entity.getGnStyleConfig());
        return vo;
    }
}
