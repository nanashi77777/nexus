package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.application.agent.port.AgentChatPort;
import cn.lin037.nexus.infrastructure.adapter.agent.constant.AgentSystemPrompt;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatToolService;
import cn.lin037.nexus.infrastructure.common.ai.service.ToolExecutor;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageRoleEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageTypeEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.LearningSpaceMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentChatMessageMapper;
import cn.lin037.nexus.web.rest.v1.agent.vo.StreamingChatVO;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.internal.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 缓存端口适配器实现
 * 将基础设施层的CacheService适配到应用层的CachePort接口
 * 遵循依赖倒置原则，应用层通过端口接口访问缓存功能
 *
 * @author <a href="https://github.com/lin037">lin037</a>
 */
@Slf4j
@Component
public class AgentChatAdapter implements AgentChatPort {

    private final AgentChatMessageMapper agentChatMessageMapper;
    private final LearningSpaceMapper learningSpaceMapper;

    private final ToolExecutor<AgentChatExecutionContext> toolExecutor;

    private final StreamingChatToolService streamingChatToolService;

    public AgentChatAdapter(AgentChatMessageMapper agentChatMessageMapper,
                            LearningSpaceMapper learningSpaceMapper,
                            @Qualifier("AgentChatToolExecutorImpl") ToolExecutor<AgentChatExecutionContext> toolExecutor,
                            StreamingChatToolService streamingChatToolService) {
        this.agentChatMessageMapper = agentChatMessageMapper;
        this.learningSpaceMapper = learningSpaceMapper;
        this.toolExecutor = toolExecutor;
        this.streamingChatToolService = streamingChatToolService;
    }

    /**
     * 带执行上下文的流式聊天实现
     */
    @Override
    public void chatStream(AgentChatSessionEntity currentSession, String content,
                           StreamingChatListener<AgentChatExecutionContext> streamingChatListener,
                           AgentChatExecutionContext toolExecutionContext) {

        List<ChatMessage> messages = new ArrayList<>();

//        messages.add(SystemMessage.from(AgentSystemPrompt.SYSTEM_PROMPT));

        if (toolExecutionContext.isPlanningNeeded()) {
            // "否" 分支：需要创建规划
            messages.add(SystemMessage.from(AgentSystemPrompt.PLANNING_PROMPT));
        } else {
            List<AgentLearningTaskEntity> tasks = toolExecutionContext.getLearningTasks();
            List<AgentMemoryEntity> memories = toolExecutionContext.getMemories();

            String tasksString = formatLearningTasks(tasks);
            String memoriesString = formatMemories(memories);
            String guidingPrompt = AgentSystemPrompt.GUIDING_PROMPT
                    .replace("{{LEARNING_TASKS_LIST}}", tasksString)
                    .replace("{{SESSION_MEMORIES}}", memoriesString);

            messages.add(SystemMessage.from(guidingPrompt));
        }


//        QueryChain.of(agentChatMessageMapper)
//                .eq(AgentChatMessageEntity::getAcmSessionId, currentSession.getAcsId())
//                .limit(100)
//                .orderByDesc(AgentChatMessageEntity::getAcmCreatedAt)
//                .list().forEach(message -> {
//                    if (message.getAcmRole().equals(AgentChatMessageRoleEnum.USER.getRole())) {
//                        messages.add(new UserMessage(message.getAcmContent()));
//                    } else {
//                        messages.add(new AiMessage(message.getAcmContent()));
//                    }
//                });
        List<AgentChatMessageEntity> dbMessages = QueryChain.of(agentChatMessageMapper)
                .eq(AgentChatMessageEntity::getAcmSessionId, currentSession.getAcsId())
                .limit(100)
                .orderByDesc(AgentChatMessageEntity::getAcmCreatedAt)
                .list();

        java.util.Collections.reverse(dbMessages);

        dbMessages.forEach(message -> {
            if (message.getAcmRole().equals(AgentChatMessageRoleEnum.USER.getRole())) {
                messages.add(new UserMessage(message.getAcmContent()));
            } else if (message.getAcmRole().equals(AgentChatMessageRoleEnum.ASSISTANT.getRole())){
                if (message.getAcmType().equals(AgentChatMessageTypeEnum.NORMAL.getCode())) {
                    if (message.getAcmContent() != null && !message.getAcmContent().isEmpty()) {
                        messages.add(new AiMessage(message.getAcmContent()));
                    }
                } else if (message.getAcmType().equals(AgentChatMessageTypeEnum.TOOL_LIST.getCode())) {
                    try {
                        String toolListJson = message.getAcmContent();
                        List<ToolExecutionRequest> toolRequests = Json.fromJson(toolListJson,
                                new com.google.gson.reflect.TypeToken<List<ToolExecutionRequest>>() {}.getType());

                        messages.add(new AiMessage(toolRequests));

                    } catch (Exception e) {
                        log.warn("Failed to parse TOOL_LIST (acmType=4) message content (ID: {}). Skipping history item.", message.getAcmId(), e);
                    }
                }

            }
        });
        messages.add(new UserMessage(content));

        streamingChatToolService.chat(
                "deepseek-chat",
                "STREAMING_CHAT_WITH_TOOL",
                messages,
                toolExecutor,
                streamingChatListener,
                toolExecutionContext
        );
    }

    /**
     * 在用户授权工具后，继续对话流程并执行工具。
     * 该方法将：
     * 1) 构建与最新 AI 消息衔接的对话上下文；
     * 2) 通过 StreamingChatToolService 发起对话续写与工具执行；
     * 3) 用代理监听器将底层事件转换为 StreamingChatVO 并转发给上层 listener 以进行持久化；
     *
     * @param session             当前会话
     * @param latestAiMessage     最新的 AI 消息（包含工具调用请求的上下文）
     * @param listener            应用层监听器，用于持久化消息、记录工具执行、统计令牌等
     * @param context             工具执行上下文
     * @return Flux 流式输出给前端的事件对象
     */
    @Override
    public Flux<StreamingChatVO> executeToolsAndContinueStream(AgentChatSessionEntity session,
                                                               AgentChatMessageEntity latestAiMessage,
                                                               StreamingChatListener<AgentChatExecutionContext> listener,
                                                               AgentChatExecutionContext context) {
        return Flux.create(sink -> {
        });
    }

    /**
     * 构建工具执行上下文
     *
     * @param currentSession 当前聊天会话实体
     * @return 执行上下文对象
     */
    private AgentChatExecutionContext buildToolExecutionContext(AgentChatSessionEntity currentSession) {
        return AgentChatExecutionContext.builder()
                .userId(currentSession.getAcsUserId())
                .sessionId(currentSession.getAcsId())
                .learningSpaceId(currentSession.getAcsLearningSpaceId())
                .build();
    }

    private String formatMemories(List<AgentMemoryEntity> memories) {
        if (memories == null || memories.isEmpty()) {
            return "  (当前没有相关记忆)";
        }

        AtomicInteger index = new AtomicInteger(1);
        return memories.stream()
                .map(memory -> String.format(
                        "  %d. [记忆] %s (重要性: %d)\n     - 内容: %s\n     - 记忆ID: %d",
                        index.getAndIncrement(),
                        memory.getAmTitle(),
                        memory.getAmImportanceScore(),
                        memory.getAmContent(),
                        memory.getAmId()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatLearningTasks(List<AgentLearningTaskEntity> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return "  (当前没有学习任务)";
        }

        AtomicInteger index = new AtomicInteger(1);
        return tasks.stream()
                .map(task -> {
                    String status = task.getAltIsCompleted() ? "[已完成]" : "[未完成]";
                    return String.format(
                            "  %d. %s %s\n     - 目标: %s\n     - 难度: %d\n     - 任务ID: %d",
                            index.getAndIncrement(),
                            status,
                            task.getAltTitle(),
                            task.getAltObjective(),
                            task.getAltDifficultyLevel(),
                            task.getAltId()
                    );
                })
                .collect(Collectors.joining("\n"));
    }
}