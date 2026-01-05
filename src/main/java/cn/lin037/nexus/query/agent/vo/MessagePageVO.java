package cn.lin037.nexus.query.agent.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent消息分页查询视图对象
 *
 * @author Lin037
 */
@Data
@ResultEntity(AgentChatMessageEntity.class)
@Schema(description = "Agent消息分页查询视图对象")
public class MessagePageVO {

    @Schema(description = "消息ID")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmId)
    private String id;

    @Schema(description = "会话ID")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmSessionId)
    private String sessionId;

    @Schema(description = "学习空间ID")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmLearningSpaceId)
    private String learningSpaceId;

    @Schema(description = "消息角色")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmRole)
    private String role;

    @Schema(description = "消息内容")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmContent)
    private String content;

    @Schema(description = "消息类型")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmType)
    private Integer messageType;

    @Schema(description = "关联内容")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmCorrelationContent)
    private String correlationContent;

    @Schema(description = "Token数量")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmTokens)
    private Integer tokens;

    @Schema(description = "创建时间")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmCreatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @ResultEntityField(property = AgentChatMessageEntity.Fields.acmUpdatedAt)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
