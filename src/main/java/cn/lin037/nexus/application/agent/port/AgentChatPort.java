package cn.lin037.nexus.application.agent.port;

import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.web.rest.v1.agent.vo.StreamingChatVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Agent 聊天端口，定义了与底层AI模型交互的契约
 *
 * @author Lin037
 */
public interface AgentChatPort {

    /**
     * 发起新的流式聊天
     *
     * @param session     当前会话
     * @param userMessage 用户输入的消息
     * @param listener    流式聊天事件监听器
     * @param context     执行上下文
     */
    void chatStream(AgentChatSessionEntity session,
                    String userMessage,
                    StreamingChatListener<AgentChatExecutionContext> listener,
                    AgentChatExecutionContext context);


    /**
     * 在用户授权后，执行工具并继续聊天流程
     *
     * @param session             当前会话
     * @param latestAiMessage     最新的AI消息（包含工具调用请求）
     * @return 聊天响应流
     */
    Flux<StreamingChatVO> executeToolsAndContinueStream(
            AgentChatSessionEntity session,
            AgentChatMessageEntity latestAiMessage,
            StreamingChatListener<AgentChatExecutionContext> listener,
            AgentChatExecutionContext context
    );
}