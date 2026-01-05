package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 记忆新增参数
 * 用于 Agent 工具中创建用户记忆时的参数传递，字段名称与 AgentMemoryEntity 的语义保持一致。
 * <p>
 * 功能与约定：
 * - 支持设置记忆的上下文（学习空间 / 会话）
 * - 支持指定记忆等级（0=不启用，1=会话级，2=全局），默认 1
 * - 支持设置重要性、标签及来源
 * - content 为必填项，由调用方在进入应用服务前完成非空校验
 */
@Data
public class MemoryAddParams {

    /**
     * 记忆标题（可选）
     * 对应 AgentMemoryEntity.amTitle
     */
    private String title;

    /**
     * 记忆内容（必填）
     * 对应 AgentMemoryEntity.amContent
     */
    private String content;

    /**
     * 重要性评分（1-10），默认 5
     * 对应 AgentMemoryEntity.amImportanceScore
     */
    private Integer importanceScore = 5;
}