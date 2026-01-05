package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新章节请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateExplanationSectionReq {

    /**
     * 章节标题
     */
    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "章节标题长度不能超过200个字符")
    private String title;

    /**
     * 章节摘要
     */
    @Size(max = 500, message = "章节摘要长度不能超过500个字符")
    private String summary;

    /**
     * 章节内容
     */
    private String content;
}
