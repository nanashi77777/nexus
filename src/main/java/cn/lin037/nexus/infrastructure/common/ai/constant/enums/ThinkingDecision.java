package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 思考响应决策枚举
 * 用于在思考响应结束时，让外部系统做出决策
 *
 * @author Lin037
 */
public enum ThinkingDecision {

    /**
     * 继续处理 - 按照正常流程继续处理后续内容
     */
    CONTINUE,

    /**
     * 终止响应 - 立即终止当前流式响应
     */
    TERMINATE
}