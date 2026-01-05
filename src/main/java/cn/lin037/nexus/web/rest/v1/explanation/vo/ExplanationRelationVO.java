package cn.lin037.nexus.web.rest.v1.explanation.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 讲解关系视图对象
 *
 * @author LinSanQi
 */
@Data
public class ExplanationRelationVO {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 所属讲解文档ID
     */
    private Long explanationDocumentId;

    /**
     * 创建者用户ID
     */
    private Long createdByUserId;

    /**
     * 源知识点ID
     */
    private Long sourcePointId;

    /**
     * 目标知识点ID
     */
    private Long targetPointId;

    /**
     * 关系类型
     */
    private String relationType;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 边样式配置
     */
    private Object styleConfig;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
