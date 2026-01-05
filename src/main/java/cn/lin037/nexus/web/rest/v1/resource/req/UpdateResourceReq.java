package cn.lin037.nexus.web.rest.v1.resource.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新资源信息请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateResourceReq {

    @NotBlank(message = "资源标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
} 