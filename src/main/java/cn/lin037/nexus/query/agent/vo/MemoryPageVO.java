package cn.lin037.nexus.query.agent.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent记忆分页查询视图对象
 *
 * @author Lin037
 */
@Data
@ResultEntity(AgentMemoryEntity.class)
@Schema(description = "Agent记忆分页查询视图对象")
public class MemoryPageVO {

    @Schema(description = "记忆ID")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amId)
    private String id;

    @Schema(description = "学习空间ID")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amLearningSpaceId)
    private String learningSpaceId;

    @Schema(description = "会话ID")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amSessionId)
    private String sessionId;

    @Schema(description = "记忆等级")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amLevel)
    private Integer level;

    @Schema(description = "记忆标题")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amTitle)
    private String title;

    @Schema(description = "记忆内容")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amContent)
    private String content;

    @Schema(description = "重要性评分")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amImportanceScore)
    private Integer importanceScore;

    @Schema(description = "标签列表")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amTags)
    private List<String> tags;

    @Schema(description = "记忆来源")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amSource)
    private String source;

    @Schema(description = "访问次数")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amAccessCount)
    private Integer accessCount;

    @Schema(description = "最后访问时间")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amLastAccessedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedAt;

    @Schema(description = "创建时间")
    @ResultEntityField(property = AgentMemoryEntity.Fields.amCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
