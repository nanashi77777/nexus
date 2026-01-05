package cn.lin037.nexus.web.rest.v1.explanation.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讲解文档视图对象
 *
 * @author LinSanQi
 */
@Data
public class ExplanationDocumentVO {

    /**
     * 讲解文档ID
     */
    private Long id;

    /**
     * 学习空间ID
     */
    private Long learningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long createdByUserId;

    /**
     * 讲解文档标题
     */
    private String title;

    /**
     * 讲解文档描述
     */
    private String description;

    /**
     * 文档状态
     */
    private Integer status;

    /**
     * 章节位置顺序
     */
    private List<Long> sectionOrder;

    /**
     * 图谱数据配置
     */
    private Object graphConfig;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
