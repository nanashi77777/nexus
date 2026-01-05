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
 * 知识点实体
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("knowledge_points")
public class KnowledgePointEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识点ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long kpId;

    /**
     * 学习空间ID
     */
    private Long kpLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long kpCreatedByUserId;

    /**
     * 文件夹ID
     */
    private Long kpFolderId;

    /**
     * 当前版本ID
     */
    private Long kpCurrentVersionId;

    /**
     * 创建时间
     */
    private LocalDateTime kpCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime kpUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime kpDeletedAt;

} 