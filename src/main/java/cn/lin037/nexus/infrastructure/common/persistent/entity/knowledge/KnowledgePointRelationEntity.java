package cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点关系实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("knowledge_point_relations")
public class KnowledgePointRelationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long kprId;

    /**
     * 学习空间ID
     */
    private Long kprLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long kprCreatedByUserId;

    /**
     * 源知识点ID
     */
    private Long kprSourcePointId;

    /**
     * 目标知识点ID
     */
    private Long kprTargetPointId;

    /**
     * 关系类型
     */
    private String kprRelationType;

    /**
     * 关系描述
     */
    private String kprDescription;

    /**
     * 创建时间
     */
    private LocalDateTime kprCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime kprUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime kprDeletedAt;

} 