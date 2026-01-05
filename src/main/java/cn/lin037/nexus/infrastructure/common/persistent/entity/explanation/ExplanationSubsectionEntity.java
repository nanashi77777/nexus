package cn.lin037.nexus.infrastructure.common.persistent.entity.explanation;

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
 * 讲解小节实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("explanation_subsections")
public class ExplanationSubsectionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 小节ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long essId;

    /**
     * 小节标题
     */
    private String essTitle;

    /**
     * 小节摘要
     */
    private String essSummary;

    /**
     * 小节内容
     */
    private String essContent;

    /**
     * 创建者用户ID
     */
    private Long essCreatedByUserId;

    /**
     * 所属讲解文档ID
     */
    private Long essExplanationDocumentId;

    /**
     * 所属章节ID
     */
    private Long essSectionId;

    /**
     * 小节顺序
     */
    private Integer essOrder;

    /**
     * 创建时间
     */
    private LocalDateTime essCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime essUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime essDeletedAt;
}