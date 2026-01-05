package cn.lin037.nexus.web.rest.v1.agent.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent消息分页查询参数
 * 专门用于聊天消息的分页查询
 *
 * @author lin037
 * @since 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent消息分页查询参数")
public class AgentMessagePageQuery extends BasePageQuery {

    /**
     * 学习空间ID
     */
    @Schema(description = "学习空间ID")
    private Long learningSpaceId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long sessionId;
}