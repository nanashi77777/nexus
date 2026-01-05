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
 * 讲解知识点实体
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("explanation_points")
public class ExplanationPointEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 知识点ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long epId;

    /**
     * 所属讲解文档ID
     */
    private Long epExplanationDocumentId;

    /**
     * 创建者用户ID
     */
    private Long epCreatedByUserId;

    /**
     * 知识点标题
     */
    private String epTitle;

    /**
     * 知识点定义
     */
    private String epDefinition;

    /**
     * 知识点解释
     */
    private String epExplanation;

    /**
     * 公式或代码
     */
    private String epFormulaOrCode;

    /**
     * 使用示例
     */
    private String epExample;

    /**
     * 节点样式配置（JSONB格式）
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String epStyleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime epCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime epUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime epDeletedAt;
}