package cn.lin037.nexus.infrastructure.common.ai.service.impl;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.*;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.DefaultLoopLimitContext;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.LoopLimitContext;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatToolService;
import cn.lin037.nexus.infrastructure.common.ai.service.ToolExecutor;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流式聊天工具服务实现类。
 * 实现了 StreamingChatToolService 接口，提供与支持工具调用的AI模型进行流式聊天的能力。
 * 该服务使用循环方式处理工具调用，避免递归导致的内存问题。
 * <p>
 * 主要功能：
 * 1. 支持流式文本生成，实时返回AI生成的内容
 * 2. 自动处理工具调用请求，执行相应的工具并将结果反馈给AI
 * 3. 维护完整的对话上下文，确保多轮对话的连贯性
 * 4. 通过监听器模式提供丰富的事件通知
 * 5. 使用循环代替递归，提高稳定性和性能
 *
 * @author Lin037
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingChatToolServiceImpl implements StreamingChatToolService {

    /**
     * 与AI模型进行流式聊天，并根据需要执行工具。
     * <p>
     * 执行流程：
     * 1. 获取指定的流式聊天模型
     * 2. 创建工具调用响应处理器
     * 3. 开始流式对话，处理AI响应和工具调用
     * 4. 通过监听器通知各种事件给调用方
     *
     * @param modelName    AI模型的名称（如：gpt-4、claude-3等）
     * @param usedFor      使用场景标识（用于模型配置和统计）
     * @param messages     当前的聊天消息列表（包含用户消息、AI消息、工具执行结果等）
     * @param toolExecutor 工具执行器，用于执行AI请求的工具
     * @param listener     流式聊天监听器，用于接收和处理聊天过程中的各种事件
     */
    @Override
    public void chat(String modelName, String usedFor, List<ChatMessage> messages, ToolExecutor<DefaultLoopLimitContext> toolExecutor, StreamingChatListener<DefaultLoopLimitContext> listener) {
        this.chat(modelName, usedFor, messages, toolExecutor, listener, new DefaultLoopLimitContext());
    }

    // AI核心服务，用于获取不同的AI模型实例
    private final AiCoreService aiCoreService;

    /**
     * 与AI模型进行流式聊天（带工具执行上下文），并根据需要执行工具。
     * 将上下文仅用于事件监听器的回调透传，infra层不解析上下文具体内容。
     */
    @Override
    public <R extends LoopLimitContext> void chat(String modelName, String usedFor, List<ChatMessage> messages,
                                                  ToolExecutor<R> toolExecutor, StreamingChatListener<R> listener, R context) {
        StreamingChatModel model = null;
        try {
            model = aiCoreService.getStreamingChatModel(modelName, usedFor);
        } catch (Exception e) {
            log.error("启动AI对话时发生异常", e);
            if (listener != null) {
                listener.onError(e, context);
                listener.onStreamingComplete(AiMessage.from("AI对话启动失败：" + e.getMessage()), context);
            }
        }
        // 启动异步流式对话处理，不阻塞当前线程
        startAsyncChatLoop(model, messages, toolExecutor, listener, context);
    }

    /**
     * 启动异步聊天循环处理。
     * 使用事件驱动的方式处理AI响应，避免阻塞等待。
     * 每次AI响应完成后，会自动触发下一轮对话或结束流程。
     *
     * @param model        流式聊天模型
     * @param messages     对话消息列表
     * @param toolExecutor 工具执行器
     * @param listener     流式聊天监听器
     * @param context      循环限制上下文
     * @param <R>          上下文类型
     */
    private <R extends LoopLimitContext> void startAsyncChatLoop(
            StreamingChatModel model,
            List<ChatMessage> messages,
            ToolExecutor<R> toolExecutor,
            StreamingChatListener<R> listener,
            R context) {

        // 检查循环限制
        if (context != null && context.isLoopLimitReached()) {
            log.info("对话已达到最大循环限制（{}次），为防止无限循环，对话将在此结束。", context.getMaxLoopCount());
            AiMessage finalMessage = AiMessage.from(
                    String.format("对话已达到最大循环限制（%d次），为防止无限循环，对话将在此结束。",
                            context.getMaxLoopCount())
            );
            if (listener != null) {
                listener.onStreamingComplete(finalMessage, context);
            }
            return;
        }

        // 创建异步响应处理器，它会在AI响应完成后自动决定是否继续下一轮
        ToolCallingResponseHandler<R> handler = new ToolCallingResponseHandler<>(
                model, messages, toolExecutor, listener, context, this::startAsyncChatLoop);



        // 启动异步AI对话，不阻塞当前线程
        try {
            model.chat(messages, handler);
        } catch (Exception e) {
            log.error("启动AI对话时发生异常", e);
            if (listener != null) {
                listener.onError(e, context);
                listener.onStreamingComplete(AiMessage.from("AI对话启动失败：" + e.getMessage()), context);
            }
        }
    }

    /**
     * 异步聊天循环回调接口。
     * 用于在AI响应完成后决定是否继续下一轮对话。
     */
    @FunctionalInterface
    private interface AsyncChatLoopCallback<R extends LoopLimitContext> {
        void continueLoop(StreamingChatModel model, List<ChatMessage> messages,
                          ToolExecutor<R> toolExecutor, StreamingChatListener<R> listener, R context);
    }

    /**
     * 工具调用响应处理器内部类。
     * 这是一个自定义的 StreamingChatResponseHandler，用于协调工具调用的循环处理。
     * 它负责：
     * 1. 接收AI模型的流式响应
     * 2. 处理工具调用请求
     * 3. 逐个确认并执行工具
     * 4. 将工具执行结果反馈给AI模型
     * 5. 重复上述过程直到对话完成
     */
    private static class ToolCallingResponseHandler<R extends LoopLimitContext> implements StreamingChatResponseHandler {
        private final StreamingChatModel model;
        private final List<ChatMessage> messages;
        private final ToolExecutor<R> toolExecutor;
        private final StreamingChatListener<R> listener;
        private final R context;
        private final AsyncChatLoopCallback<R> loopCallback;
        private volatile boolean hasThinkingCompleted = false;


        public ToolCallingResponseHandler(StreamingChatModel model, List<ChatMessage> messages,
                                          ToolExecutor<R> toolExecutor, StreamingChatListener<R> listener,
                                          R context, AsyncChatLoopCallback<R> loopCallback) {
            this.model = model;
            this.messages = messages;
            this.toolExecutor = toolExecutor;
            this.listener = listener;
            this.context = context;
            this.loopCallback = loopCallback;
        }


        // 注意：以下方法可能在当前langchain4j版本中不存在
        // 如果需要支持思考模式，需要根据实际版本调整实现
        @Override
        public void onPartialThinking(PartialThinking partialThinking) {
            if (listener != null && partialThinking != null && partialThinking.text() != null && !partialThinking.text().isEmpty()) {
                listener.onThinkingToken(partialThinking.text(), context);
                // 标记正在思考中
                hasThinkingCompleted = false;
            }
        }

        /**
         * 处理AI模型生成的部分响应。
         * 当AI模型以流式方式生成文本时，每个文本片段都会触发此方法。
         * 这使得调用方可以实时显示AI正在生成的内容，提供更好的用户体验。
         *
         * @param partialResponse AI模型生成的部分文本响应
         */
        @Override
        public void onPartialResponse(String partialResponse) {
            if (listener != null && partialResponse != null && !partialResponse.isEmpty()) {
                listener.onContentToken(partialResponse, context);
            }
        }


        /**
         * 处理AI模型的完整响应。
         * 当AI模型完成一次完整的响应生成时调用此方法。
         * 根据完成原因决定是继续工具调用流程还是结束对话。
         *
         * @param completeResponse 包含AI消息和完成原因的完整响应对象
         */
        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            // 提取AI消息和完成原因
            AiMessage aiMessage = completeResponse.aiMessage();
            FinishReason finishReason = completeResponse.finishReason();

            // 将AI消息添加到对话历史中，用于维持上下文
            messages.add(aiMessage);

            // 根据不同的完成原因进行不同的处理
            if (finishReason == FinishReason.TOOL_EXECUTION) {
                // 工具执行情况：先通知完整消息（携带FinishReason），让监听器决定如何处理
                if (listener != null) {
                    ContentDecision contentDecision = listener.onCompleteMessage(aiMessage, completeResponse.tokenUsage(), finishReason, context);
                    if (contentDecision == ContentDecision.TERMINATE) {
                        // 监听器决定终止对话
                        listener.onStreamingComplete(aiMessage, context);
                        return;
                    }
                }

                // 继续处理工具执行逻辑
                handleToolExecutionAsync(aiMessage, completeResponse.tokenUsage());
            } else if (finishReason == FinishReason.STOP || finishReason == FinishReason.LENGTH) {
                // 如果之前有思考过程，先通知思考完成
                if (hasThinkingCompleted && listener != null) {
                    ThinkingDecision thinkingDecision = listener.onThinkingComplete(context);
                    if (thinkingDecision == ThinkingDecision.TERMINATE) {
                        // 终止对话
                        listener.onStreamingComplete(aiMessage, context);
                        return;
                    }
                }

                // 正常结束或长度限制：通知完整消息，然后结束流式响应
                if (listener != null) {
                    ContentDecision contentDecision = listener.onCompleteMessage(aiMessage, completeResponse.tokenUsage(), finishReason, context);
                    if (contentDecision == ContentDecision.TERMINATE) {
                        // 终止对话
                        listener.onStreamingComplete(aiMessage, context);
                        return;
                    }
                }
                // 对话自然结束
                if (listener != null) {
                    listener.onStreamingComplete(aiMessage, context);
                }
            } else {
                // 其他情况（如错误）：通知完整消息后结束
                if (listener != null) {
                    listener.onCompleteMessage(aiMessage, completeResponse.tokenUsage(), finishReason, context);
                    listener.onStreamingComplete(aiMessage, context);
                }
            }
        }

        /**
         * 异步处理AI消息中的工具执行请求。
         * 当AI决定需要调用工具时，此方法会被触发。
         * 它会遍历所有请求的工具，根据监听器的决策执行或跳过工具，并处理执行结果。
         * 完成后会根据监听器的决策决定是否继续下一轮对话。
         *
         * @param aiMessage  包含工具执行请求的AI消息
         * @param tokenUsage Token使用情况
         */
        private void handleToolExecutionAsync(AiMessage aiMessage, TokenUsage tokenUsage) {
            // 从AI消息中提取工具执行请求列表
            List<ToolExecutionRequest> toolExecutionRequests = aiMessage.toolExecutionRequests();

            // 如果没有工具请求，则直接结束对话
            if (toolExecutionRequests == null || toolExecutionRequests.isEmpty()) {
                if (listener != null) {
                    listener.onStreamingComplete(aiMessage, context);
                }
                return;
            }

            // 初始化批处理决策为继续执行
            ToolBatchDecision batchDecision = ToolBatchDecision.PROCEED;
            // 如果有监听器，则通知收到完整工具列表，并获取批处理决策
            if (listener != null) {
                batchDecision = listener.onReceiveCompleteToolList(toolExecutionRequests, tokenUsage, context);
            }
            // 如果批处理决策是延迟并关闭，则直接结束对话
            if (batchDecision == ToolBatchDecision.DEFER_AND_CLOSE) {
                listener.onStreamingComplete(aiMessage, context);
                return;
            }

            // 遍历每个工具执行请求
            for (ToolExecutionRequest toolExecutionRequest : toolExecutionRequests) {
                // 初始化单个工具的执行决策为执行
                ToolDecision shouldExecute = ToolDecision.EXECUTE;
                // 如果有监听器，则通知单个工具执行请求，并获取执行决策
                if (listener != null) {
                    shouldExecute = listener.onToolExecutionStart(toolExecutionRequest, context);
                }

                // 根据执行决策采取相应行动
                if (shouldExecute == ToolDecision.EXECUTE) {
                    // 安全地执行工具并获取结果，如果执行失败，它依旧会返回结果，并将错误信息放置在错误字段中
                    ToolExecutionResult result = executeToolSafely(toolExecutionRequest, context);

                    // 如果执行结果不为空，则将其添加到消息历史中，并通知监听器
                    if (result != null) {
                        messages.add(result.toToolExecutionResultMessage());

                        // 通知监听器工具执行完成，传递工具执行结果、工具执行请求和上下文
                        if (listener != null) {
                            ToolContinueDecision continueDecision = listener.onToolExecutionFinish(result, context);
                            if (continueDecision == ToolContinueDecision.TERMINATE) {
                                log.debug("监听器决定终止工具执行流程");
                                listener.onStreamingComplete(aiMessage, context);
                                return;
                            }
                        }
                    }
                } else if (shouldExecute == ToolDecision.SKIP) {
                    ToolExecutionResult skipped = ToolExecutionResult.skipped(toolExecutionRequest, "工具执行被用户取消");
                    messages.add(skipped.toToolExecutionResultMessage());
                    ToolContinueDecision continueDecision = listener.onToolExecutionFinish(skipped, context);
                    if (continueDecision == ToolContinueDecision.TERMINATE) {
                        log.debug("监听器决定终止工具继续执行流程");
                        listener.onStreamingComplete(aiMessage, context);
                        return;
                    }
                } else {
                    // DEFER_AND_CLOSE
                    // 如果决策是延迟并关闭，则直接结束对话
                    listener.onStreamingComplete(aiMessage, context);
                    return;
                }
            }

            // 所有工具执行完毕后，通知监听器并根据决策决定是否继续
            if (listener != null) {
                LoopDecision loopDecision = listener.onAllToolsComplete(context);
                if (loopDecision == LoopDecision.CONTINUE_LOOP) {
                    // 递增循环计数器
                    if (context != null) {
                        context.incrementLoopCount();
                        log.info("继续下一轮对话，当前循环计数：{}", context.getLoopCounter().get());
                    }
                    // 异步启动下一轮对话
                    loopCallback.continueLoop(model, messages, toolExecutor, listener, context);
                } else {
                    // 结束对话
                    listener.onStreamingComplete(aiMessage, context);
                }
            } else {
                // 没有监听器时，默认结束对话
                // 这种情况下无法获取到最终的AI消息，使用当前消息作为结束标志
                log.warn("没有监听器，无法继续对话流程，强制结束");
            }
        }


        /**
         * 处理过程中发生错误时的回调。
         * 当对话处理过程中发生错误时，此方法会被调用。
         * 它会通知监听器发生了错误，并结束对话流程。
         *
         * @param error 发生的错误
         */
        @Override
        public void onError(Throwable error) {
            // 如果有监听器，则通知错误发生
            if (listener != null) {
                listener.onError(error, context);
                // 错误发生时结束对话流程
                listener.onStreamingComplete(AiMessage.from("对话因错误而终止：" + error.getMessage()), context);
            }
        }

        /**
         * 安全执行工具的方法。
         * 此方法尝试执行给定的工具请求，并处理任何可能发生的异常。
         * 如果执行成功，它会返回工具执行结果；如果执行失败，它会抛出运行时异常。
         *
         * @param toolExecutionRequest 要执行的工具请求
         * @param context              执行上下文
         * @return 工具执行结果
         * @throws RuntimeException 如果执行过程中发生异常
         */

        private ToolExecutionResult executeToolSafely(ToolExecutionRequest toolExecutionRequest, R context) {
            long start = System.currentTimeMillis();
            ToolExecutionResult result;
            try {
                result = toolExecutor.execute(toolExecutionRequest, context);
                if (result != null) {
                    result.setExecutionTimeMs(System.currentTimeMillis() - start);
                    if (result.getRequest() == null) {
                        result.setRequest(toolExecutionRequest);
                    }

                }
            } catch (Exception e) {
                result = ToolExecutionResult.error(toolExecutionRequest, e.getMessage());
            }
            return result;
        }
    }
}
