package cn.lin037.nexus.web.rest.v1.agent.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent分页查询参数类
 * 继承基础分页查询参数，添加Agent相关的筛选条件
 *
 * @author Lin037
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AgentSession分页查询参数类")
public class AgentSessionPageQuery extends BasePageQuery {

    /**
     * 学习空间ID
     */
    @Schema(description = "学习空间ID")
    private Long learningSpaceId;

    /**
     * 会话状态（用于会话查询）
     */
    @Schema(description = "会话状态")
    private Integer status;
}