package cn.lin037.nexus.web.rest.v1.resource.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建资源分片请求
 *
 * @author LinSanQi
 */
@Data
public class CreateChunkReq {

    @NotBlank(message = "分片内容不能为空")
    private String content;

    @NotNull(message = "页码索引不能为空")
    private Integer pageIndex;

    @NotNull(message = "分片顺序索引不能为空")
    private Integer chunkIndex;
} 