package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 记忆删除参数
 * 用于 Agent 工具中删除用户记忆时的参数传递。
 * <p>
 * 功能与约定：
 * - 传入需要删除的记忆ID
 */
@Data
public class MemoryDeleteParams {

    /**
     * 记忆ID（必填）
     * 对应 AgentMemoryEntity.amId
     */
    private Long memoryId;
}