package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建图谱边请求
 *
 * @author LinSanQi
 */
@Data
public class CreateGraphEdgeReq {
    /**
     * 图谱ID
     */
    @NotNull(message = "图谱ID不能为空")
    private Long graphId;

    /**
     * 源节点ID
     */
    @NotNull(message = "源节点ID不能为空")
    private Long sourceNodeId;

    /**
     * 目标节点ID
     */
    @NotNull(message = "目标节点ID不能为空")
    private Long targetNodeId;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 边样式配置
     */
    private String styleConfig;
}

