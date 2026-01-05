package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 学习计划完成状态更新参数
 * 用于将计划标记为已完成或取消完成。
 * 功能与约定：
 * - completed = true 表示打勾（完成），false 表示取消完成
 * - completedByMessageId 可选，仅在完成时用于记录由哪条消息触发
 */
@Data
public class LearningPlanCompletionParams {

    /**
     * 学习计划ID（必填）
     * 对应 AgentLearningTaskEntity.altId
     */
    private Long planId;

    /**
     * 是否标记为已完成（true=完成，false=取消完成）
     * 对应 AgentLearningTaskEntity.altIsCompleted
     */
    private Boolean isCompleted;
}