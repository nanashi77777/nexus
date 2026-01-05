package cn.lin037.nexus.infrastructure.common.persistent.entity.explanation;

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
 * 讲解章节实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("explanation_sections")
public class ExplanationSectionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 章节ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long esId;

    /**
     * 章节标题
     */
    private String esTitle;

    /**
     * 章节摘要
     */
    private String esSummary;

    /**
     * 章节内容
     */
    private String esContent;

    /**
     * 创建者用户ID
     */
    private Long esCreatedByUserId;

    /**
     * 所属讲解文档ID
     */
    private Long esExplanationDocumentId;

    /**
     * 小节位置顺序（有序数组，存储小节ID）
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> esSubsectionOrder;

    /**
     * 创建时间
     */
    private LocalDateTime esCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime esUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime esDeletedAt;
}