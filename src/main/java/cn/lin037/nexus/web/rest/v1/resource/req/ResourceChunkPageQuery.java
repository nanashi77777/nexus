package cn.lin037.nexus.web.rest.v1.resource.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分片分页查询参数
 *
 * @author Gemini
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "资源分片分页查询参数")
public class ResourceChunkPageQuery extends BasePageQuery {

    @Schema(description = "所属学习空间ID，可为空")
    private String learningSpaceId;

    @Schema(description = "所属资源ID，可为空")
    private String resourceId;

    @Schema(description = "分片状态，可为空")
    private Boolean status;
}