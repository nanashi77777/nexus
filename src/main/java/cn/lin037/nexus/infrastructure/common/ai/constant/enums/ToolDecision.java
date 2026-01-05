package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

/**
 * 单个工具执行决策枚举
 * 用于在单个工具执行前，由外部系统返回明确的三态决策：执行/跳过/延迟并关闭
 * EXECUTE: 立即执行该工具
 * SKIP: 跳过该工具执行
 * DEFER_AND_CLOSE: 暂停当前流程并立即关闭响应，后续由外部系统自行触发工具调用
 */
public enum ToolDecision {
    EXECUTE,
    SKIP,
    DEFER_AND_CLOSE
}