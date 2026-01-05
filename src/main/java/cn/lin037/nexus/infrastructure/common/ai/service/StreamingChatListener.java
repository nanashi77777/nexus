package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.*;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;

/**
 * 增强的流式聊天对话事件监听器接口
 * 支持细化的事件类型：思考token、内容token、完整消息、工具列表等
 * 所有方法均提供 default 实现，便于使用方按需覆写，避免强制实现全部回调。
 *
 * @author Lin037
 */
public interface StreamingChatListener<T> {

    /**
     * 当 AI 模型生成思考内容的文本片段时调用
     * 函数职责：在流式响应过程中接收思考部分的文本片段，可用于实时渲染思考过程
     * 默认实现：不做任何处理
     *
     * @param token   思考内容的文本片段
     * @param context 执行上下文，包含用户ID、会话ID等信息
     */
    default void onThinkingToken(String token, T context) {
        // 默认空实现
    }

    /**
     * 当思考响应结束时调用
     * 函数职责：通知思考部分已完成，外部系统可决定是否继续处理
     * 默认实现：返回 CONTINUE（继续处理）
     *
     * @param context 执行上下文
     * @return 思考决策：CONTINUE 表示继续处理，TERMINATE 表示终止响应
     */
    default ThinkingDecision onThinkingComplete(T context) {
        return ThinkingDecision.CONTINUE;
    }

    /**
     * 当 AI 模型生成内容的文本片段时调用
     * 函数职责：在流式响应过程中接收内容部分的文本片段，可用于实时渲染内容
     * 默认实现：不做任何处理
     *
     * @param token   内容的文本片段
     * @param context 执行上下文，包含用户ID、会话ID等信息
     */
    default void onContentToken(String token, T context) {
        // 默认空实现
    }

    /**
     * 当收到完整的消息时调用
     * 函数职责：在收到 AI 的完整消息后进行处理，允许外部决定是否继续或终止响应
     * 默认实现：返回 CONTINUE（继续处理）
     *
     * @param aiMessage    完整的 AI 消息对象
     * @param tokenUsage   消息的 token 使用情况
     * @param finishReason AI 响应的完成原因（STOP、LENGTH、TOOL_EXECUTION等）
     * @param context      执行上下文
     * @return 内容决策：CONTINUE 表示继续处理，TERMINATE 表示终止响应
     */
    default ContentDecision onCompleteMessage(AiMessage aiMessage, TokenUsage tokenUsage, FinishReason finishReason, T context) {
        return ContentDecision.CONTINUE;
    }

    /**
     * 当收到完整的工具列表时调用
     * 函数职责：在所有工具执行开始前通知外部即将开始工具执行流程，并允许给出批次级决策
     * 默认实现：返回 DEFER_AND_CLOSE（结束响应，等待授权）
     *
     * @param toolExecutionRequests 所有待执行的工具请求列表
     * @param context               执行上下文
     * @return 批次执行决策：PROCEED 表示继续处理，DEFER_AND_CLOSE 表示延迟并立即关闭响应
     */
    default ToolBatchDecision onReceiveCompleteToolList(List<ToolExecutionRequest> toolExecutionRequests, TokenUsage tokenUsage, T context) {
        return ToolBatchDecision.DEFER_AND_CLOSE;
    }

    /**
     * 在执行单个工具前调用
     * 函数职责：在执行单个工具之前进行授权或决策，可实现用户确认、权限控制等
     * 默认实现：返回 DEFER_AND_CLOSE（延迟并关闭，等待授权）
     *
     * @param toolExecutionRequest 待执行的工具请求
     * @param context              执行上下文
     * @return 工具执行决策：EXECUTE/ SKIP/ DEFER_AND_CLOSE
     */
    default ToolDecision onToolExecutionStart(ToolExecutionRequest toolExecutionRequest, T context) {
        return ToolDecision.DEFER_AND_CLOSE;
    }

    /**
     * 当单个工具执行完成并获得结果后调用
     * 函数职责：在工具执行完成后处理结果，如记录、转存或渲染
     * 默认实现：返回 CONTINUE（继续处理）
     *
     * @param result  工具执行的统一结果
     * @param context 执行上下文
     * @return 工具继续决策：CONTINUE 表示继续处理，TERMINATE 表示终止响应
     */
    default ToolContinueDecision onToolExecutionFinish(ToolExecutionResult result, T context) {
        return ToolContinueDecision.CONTINUE;
    }

    /**
     * 当所有工具列表执行完成时调用
     * 函数职责：在所有工具执行完成后，决定是否继续下一轮循环
     * 默认实现：返回 CONTINUE_LOOP（继续循环）
     *
     * @param context 执行上下文
     * @return 循环决策：CONTINUE_LOOP 表示继续下一轮循环，END_RESPONSE 表示结束响应
     */
    default LoopDecision onAllToolsComplete(T context) {
        return LoopDecision.CONTINUE_LOOP;
    }

    /**
     * 当流式响应完成时调用
     * 函数职责：在成功的对话轮次中，表示 AI 已生成最终响应且不需要进一步的处理
     * 默认实现：不做任何处理
     *
     * @param finalMessage 最终的 AI 消息响应
     * @param context      执行上下文
     */
    default void onStreamingComplete(AiMessage finalMessage, T context) {
        // 默认空实现
    }

    /**
     * 当处理过程中发生错误时调用
     * 函数职责：在流式响应、工具执行或其他阶段发生错误时进行统一处理
     * 默认实现：不做任何处理
     *
     * @param error   发生的错误异常
     * @param context 执行上下文
     */
    default void onError(Throwable error, T context) {
        // 默认空实现
    }
}
