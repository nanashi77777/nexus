package cn.lin037.nexus.infrastructure.common.persistent.entity.explanation;

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
 * 讲解关系实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("explanation_relations")
public class ExplanationRelationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long erId;

    /**
     * 所属讲解文档ID
     */
    private Long erExplanationDocumentId;

    /**
     * 创建者用户ID
     */
    private Long erCreatedByUserId;

    /**
     * 源知识点ID
     */
    private Long erSourcePointId;

    /**
     * 目标知识点ID
     */
    private Long erTargetPointId;

    /**
     * 关系类型
     */
    private String erRelationType;

    /**
     * 关系描述
     */
    private String erDescription;

    /**
     * 边样式配置（JSONB格式）
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String erStyleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime erCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime erUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime erDeletedAt;
}