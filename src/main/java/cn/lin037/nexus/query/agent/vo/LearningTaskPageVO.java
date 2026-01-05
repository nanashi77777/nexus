package cn.lin037.nexus.query.agent.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent学习任务分页查询视图对象
 *
 * @author Lin037
 */
@Data
@ResultEntity(AgentLearningTaskEntity.class)
@Schema(description = "Agent学习任务分页查询视图对象")
public class LearningTaskPageVO {

    @Schema(description = "学习任务ID")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altId)
    private String id;

    @Schema(description = "学习空间ID")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altLearningSpaceId)
    private String learningSpaceId;

    @Schema(description = "所属会话ID")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altSessionId)
    private String sessionId;

    @Schema(description = "规划标题")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altTitle)
    private String title;

    @Schema(description = "学习目标")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altObjective)
    private String objective;

    @Schema(description = "难度评估")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altDifficultyLevel)
    private Integer difficultyLevel;

    @Schema(description = "是否完成")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altIsCompleted)
    private Boolean isCompleted;

    @Schema(description = "创建时间")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @ResultEntityField(property = AgentLearningTaskEntity.Fields.altUpdatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
