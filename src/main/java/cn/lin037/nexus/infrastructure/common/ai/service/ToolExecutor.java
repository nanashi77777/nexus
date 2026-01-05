package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

/**
 * AI 模型请求的工具执行器接口
 * 该接口定义了执行工具的标准方法，支持泛型上下文参数，
 * 允许应用层传入自定义的上下文对象进行权限控制或数据过滤
 * 适配新的循环处理架构
 *
 * @param <T> 上下文类型，可以是任何自定义的上下文对象
 * @author Lin037
 */
public interface ToolExecutor<T> {

    /**
     * Executes a tool with additional execution context.
     * 说明：
     * - 在工具调用需要基于当前业务上下文（如用户ID、学习空间ID、会话ID）进行权限控制或数据过滤时，
     *   建议优先使用此方法。
     * - 默认实现会回退到无上下文的 execute(request) 方法以保证兼容性。
     * - 泛型参数 T 允许应用层传入自定义的上下文对象，实现解耦和灵活性。
     *
     * @param request 工具执行请求
     * @param context 工具执行上下文，由应用层定义具体类型
     * @return 工具执行结果（统一 DTO）
     */
    ToolExecutionResult execute(ToolExecutionRequest request, T context);
}
