package cn.lin037.nexus.infrastructure.common.persistent.entity;

import cn.lin037.nexus.infrastructure.common.persistent.entity.dto.NodeStyleConfig;
import cn.lin037.nexus.infrastructure.common.persistent.handler.NodeStyleConfigTypeHandler;
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
 * 图谱节点实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("graph_nodes")
public class GraphNodeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long gnId;

    /**
     * 学习空间ID
     */
    private Long gnLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long gnCreatedByUserId;

    /**
     * 所属图谱ID
     */
    private Long gnGraphId;

    /**
     * 是否为投影体
     */
    private Boolean gnIsProjection;

    /**
     * 关联的知识点实体ID
     */
    private Long gnEntityId;

    /**
     * 节点标题（知识点实体表字段）
     */
    private String gnTitle;

    /**
     * 节点定义（知识点实体表字段）
     */
    private String gnDefinition;

    /**
     * 节点解释（知识点实体表字段）
     */
    private String gnExplanation;

    /**
     * 节点公式或代码（知识点实体表字段）
     */
    private String gnFormulaOrCode;

    /**
     * 节点示例（知识点实体表字段）
     */
    private String gnExample;

    /**
     * 节点样式配置
     */
    @TableField(typeHandler = NodeStyleConfigTypeHandler.class)
    private NodeStyleConfig gnStyleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime gnCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime gnUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime gnDeletedAt;

}
