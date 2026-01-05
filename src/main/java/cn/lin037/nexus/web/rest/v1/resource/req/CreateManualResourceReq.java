package cn.lin037.nexus.web.rest.v1.resource.req;

import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手动创建资源请求
 *
 * @author LinSanQi
 */
@Data
public class CreateManualResourceReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 资源标题
     */
    @NotBlank(message = "资源标题不能为空")
    @Size(min = 1, max = 255, message = "资源标题长度必须在1到255个字符之间")
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
    private Integer sliceStrategy = SliceStrategyEnum.RECURSIVE_BY_TOKEN.getCode();

} 