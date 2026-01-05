package cn.lin037.nexus.application.agent.service;

import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.agent.req.ChatReq;
import cn.lin037.nexus.web.rest.v1.agent.vo.StreamingChatVO;
import reactor.core.publisher.Flux;

/**
 * Agent 聊天应用服务
 * 负责编排AI流式聊天、工具调用、会话管理等业务用例。
 */
public interface AgentChatAppService {

    /**
     * 启动流式聊天对话
     *
     * @param req 请求参数，包含会话ID、消息内容等
     * @return SSE流式事件
     */
    Flux<ResultVO<StreamingChatVO>> streamingChat(ChatReq req);

    /**
     * 取消当前会话的流式对话
     *
     * @param sessionId 会话ID
     */
    void cancelStreaming(Long sessionId);
}
