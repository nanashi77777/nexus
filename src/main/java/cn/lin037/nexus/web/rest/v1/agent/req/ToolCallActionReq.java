package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 工具调用操作请求参数
 * 用于授权或拒绝特定的工具调用
 *
 * @author Lin037
 */
@Data
public class ToolCallActionReq {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 工具调用记录ID列表
     * 指定要授权或拒绝的工具调用记录
     */
    @NotEmpty(message = "工具调用记录ID列表不能为空")
    private List<Long> toolRecordIds;
}