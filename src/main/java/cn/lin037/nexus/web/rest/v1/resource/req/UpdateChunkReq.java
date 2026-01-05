package cn.lin037.nexus.web.rest.v1.resource.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新资源分片请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateChunkReq {

    /**
     * 分片内容
     */
    @NotBlank(message = "分片内容不能为空")
    private String content;
} 