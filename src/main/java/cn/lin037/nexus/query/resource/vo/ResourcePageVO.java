package cn.lin037.nexus.query.resource.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源分页查询视图对象
 *
 * @author Gemini
 */
@Data
@ResultEntity(ResourceEntity.class)
@Schema(description = "资源分页查询视图对象")
public class ResourcePageVO {

    @Schema(description = "资源ID")
    @ResultEntityField(property = ResourceEntity.Fields.rsId)
    private String id;

    @Schema(description = "资源标题")
    @ResultEntityField(property = ResourceEntity.Fields.rsTitle)
    private String title;

    @Schema(description = "资源描述")
    @ResultEntityField(property = ResourceEntity.Fields.rsDescription)
    private String description;

    @Schema(description = "来源类型")
    @ResultEntityField(property = ResourceEntity.Fields.rsSourceType)
    private Integer sourceType;

    @Schema(description = "资源状态")
    @ResultEntityField(property = ResourceEntity.Fields.rsStatus)
    private Integer status;

    @Schema(description = "创建时间")
    @ResultEntityField(property = ResourceEntity.Fields.rsCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
