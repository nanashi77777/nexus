package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.DefaultLoopLimitContext;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.LoopLimitContext;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 支持工具调用的流式聊天对话服务接口。
 * 提供与AI模型进行流式对话的核心功能，并支持工具调用的完整生命周期管理。
 *
 * @author Lin037
 */
public interface StreamingChatToolService {

    /**
     * 启动与AI模型的流式聊天对话。
     * 该方法会建立一个持续的对话通道，支持文本流式响应和工具调用交互。
     *
     * @param modelName    AI模型名称（例如："gpt-4o-mini"）
     * @param usedFor      模型使用场景（例如："customer-support"）
     * @param messages     对话历史消息列表
     * @param toolExecutor 工具执行器，用于处理模型发起的工具调用请求
     * @param listener     对话事件监听器，用于接收对话过程中的各种事件
     */
    void chat(String modelName, String usedFor, List<ChatMessage> messages, ToolExecutor<DefaultLoopLimitContext> toolExecutor, StreamingChatListener<DefaultLoopLimitContext> listener);

    /**
     * 启动与AI模型的流式聊天对话（带泛型工具执行上下文）。
     * 设计说明：
     * - 为了在工具调用阶段访问业务上下文（如用户ID、学习空间ID、会话ID），引入该扩展方法。
     * - 默认实现回退到无上下文的chat方法，保持现有调用方兼容。
     *
     * @param modelName    AI模型名称
     * @param usedFor      模型使用场景
     * @param messages     对话历史消息列表
     * @param toolExecutor 工具执行器
     * @param listener     事件监听器
     * @param context      泛型工具执行上下文
     * @param <T>          工具执行上下文类型，必须实现LoopLimitContext接口
     */
    <T extends LoopLimitContext> void chat(String modelName, String usedFor, List<ChatMessage> messages,
                                                ToolExecutor<T> toolExecutor, StreamingChatListener<T> listener, T context);
}
