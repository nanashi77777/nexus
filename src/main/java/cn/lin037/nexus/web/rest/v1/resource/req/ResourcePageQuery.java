package cn.lin037.nexus.web.rest.v1.resource.req;

import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceStatusEnum;
import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源分页查询参数
 *
 * @author Gemini
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "资源分页查询参数")
public class ResourcePageQuery extends BasePageQuery {

    @Schema(description = "所属学习空间ID，可为空")
    private String learningSpaceId;

    @Schema(description = "资源状态，可为空")
    private ResourceStatusEnum status;

    @Schema(description = "资源来源类型，可为空")
    private ResourceSourceTypeEnum sourceType;
}
