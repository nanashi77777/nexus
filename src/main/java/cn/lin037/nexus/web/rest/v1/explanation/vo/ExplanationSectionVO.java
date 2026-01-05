package cn.lin037.nexus.web.rest.v1.explanation.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 章节视图对象
 *
 * @author LinSanQi
 */
@Data
public class ExplanationSectionVO {

    /**
     * 章节ID
     */
    private Long id;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节摘要
     */
    private String summary;

    /**
     * 章节内容
     */
    private String content;

    /**
     * 创建者用户ID
     */
    private Long createdByUserId;

    /**
     * 所属讲解文档ID
     */
    private Long explanationDocumentId;

    /**
     * 小节位置顺序
     */
    private List<Long> subsectionOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
