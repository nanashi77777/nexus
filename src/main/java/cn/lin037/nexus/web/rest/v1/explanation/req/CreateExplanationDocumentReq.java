package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建讲解文档请求
 *
 * @author LinSanQi
 */
@Data
public class CreateExplanationDocumentReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 讲解文档标题
     */
    @NotBlank(message = "讲解文档标题不能为空")
    @Size(max = 200, message = "讲解文档标题长度不能超过200个字符")
    private String title;

    /**
     * 讲解文档描述
     */
    @Size(max = 1000, message = "讲解文档描述长度不能超过1000个字符")
    private String description;
}
