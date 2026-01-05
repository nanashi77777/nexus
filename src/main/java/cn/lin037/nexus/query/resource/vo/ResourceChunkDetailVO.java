package cn.lin037.nexus.query.resource.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源分片分页查询视图对象
 *
 * @author Gemini
 */
@Data
@ResultEntity(ResourceChunkEntity.class)
@Schema(description = "资源分片详情查询视图对象")
public class ResourceChunkDetailVO {

    @Schema(description = "分片ID")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcId)
    private String id;

    @Schema(description = "分片的文本内容")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcContent)
    private String content;

    @Schema(description = "所在的页码索引")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcPageIndex)
    private Integer pageIndex;

    @Schema(description = "在页内的分片顺序索引")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcChunkIndex)
    private Integer chunkIndex;

    @Schema(description = "预估的Token数量")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcTokenCount)
    private Integer tokenCount;

    @Schema(description = "关键词列表")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcKeywords)
    private List<String> keywords;

    @Schema(description = "是否已向量化")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcIsVectorized)
    private Boolean isVectorized;

    @Schema(description = "创建时间")
    @ResultEntityField(property = ResourceChunkEntity.Fields.rcCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}