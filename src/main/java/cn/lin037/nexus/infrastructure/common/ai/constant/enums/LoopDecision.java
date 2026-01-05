package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 循环决策枚举
 * 用于在所有工具执行完成后，让外部系统决定是否继续下一轮循环
 *
 * @author Lin037
 */
public enum LoopDecision {

    /**
     * 继续循环 - 发送下一次请求，继续对话循环
     */
    CONTINUE_LOOP,

    /**
     * 结束响应 - 终止当前流式响应，完成对话
     */
    END_RESPONSE
}