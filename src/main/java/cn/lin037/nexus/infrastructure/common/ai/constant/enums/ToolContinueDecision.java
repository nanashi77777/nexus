package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 工具完成后继续决策枚举
 * 用于在单个工具执行完成后，让外部系统决定是否继续处理
 *
 * @author Lin037
 */
public enum ToolContinueDecision {

    /**
     * 继续处理 - 继续处理后续工具或流程
     */
    CONTINUE,

    /**
     * 终止响应 - 立即终止当前流式响应
     */
    TERMINATE
}