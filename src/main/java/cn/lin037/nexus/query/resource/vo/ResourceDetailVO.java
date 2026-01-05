package cn.lin037.nexus.query.resource.vo;

import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceStatusEnum;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.xbatis.db.annotations.PutEnumValue;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源详情视图对象
 *
 * @author Gemini
 */
@Data
@ResultEntity(ResourceEntity.class)
@Schema(description = "资源详情视图对象")
public class ResourceDetailVO {

    @Schema(description = "资源ID")
    @ResultEntityField(property = ResourceEntity.Fields.rsId)
    private String id;

    @Schema(description = "所属学习空间ID")
    @ResultEntityField(property = ResourceEntity.Fields.rsLearningSpaceId)
    private Long learningSpaceId;

    @Schema(description = "资源标题")
    @ResultEntityField(property = ResourceEntity.Fields.rsTitle)
    private String title;

    @Schema(description = "资源描述")
    @ResultEntityField(property = ResourceEntity.Fields.rsDescription)
    private String description;

    @Schema(description = "来源类型")
    @ResultEntityField(property = ResourceEntity.Fields.rsSourceType)
    private ResourceSourceTypeEnum sourceType;

    @Schema(description = "来源URI")
    @ResultEntityField(property = ResourceEntity.Fields.rsSourceUri)
    private String sourceUri;

    @Schema(description = "提示语")
    @ResultEntityField(property = ResourceEntity.Fields.rsPrompt)
    private String prompt;

    @Schema(description = "资源状态")
    @PutEnumValue(property = ResourceEntity.Fields.rsStatus, target = ResourceStatusEnum.class, value = "description")
    private ResourceStatusEnum status;

    @Schema(description = "解析失败时的错误信息")
    @ResultEntityField(property = ResourceEntity.Fields.rsParseErrorMessage)
    private String parseErrorMessage;

    @Schema(description = "创建时间")
    @ResultEntityField(property = ResourceEntity.Fields.rsCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "最后修改时间")
    @ResultEntityField(property = ResourceEntity.Fields.rsUpdatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}