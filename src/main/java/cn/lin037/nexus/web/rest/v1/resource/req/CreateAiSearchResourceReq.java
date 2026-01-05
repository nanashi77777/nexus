package cn.lin037.nexus.web.rest.v1.resource.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI搜索创建资源请求
 *
 * @author LinSanQi
 */
@Data
public class CreateAiSearchResourceReq {

    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    @NotBlank(message = "AI生成的要求不能为空")
    private String requirementPrompt;

    @NotBlank(message = "资源标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}