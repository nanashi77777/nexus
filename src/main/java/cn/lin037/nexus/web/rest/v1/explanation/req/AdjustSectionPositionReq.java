package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 调整章节位置请求
 *
 * @author LinSanQi
 */
@Data
public class AdjustSectionPositionReq {

    /**
     * 讲解文档ID
     */
    @NotNull(message = "讲解文档ID不能为空")
    private Long documentId;

    /**
     * 调整类型：SECTION（章节位置调整）、SUBSECTION（小节位置调整）
     */
    @NotNull(message = "调整类型不能为空")
    private AdjustmentType adjustmentType;

    /**
     * 新的章节顺序（章节位置调整时使用）
     */
    private List<Long> newSectionOrder;

    /**
     * 章节ID（小节位置调整时使用）
     */
    private Long sectionId;

    /**
     * 新的小节顺序（小节位置调整时使用）
     */
    private List<Long> newSubsectionOrder;

    /**
     * 调整类型枚举
     */
    public enum AdjustmentType {
        SECTION,
        SUBSECTION
    }
}
