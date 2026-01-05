package cn.lin037.nexus.web.rest.v1.explanation.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小节视图对象
 *
 * @author LinSanQi
 */
@Data
public class ExplanationSubsectionVO {

    /**
     * 小节ID
     */
    private Long id;

    /**
     * 小节标题
     */
    private String title;

    /**
     * 小节摘要
     */
    private String summary;

    /**
     * 小节内容
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
     * 所属章节ID
     */
    private Long sectionId;

    /**
     * 小节顺序
     */
    private Integer order;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
