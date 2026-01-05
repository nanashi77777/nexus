package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新小节请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateExplanationSubsectionReq {

    /**
     * 小节标题
     */
    @NotBlank(message = "小节标题不能为空")
    @Size(max = 200, message = "小节标题长度不能超过200个字符")
    private String title;

    /**
     * 小节摘要
     */
    @Size(max = 500, message = "小节摘要长度不能超过500个字符")
    private String summary;

    /**
     * 小节内容
     */
    private String content;
}
