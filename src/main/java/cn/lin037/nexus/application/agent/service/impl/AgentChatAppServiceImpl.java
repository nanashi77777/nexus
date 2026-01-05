package cn.lin037.nexus.application.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.application.agent.dto.ToolListItem;
import cn.lin037.nexus.application.agent.enums.AcsToolEnum;
import cn.lin037.nexus.application.agent.enums.AgentChatErrorCodeEnum;
import cn.lin037.nexus.application.agent.port.*;
import cn.lin037.nexus.application.agent.service.AgentChatAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.*;
import cn.lin037.nexus.infrastructure.common.ai.langchain4j.CustomTokenCountEstimator;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AcsAutoCallToolPermissionEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageRoleEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageTypeEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionStatusEnum;
import cn.lin037.nexus.web.rest.v1.agent.req.ChatReq;
import cn.lin037.nexus.web.rest.v1.agent.vo.StreamingChatVO;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Agent聊天应用服务实现
 * 重构后支持完整的流式响应、取消机制、工具授权和异常处理
 *
 * @author Lin037
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentChatAppServiceImpl implements AgentChatAppService {

    // 核心依赖服务
    private final AgentChatSessionRepository sessionRepository;
    private final AgentChatMessageRepository messageRepository;
    private final AgentChatPort agentChatPort;
    private final AgentCachePort agentCachePort;
    private final CustomTokenCountEstimator customTokenCountEstimator;
    private final AgentLearningTaskRepository learningTaskRepository;
    private final AgentMemoryRepository memoryRepository;

    @Override
    public Flux<ResultVO<StreamingChatVO>> streamingChat(ChatReq req) {
        // 1. 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        // 线程安全的当前消息内容，用于兜底保存
        AtomicReference<String> currentMessageContent = new AtomicReference<>("");

        return Flux.create((FluxSink<ResultVO<StreamingChatVO>> sink) -> {
                    try {
                        // 2. 获取并验证用户会话
                        AgentChatSessionEntity session = sessionRepository.findByIdAndUserId(
                                req.getSessionId(), userId,
                                List.of(AgentChatSessionEntity::getAcsId, AgentChatSessionEntity::getAcsStatus,
                                        AgentChatSessionEntity::getAcsLearningSpaceId, AgentChatSessionEntity::getAcsIsAutoCallTool)
                        ).orElse(null);

                        if (session == null) {
                            sink.error(new ApplicationException(AgentChatErrorCodeEnum.SESSION_NOT_FOUND, "会话ID错误"));
                            return;
                        }
                        session.setAcsId(req.getSessionId());
                        List<AgentLearningTaskEntity> learningTasks = learningTaskRepository.findBySessionId(session.getAcsId());
                        List<AgentMemoryEntity> memories = memoryRepository.findBySessionId(session.getAcsId(),
                                List.of(AgentMemoryEntity::getAmId, AgentMemoryEntity::getAmContent, AgentMemoryEntity::getAmTitle,
                                        AgentMemoryEntity::getAmImportanceScore));
                        boolean isPlanningNeeded;

                        if (learningTasks.isEmpty()) {
                            // [!INFO] 流程图 "否" 分支：没有学习规划
                            log.info("会话[{}] 没有找到学习规划，准备引导创建。", session.getAcsId());
                            isPlanningNeeded = true;


                        } else {
                            // [!INFO] 流程图 "是" 分支：拥有学习规划
                            log.info("会话[{}] 找到了 {} 个学习规划。", session.getAcsId(), learningTasks.size());
                            isPlanningNeeded = false;

                        }

                        // 3. 检查会话状态，防止并发流式处理
                        if (Objects.equals(session.getAcsStatus(), AgentChatSessionStatusEnum.RESPONDING.getCode()) ||
                                Objects.equals(session.getAcsStatus(), AgentChatSessionStatusEnum.TOOL_CALLING.getCode())) {
                            sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.SESSION_BUSY));
                            sink.complete();
                            return;
                        }

                        // 4. 清理历史取消标志
                        clearSessionCancellationFlag(session.getAcsId(), userId, session.getAcsLearningSpaceId());

                        // 5. 更新会话状态为响应中
                        sessionRepository.updateStatus(session.getAcsId(), AgentChatSessionStatusEnum.RESPONDING);

                        // 6. 构建执行上下文
                        AgentChatExecutionContext context = AgentChatExecutionContext.builder()
                                .userId(userId)
                                .sessionId(session.getAcsId())
                                .learningSpaceId(session.getAcsLearningSpaceId())
                                .isPlanningNeeded(isPlanningNeeded) //  设置"是否需要规划"
                                .learningTasks(learningTasks)       // 传入任务列表(空或非空)
                                .build();

                        // 7. 保存用户消息
                        createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.USER,
                                req.getContent(), AgentChatMessageTypeEnum.NORMAL, null,null);

                        // 8. 创建流式聊天监听器
                        StreamingChatListener<AgentChatExecutionContext> listener = createStreamingListener(session, userId, sink, currentMessageContent);

                        // 9. 启动流式聊天
                        agentChatPort.chatStream(session, req.getContent(), listener, context);

                    } catch (ApplicationException e) {
                        log.error("启动流式聊天失败: {}", e.getMessage());

                        try {
                            // 尝试获取会话以设置错误状态
                            AgentChatSessionEntity sessionOnError = sessionRepository.findByIdAndUserId(
                                    req.getSessionId(), userId,
                                    List.of(AgentChatSessionEntity::getAcsId, AgentChatSessionEntity::getAcsLearningSpaceId)
                            ).orElse(null);
                            if (sessionOnError != null) {
                                // 使用 safeSetSessionError 来确保状态被更新为 ERROR
                                safeSetSessionError(sessionOnError);
                            }
                        } catch (Exception cleanupError) {
                            log.error("启动流式聊天失败后，清理会话状态时再次发生错误", cleanupError);
                        }


                        // 根据错误码找到对应的枚举，如果找不到则使用通用错误
                        AgentChatErrorCodeEnum errorCode = findErrorCodeByCode(e.getCode());
                        sink.next(StreamingChatVO.error(errorCode, e.getMessage()));
                        sink.complete();
                    } catch (Exception e) {
                        log.error("启动流式聊天失败", e);


                        try {
                            // 尝试获取会话以设置错误状态
                            AgentChatSessionEntity sessionOnError = sessionRepository.findByIdAndUserId(
                                    req.getSessionId(), userId,
                                    List.of(AgentChatSessionEntity::getAcsId, AgentChatSessionEntity::getAcsLearningSpaceId)
                            ).orElse(null);
                            if (sessionOnError != null) {
                                // 使用 safeSetSessionError 来确保状态被更新为 ERROR
                                safeSetSessionError(sessionOnError);
                            }
                        } catch (Exception cleanupError) {
                            log.error("启动流式聊天失败后，清理会话状态时再次发生错误", cleanupError);
                        }


                        sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, e.getMessage()));
                        sink.complete();
                    }
                })
                .doOnError(error -> {

                    // 错误处理：记录日志并清理资源
                    log.error("流式聊天发生异常，会话ID: {}", req.getSessionId(), error);

                    try {
                        AgentChatSessionEntity session = sessionRepository.findByIdAndUserId(
                                req.getSessionId(), userId,
                                List.of(AgentChatSessionEntity::getAcsLearningSpaceId)
                        ).orElse(null);

                        if (session != null) {
                            // 1. 清理缓存
                            agentCachePort.removeSessionCancellation(req.getSessionId(), userId, session.getAcsLearningSpaceId());

                            // 2. 更新会话状态为错误状态
                            sessionRepository.updateStatus(req.getSessionId(), AgentChatSessionStatusEnum.ERROR);

                            // 3. 保存当前消息内容（如果有的话）
                            String messageContent = currentMessageContent.get();
                            if (messageContent != null && !messageContent.trim().isEmpty()) {
                                createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.ASSISTANT,
                                        messageContent.trim(), AgentChatMessageTypeEnum.NORMAL, null,null);
                                // 清空内容，防止重复保存
                                currentMessageContent.set("");
                            }
                        }

                        // 注意：在doOnError中无法向流发送数据，错误信息会通过异常机制传递

                    } catch (Exception cleanupError) {
                        log.error("异常处理过程中发生错误，会话ID: {}", req.getSessionId(), cleanupError);
                    }
                })
                .doOnCancel(() -> {
                    // 取消处理：清理资源并更新状态
                    log.info("流式聊天被用户取消，会话ID: {}", req.getSessionId());

                    try {
                        AgentChatSessionEntity session = sessionRepository.findByIdAndUserId(
                                req.getSessionId(), userId,
                                List.of(AgentChatSessionEntity::getAcsLearningSpaceId)
                        ).orElse(null);

                        if (session != null) {
                            // 1. 清理缓存
                            agentCachePort.removeSessionCancellation(req.getSessionId(), userId, session.getAcsLearningSpaceId());

                            // 2. 更新会话状态为正常（可以继续对话）
                            sessionRepository.updateStatus(req.getSessionId(), AgentChatSessionStatusEnum.NORMAL);

                            // 3. 保存当前消息内容（如果有的话）
                            String messageContent = currentMessageContent.get();
                            if (messageContent != null && !messageContent.trim().isEmpty()) {
                                createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.ASSISTANT,
                                        messageContent.trim(), AgentChatMessageTypeEnum.NORMAL, null,null);
                                // 清空内容，防止重复保存
                                currentMessageContent.set("");
                            }
                        }

                        // 注意：在doOnCancel中无法向流发送数据，取消信息会通过取消机制传递

                    } catch (Exception cleanupError) {
                        log.error("取消处理过程中发生错误，会话ID: {}", req.getSessionId(), cleanupError);
                    }
                })
                .doFinally(signalType -> {
                    // 最终清理：无论成功、失败还是取消都会执行
                    log.info("流式聊天结束，会话ID: {}, 信号类型: {}", req.getSessionId(), signalType);

                    try {
                        sessionRepository.findByIdAndUserId(
                                req.getSessionId(), userId,
                                List.of(AgentChatSessionEntity::getAcsLearningSpaceId)
                        ).ifPresent(session -> agentCachePort.removeSessionCancellation(req.getSessionId(), userId, session.getAcsLearningSpaceId()));

                        // 记录会话结束日志
                        log.info("会话[{}]流式响应已结束，信号: {}", req.getSessionId(), signalType);

                    } catch (Exception e) {
                        log.error("最终清理过程中发生错误，会话ID: {}", req.getSessionId(), e);
                    }
                });
    }

    /**
     * 根据错误码查找对应的AgentChatErrorCodeEnum
     *
     * @param code 错误码
     * @return 对应的枚举，如果找不到则返回STREAMING_ERROR
     */
    private AgentChatErrorCodeEnum findErrorCodeByCode(String code) {
        for (AgentChatErrorCodeEnum errorCode : AgentChatErrorCodeEnum.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return AgentChatErrorCodeEnum.STREAMING_ERROR;
    }

    @Override
    public void cancelStreaming(Long sessionId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();

            // 1. 校验用户与会话状态
            AgentChatSessionEntity session = sessionRepository.findByIdAndUserId(sessionId, userId,
                    List.of(AgentChatSessionEntity::getAcsId, AgentChatSessionEntity::getAcsUserId,
                            AgentChatSessionEntity::getAcsStatus, AgentChatSessionEntity::getAcsLearningSpaceId)
            ).orElseThrow(() -> new ApplicationException(AgentChatErrorCodeEnum.SESSION_NOT_FOUND));

            // 2. 检查会话是否处于可取消状态
            if (!AgentChatSessionStatusEnum.RESPONDING.getCode().equals(session.getAcsStatus())
                    && !AgentChatSessionStatusEnum.TOOL_CALLING.getCode().equals(session.getAcsStatus())) {
                log.warn("会话[{}]当前状态[{}]不需要取消", sessionId, session.getAcsStatus());
                return;
            }

            // 3. 设置取消标志
            markSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId());

            // 4. 设置取消标志后，流式监听器会自动检测并处理取消

            // 5. 更新会话状态为取消
            boolean updated = sessionRepository.updateStatusAtomically(sessionId,
                    AgentChatSessionStatusEnum.RESPONDING, AgentChatSessionStatusEnum.CANCELLED);

            if (!updated) {
                // 如果从RESPONDING更新失败，尝试从TOOL_CALLING更新
                sessionRepository.updateStatusAtomically(sessionId,
                        AgentChatSessionStatusEnum.TOOL_CALLING, AgentChatSessionStatusEnum.CANCELLED);
            }

            log.info("用户[{}]成功取消会话[{}]的流式对话", userId, sessionId);

        } catch (Exception e) {
            log.error("取消流式聊天失败，会话ID: {}", sessionId, e);
            throw new ApplicationException(AgentChatErrorCodeEnum.STREAMING_ERROR, e.getMessage());
        }
    }

    /**
     * 创建并保存消息（统一方法）
     */
    private AgentChatMessageEntity createAndSaveMessage(AgentChatSessionEntity session, Long userId,
                                                        AgentChatMessageRoleEnum role, String content,
                                                        AgentChatMessageTypeEnum type, String correlationContent,TokenUsage tokenUsage) {
        AgentChatMessageEntity message = new AgentChatMessageEntity();
        message.setAcmId(HutoolSnowflakeIdGenerator.generateLongId());
        message.setAcmSessionId(session.getAcsId());
        message.setAcmUserId(userId);
        message.setAcmLearningSpaceId(session.getAcsLearningSpaceId());
        message.setAcmRole(role.getRole());
        message.setAcmContent(content);
        message.setAcmType(type.getCode());

        message.setAcmCorrelationContent(correlationContent);

        message.setAcmCreatedAt(LocalDateTime.now());
        message.setAcmUpdatedAt(LocalDateTime.now());

        if (tokenUsage != null) {
            message.setAcmTokens(tokenUsage.totalTokenCount());
        } else if (content != null && !content.isEmpty()) {
            message.setAcmTokens(customTokenCountEstimator.estimateTokenCountInText(content));
        }

        messageRepository.save(message);
        return message;
    }

    /**
     * 创建流式聊天监听器
     */
    private StreamingChatListener<AgentChatExecutionContext> createStreamingListener(
            AgentChatSessionEntity session,
            Long userId,
            FluxSink<ResultVO<StreamingChatVO>> sink,
            AtomicReference<String> currentMessageContent) {


        return new StreamingChatListener<>() {

            @Override
            public void onThinkingToken(String token, AgentChatExecutionContext context) {
                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onThinkingToken]检测到会话取消标志，停止流式响应");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return;
                }

                // 积累消息内容（思考部分）
                currentMessageContent.updateAndGet(current -> current + token);

                // 发送思考片段
                sink.next(StreamingChatVO.thinkingContent(token));
            }

            @Override
            public void onContentToken(String token, AgentChatExecutionContext context) {
                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onContentToken]检测到会话取消标志，停止流式响应");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return;
                }

                // 积累消息内容（内容部分）
                currentMessageContent.updateAndGet(current -> current + token);

                // 发送内容片段
                sink.next(StreamingChatVO.content(token));
            }

            @Override
            public ContentDecision onCompleteMessage(AiMessage aiMessage, TokenUsage tokenUsage, FinishReason finishReason, AgentChatExecutionContext context) {

                // 直接使用aiMessage.text()保存消息，无论finishReason是什么
                // 因为工具信息存储在toolExecutionRequests中，不会影响text内容
                String messageText = aiMessage.text();
                if (messageText != null && !messageText.trim().isEmpty()) {
                    createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.ASSISTANT,
                            messageText.trim(), AgentChatMessageTypeEnum.NORMAL, null,tokenUsage);
                    log.info("保存AI消息，内容长度: {}, 完成原因: {}", messageText.length(), finishReason);
                } else {
                    log.debug("AI消息内容为空，跳过保存，完成原因: {}", finishReason);
                }

                // 清空累积内容，防止后续内容累积
                currentMessageContent.set("");

                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onCompleteMessage]检测到会话取消标志，停止流式响应");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return ContentDecision.TERMINATE;
                }

                return ContentDecision.CONTINUE;
            }

            @Override
            public ToolBatchDecision onReceiveCompleteToolList(List<ToolExecutionRequest> toolExecutionRequests, TokenUsage tokenUsage, AgentChatExecutionContext context) {
                log.info("检测到工具调用请求，数量: {}", toolExecutionRequests.size());

                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onReceiveCompleteToolList]检测到会话取消标志，停止工具调用");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return ToolBatchDecision.DEFER_AND_CLOSE;
                }

                // 创建工具列表项
                List<ToolListItem> toolListItems = toolExecutionRequests.stream()
                        .map(request -> ToolListItem.create(HutoolSnowflakeIdGenerator.generateLongId(), request))
                        .collect(Collectors.toList());

                // 保存工具列表到上下文和数据库
                context.setToolList(toolListItems);
                String toolListJsonStr = JSONUtil.toJsonStr(toolListItems);
                AgentChatMessageEntity toolListMessage = createAndSaveMessage(session, userId,
                        AgentChatMessageRoleEnum.ASSISTANT, toolListJsonStr,
                        AgentChatMessageTypeEnum.TOOL_LIST, null,tokenUsage);
                context.setCurrentToolRequestMessage(toolListMessage);

                // 设置第一个工具为当前执行工具（如果有工具的话）
                if (!toolListItems.isEmpty()) {
                    context.setCurrentExecutingToolId(toolListItems.getFirst().getId());
                    log.debug("设置第一个工具为当前执行工具，工具ID: {}", toolListItems.getFirst().getId());
                }

                // 解析会话的工具自动执行权限
                AcsAutoCallToolPermissionEnum permission = AcsAutoCallToolPermissionEnum.fromCode(session.getAcsIsAutoCallTool());
                if (permission == null) {
                    log.warn("接受工具列表时，会话工具权限配置无效，会话ID: {}", session.getAcsId());
                    return ToolBatchDecision.DEFER_AND_CLOSE;
                }

                // 如果权限为关闭，直接返回工具列表给用户并结束流式响应
                if (permission == AcsAutoCallToolPermissionEnum.CLOSED) {
                    log.info("工具自动执行权限已关闭，返回工具列表给用户，会话ID: {}", session.getAcsId());

                    // 更新会话状态为等待工具授权
                    sessionRepository.updateStatusAtomically(context.getSessionId(),
                            AgentChatSessionStatusEnum.RESPONDING, AgentChatSessionStatusEnum.WAITING_TOOL_AUTHORIZATION);

                    // 设置等待授权标识
                    context.setWaitAuthAfterClose(true);

                    // 发送工具列表事件
                    sink.next(StreamingChatVO.toolList(toolListJsonStr));

                    return ToolBatchDecision.DEFER_AND_CLOSE;
                }

                // 其他权限情况，更新会话状态为工具调用中，继续执行工具
                sessionRepository.updateStatusAtomically(context.getSessionId(),
                        AgentChatSessionStatusEnum.RESPONDING, AgentChatSessionStatusEnum.TOOL_CALLING);

                return ToolBatchDecision.PROCEED;
            }

            @Override
            public ToolDecision onToolExecutionStart(ToolExecutionRequest toolExecutionRequest, AgentChatExecutionContext context) {
                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onToolExecutionStart]检测到会话取消标志，停止工具执行");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return ToolDecision.DEFER_AND_CLOSE;
                }

                // 查找对应的工具列表项（使用当前执行工具ID）
                Long currentToolId = context.getCurrentExecutingToolId();
                ToolListItem toolListItem = context.findToolListItemById(currentToolId);
                if (toolListItem == null) {
                    log.warn("单个工具调用时，未找到工具列表项，当前执行工具ID: {}", currentToolId);
                    return ToolDecision.DEFER_AND_CLOSE;
                }

                // 解析权限
                AcsAutoCallToolPermissionEnum permission = AcsAutoCallToolPermissionEnum.fromCode(session.getAcsIsAutoCallTool());
                if (permission == null) {
                    log.warn("会话工具权限配置无效，会话ID: {}", session.getAcsId());
                    toolListItem.markFailed("工具权限配置无效");
                    updateToolListMessage(context);
                    return ToolDecision.DEFER_AND_CLOSE;
                }

                // 匹配工具定义
                AcsToolEnum matchedTool = AcsToolEnum.getByName(toolExecutionRequest.name());
                if (matchedTool == null) {
                    log.warn("未知工具类型: {}", toolExecutionRequest.name());
                    toolListItem.markWaitingAuthorization();
                    updateToolListMessage(context);

                    // 设置等待授权标识
                    context.setWaitAuthAfterClose(true);

                    // 发送工具执行延迟事件
                    sink.next(StreamingChatVO.toolExecutionDeferred());
                    return ToolDecision.DEFER_AND_CLOSE;
                }

                boolean shouldExecute = isShouldExecute(matchedTool, permission);

                // 按权限与工具读写属性判断是否应该执行

                if (shouldExecute) {
                    // 允许自动执行
                    log.info("工具允许自动执行: {}", toolExecutionRequest.name());

                    // 标记工具开始执行
                    toolListItem.markExecuting();
                    updateToolListMessage(context);

                    // 发送工具调用开始事件
                    sink.next(StreamingChatVO.toolCallStart(Json.toJson(toolExecutionRequest)));

                    return ToolDecision.EXECUTE;
                } else {
                    // 不允许自动执行，需要用户授权
                    log.info("工具需要用户授权: {}", toolExecutionRequest.name());

                    // 标记工具等待授权
                    toolListItem.markWaitingAuthorization();
                    updateToolListMessage(context);

                    // 设置等待授权标识
                    context.setWaitAuthAfterClose(true);

                    // 发送工具执行延迟事件
                    sink.next(StreamingChatVO.toolExecutionDeferred());

                    return ToolDecision.DEFER_AND_CLOSE;
                }
            }

            @Override
            public ToolContinueDecision onToolExecutionFinish(ToolExecutionResult executionResult, AgentChatExecutionContext context) {
                // 查找对应的工具列表项（使用当前执行工具ID）
                Long currentToolId = context.getCurrentExecutingToolId();
                ToolListItem toolListItem = context.findToolListItemById(currentToolId);
                if (toolListItem == null) {
                    log.warn("工具执行完成时，未找到工具列表项，当前执行工具ID: {}", currentToolId);
                    return ToolContinueDecision.CONTINUE;
                }

                // 根据执行结果更新工具状态
                if (executionResult.getStatus() == ToolExecutionResult.ToolExecutionStatus.SUCCESS) {
                    // 判断是否为查询类工具，如果是则保存工具执行结果为单独消息
                    AcsToolEnum matchedTool = AcsToolEnum.getByName(toolListItem.getName());
                    if (matchedTool != null && matchedTool.isReadOperation()) {
                        toolListItem.markSuccess("工具执行成功");
                        // 查询类工具且执行成功，保存结果为单独消息用于上下文
                        createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.ASSISTANT,
                                "工具调用记录ID为`" + toolListItem.getId() + "`的执行结果为: " + executionResult.getResultText(), AgentChatMessageTypeEnum.TOOL_RESPONSE, null,null);
                        log.info("保存查询类工具结果到消息历史: {}", toolListItem.getName());
                    }
                    toolListItem.markSuccess("工具执行成功");
                    log.info("工具执行成功: {}", toolListItem.getName());
                } else {
                    toolListItem.markFailed(executionResult.getErrorMessage() != null ?
                            executionResult.getErrorMessage() : "工具执行失败");
                    log.warn("工具执行失败: {}, 错误: {}",
                            executionResult.getRequest().name(), executionResult.getErrorMessage());
                }

                // 更新工具列表消息
                updateToolListMessage(context);

                // 检查是否被取消
                if (isSessionCancelled(session.getAcsId(), userId, session.getAcsLearningSpaceId())) {
                    log.info("[onToolExecutionFinish]检测到会话取消标志，停止工具执行完成处理");
                    sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, "流式对话已被取消"));
                    sink.complete();
                    return ToolContinueDecision.TERMINATE;
                }

                // 发送工具完成事件
                sink.next(StreamingChatVO.toolCallResult(executionResult.getResultText()));

                // 查找下一个工具并设置为当前执行工具
                setNextExecutingTool(context, currentToolId);

                return ToolContinueDecision.CONTINUE;
            }

            @Override
            public void onStreamingComplete(AiMessage finalMessage, AgentChatExecutionContext context) {

                log.info("流式聊天完成，会话ID: {}", session.getAcsId());

                // 根据waitAuthAfterClose标识决定会话状态
                if (context.isWaitAuthAfterClose()) {
                    // 需要等待用户授权，保持WAITING_TOOL_AUTHORIZATION状态
                    log.info("流式响应结束，但需要等待工具授权，保持WAITING_TOOL_AUTHORIZATION状态");
                } else {
                    // 正常完成，恢复会话状态为正常
                    sessionRepository.updateStatusAtomically(context.getSessionId(),
                            AgentChatSessionStatusEnum.TOOL_CALLING, AgentChatSessionStatusEnum.NORMAL);
                    sessionRepository.updateStatusAtomically(context.getSessionId(),
                            AgentChatSessionStatusEnum.RESPONDING, AgentChatSessionStatusEnum.NORMAL);
                    log.info("流式对话正常完成，会话状态更新为NORMAL");
                }

                // 清理取消标志
                clearSessionCancellationFlag(session.getAcsId(), userId, session.getAcsLearningSpaceId());

                // 发送完成事件
                sink.next(StreamingChatVO.done());
                sink.complete();
            }

            @Override
            public void onError(Throwable error, AgentChatExecutionContext context) {
                log.error("流式处理发生异常，会话ID: {}", session.getAcsId(), error);

                // 保存当前积累的响应为错误消息（如果有内容）
                if (!currentMessageContent.get().isEmpty()) {
                    createAndSaveMessage(session, userId, AgentChatMessageRoleEnum.ASSISTANT,
                            currentMessageContent.get(), AgentChatMessageTypeEnum.NORMAL, null,null);
                }

                // 根据waitAuthAfterClose标识决定会话状态
                if (context.isWaitAuthAfterClose()) {
                    // 如果是在等待授权过程中出错，保持WAITING_TOOL_AUTHORIZATION状态
                    log.warn("在等待工具授权过程中发生错误，保持WAITING_TOOL_AUTHORIZATION状态");
                } else {
                    // 正常错误，设置会话为错误状态
                    safeSetSessionError(session);
                }

                // 发送错误消息
                sink.next(StreamingChatVO.error(AgentChatErrorCodeEnum.STREAMING_ERROR, error.getMessage()));
                sink.complete();
            }
        };
    }

    /**
     * 标记会话为已取消
     */
    private void markSessionCancelled(Long sessionId, Long userId, Long learningSpaceId) {
        try {
            agentCachePort.setSessionCancelled(sessionId, userId, learningSpaceId, Duration.ofMinutes(5));
            log.info("设置会话取消标志，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("设置会话取消标志失败，会话ID: {}", sessionId, e);
        }
    }

    /**
     * 检查会话是否被取消
     */
    private boolean isSessionCancelled(Long sessionId, Long userId, Long learningSpaceId) {
        try {
            return agentCachePort.isSessionCancelled(sessionId, userId, learningSpaceId);
        } catch (Exception e) {
            log.error("检查会话取消状态失败，会话ID: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 清理会话取消标志
     */
    private void clearSessionCancellationFlag(Long sessionId, Long userId, Long learningSpaceId) {
        try {
            agentCachePort.removeSessionCancellation(sessionId, userId, learningSpaceId);
            log.debug("清理会话取消标志，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("清理会话取消标志失败，会话ID: {}", sessionId, e);
        }
    }

    /**
     * 设置下一个执行工具
     * 在当前工具开始执行时，查找工具列表中的下一个工具并设置为当前执行工具
     *
     * @param context       执行上下文
     * @param currentToolId 当前工具ID
     */
    private void setNextExecutingTool(AgentChatExecutionContext context, Long currentToolId) {
        try {
            List<ToolListItem> toolList = context.getToolList();
            if (toolList == null || toolList.isEmpty()) {
                return;
            }

            // 查找当前工具在列表中的位置
            int currentIndex = -1;
            for (int i = 0; i < toolList.size(); i++) {
                if (currentToolId.equals(toolList.get(i).getId())) {
                    currentIndex = i;
                    break;
                }
            }

            // 如果找到当前工具且不是最后一个，设置下一个工具为当前执行工具
            if (currentIndex >= 0 && currentIndex < toolList.size() - 1) {
                Long nextToolId = toolList.get(currentIndex + 1).getId();
                context.setCurrentExecutingToolId(nextToolId);
                log.debug("设置下一个工具为当前执行工具，工具ID: {}", nextToolId);
            } else {
                // 如果是最后一个工具，清空当前执行工具ID
                context.setCurrentExecutingToolId(null);
                log.debug("已是最后一个工具，清空当前执行工具ID");
            }
        } catch (Exception e) {
            log.error("设置下一个执行工具失败", e);
        }
    }

    /**
     * 更新工具列表消息
     * 将当前工具列表的状态更新到数据库中的工具列表消息
     *
     * @param context 执行上下文
     */
    private void updateToolListMessage(AgentChatExecutionContext context) {
        try {
            AgentChatMessageEntity toolListMessage = context.getCurrentToolRequestMessage();
            List<ToolListItem> toolList = context.getToolList();

            if (toolListMessage != null && toolList != null) {
                // 更新消息内容为最新的工具列表状态
                toolListMessage.setAcmContent(JSONUtil.toJsonStr(toolList));
                toolListMessage.setAcmUpdatedAt(LocalDateTime.now());
                messageRepository.updateById(toolListMessage);

                log.debug("更新工具列表消息成功，消息ID: {}", toolListMessage.getAcmId());
            }
        } catch (Exception e) {
            log.error("更新工具列表消息失败", e);
        }
    }

    /**
     * 安全地设置会话为错误状态
     */
    private void safeSetSessionError(AgentChatSessionEntity session) {
        try {
            boolean updated = sessionRepository.updateStatusAtomically(session.getAcsId(),
                    AgentChatSessionStatusEnum.RESPONDING, AgentChatSessionStatusEnum.ERROR);

            if (!updated) {
                updated = sessionRepository.updateStatusAtomically(session.getAcsId(),
                        AgentChatSessionStatusEnum.TOOL_CALLING, AgentChatSessionStatusEnum.ERROR);
            }

            if (!updated) {
                // 强制更新
                session.setAcsStatus(AgentChatSessionStatusEnum.ERROR.getCode());
                sessionRepository.updateById(session);
            }

            log.info("设置会话为错误状态，会话ID: {}", session.getAcsId());
        } catch (Exception e) {
            log.error("设置会话错误状态失败，会话ID: {}", session.getAcsId(), e);
        }
    }


    /**
     * 判断工具是否应该执行
     *
     * @param matchedTool 匹配到的工具
     * @param permission  工具权限
     * @return 是否应该执行
     */
    private static boolean isShouldExecute(AcsToolEnum matchedTool, AcsAutoCallToolPermissionEnum permission) {
        boolean isRead = matchedTool.isReadOperation();
        boolean isWrite = matchedTool.isWriteOperation();
        return switch (permission) {
            case READ_ONLY -> isRead && !isWrite;
            case WRITE_ONLY -> isWrite;
            case READ_WRITE -> isRead || isWrite;
            case CLOSED ->
                // CLOSED权限在onReceiveCompleteToolList中已处理，这里不应该到达
                    false;
        };
    }
}
