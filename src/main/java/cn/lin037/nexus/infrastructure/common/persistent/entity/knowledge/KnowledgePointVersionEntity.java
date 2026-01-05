package cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识点版本实体
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("knowledge_point_versions")
public class KnowledgePointVersionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 版本ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long kpvId;

    /**
     * 关联知识点ID
     */
    private Long kpvKnowledgePointId;

    /**
     * 创建者用户ID
     */
    private Long kpvCreatedByUserId;

    /**
     * 创建时间
     */
    private LocalDateTime kpvCreatedAt;

    /**
     * 删除时间
     */
    private LocalDateTime kpvDeletedAt;

    /**
     * 知识点标题
     */
    private String kpvTitle;

    /**
     * 知识点定义
     */
    private String kpvDefinition;

    /**
     * 知识点讲解
     */
    private String kpvExplanation;

    /**
     * 公式或代码示例
     */
    private String kpvFormulaOrCode;

    /**
     * 示例
     */
    private String kpvExample;

    /**
     * 难度系数
     */
    private BigDecimal kpvDifficulty;

}