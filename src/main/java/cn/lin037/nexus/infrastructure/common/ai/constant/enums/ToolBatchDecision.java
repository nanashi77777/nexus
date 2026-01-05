package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 工具批次执行决策枚举
 * 用于在工具执行开始前，让外部系统做出批次级别的决策
 *
 * @author Lin037
 */
public enum ToolBatchDecision {

    /**
     * 继续处理 - 按照正常流程逐个处理工具执行请求
     * 每个工具仍会通过 onToolExecutionRequest 方法进行单独确认
     */
    PROCEED,

    /**
     * 延迟并关闭 - 记录所有工具信息并立即关闭当前响应
     * 外部系统可以获取工具信息进行记录，然后关闭当前流式响应
     * 后续可通过独立的API触发工具执行
     */
    DEFER_AND_CLOSE
}