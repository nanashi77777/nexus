package cn.lin037.nexus.infrastructure.common.persistent.entity;

import cn.lin037.nexus.infrastructure.common.persistent.handler.JsonbTypeHandler;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图谱边实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("graph_edges")
public class GraphEdgeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 边ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long geId;

    /**
     * 学习空间ID
     */
    private Long geLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long geCreatedByUserId;

    /**
     * 所属图谱ID
     */
    private Long geGraphId;

    /**
     * 源虚体节点ID
     */
    private Long geSourceVirtualNodeId;

    /**
     * 目标虚体节点ID
     */
    private Long geTargetVirtualNodeId;

    /**
     * 源投影节点ID
     */
    private Long geSourceProjectionNodeId;

    /**
     * 目标投影节点ID
     */
    private Long geTargetProjectionNodeId;

    /**
     * 是否为投影体
     */
    private Boolean geIsProjection;

    /**
     * 关联的知识点关系实体ID（投影体时不为null）
     */
    private Long geRelationEntityRelationId;

    /**
     * 关系类型（知识点关系实体表字段）
     */
    private String geRelationType;

    /**
     * 关系描述（知识点关系实体表字段）
     */
    private String geDescription;

    /**
     * 边样式配置
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String geStyleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime geCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime geUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime geDeletedAt;
}
