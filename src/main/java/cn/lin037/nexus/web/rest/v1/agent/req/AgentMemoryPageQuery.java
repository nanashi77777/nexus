package cn.lin037.nexus.web.rest.v1.agent.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AgentMemory分页查询参数类")
public class AgentMemoryPageQuery extends BasePageQuery {

    /**
     * 学习空间ID
     */
    @Schema(description = "学习空间ID")
    private Long learningSpaceId;

    /**
     * 记忆等级
     */
    @Schema(description = "记忆等级")
    private Integer level;

    /**
     * 记忆来源
     */
    @Schema(description = "记忆来源")
    private String source;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long sessionId;
}
