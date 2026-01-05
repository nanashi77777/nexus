package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI对话请求参数
 *
 * @author Lin037
 */
@Data
public class ChatReq {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 聊天内容
     */
    @NotBlank(message = "聊天内容不能为空")
    @Size(max = 2000, message = "聊天内容不能超过2000个字符")
    private String content;
}