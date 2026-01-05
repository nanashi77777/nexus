package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 内容响应决策枚举
 * 用于在完整消息返回时，让外部系统做出决策
 *
 * @author Lin037
 */
public enum ContentDecision {

    /**
     * 继续处理 - 按照正常流程继续处理
     */
    CONTINUE,

    /**
     * 终止响应 - 立即终止当前流式响应
     */
    TERMINATE
}