package cn.lin037.nexus.web.rest.v1.resource.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 从链接创建资源请求
 *
 * @author LinSanQi
 */
@Data
public class CreateLinkResourceReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 资源链接
     */
    @NotBlank(message = "资源链接不能为空")
    @URL(message = "必须是有效的URL")
    private String url;

    /**
     * 资源标题
     */
    @Size(max = 255, message = "资源标题长度不能超过255个字符")
    private String title;

    /**
     * 资源描述
     */
    @Size(max = 500, message = "资源描述长度不能超过500个字符")
    private String description;

    /**
     * 分片策略
     */
    @NotNull(message = "分片策略不能为空")
    private Integer sliceStrategy;
} 