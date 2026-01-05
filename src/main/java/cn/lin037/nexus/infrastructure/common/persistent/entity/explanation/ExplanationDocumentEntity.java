package cn.lin037.nexus.infrastructure.common.persistent.entity.explanation;

import cn.lin037.nexus.infrastructure.common.persistent.handler.JsonbTypeHandler;
import cn.lin037.nexus.infrastructure.common.persistent.handler.LongListTypeHandler;
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
import java.util.List;

/**
 * 讲解文档实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("explanation_documents")
public class ExplanationDocumentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 讲解文档ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long edId;

    /**
     * 学习空间ID
     */
    private Long edLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long edCreatedByUserId;

    /**
     * 讲解文档标题
     */
    private String edTitle;

    /**
     * 讲解文档描述
     */
    private String edDescription;

    /**
     * 文档状态 (0-草稿、1-AI生成中、2-正常)
     *
     * @see cn.lin037.nexus.infrastructure.common.persistent.enums.ExplanationDocumentStatusEnum
     */
    private Integer edStatus;

    /**
     * 章节位置顺序（有序数组，存储章节ID）
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> edSectionOrder;

    /**
     * 图谱数据配置（JSONB格式）
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String edGraphConfig;

    /**
     * 创建时间
     */
    private LocalDateTime edCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime edUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime edDeletedAt;
}