package cn.lin037.nexus.query.agent.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent会话分页查询视图对象
 *
 * @author Lin037
 */
@Data
@ResultEntity(AgentChatSessionEntity.class)
@Schema(description = "Agent会话分页查询视图对象")
public class SessionPageVO {

    @Schema(description = "会话ID")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsId)
    private String id;

    @Schema(description = "学习空间ID")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsLearningSpaceId)
    private String learningSpaceId;

    @Schema(description = "会话标题")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsTitle)
    private String title;

    @Schema(description = "会话类型")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsType)
    private Integer type;

    @Schema(description = "会话状态")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsStatus)
    private Integer status;

    @Schema(description = "会话所属")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsBelongsTo)
    private Integer belongsTo;

    @Schema(description = "自动调用工具权限")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsIsAutoCallTool)
    private Integer isAutoCallTool;

    @Schema(description = "创建时间")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @ResultEntityField(property = AgentChatSessionEntity.Fields.acsUpdatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
