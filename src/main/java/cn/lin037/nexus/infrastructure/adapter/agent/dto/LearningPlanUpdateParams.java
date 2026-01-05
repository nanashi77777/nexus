package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import lombok.Data;

/**
 * 学习计划修改参数
 * 用于更新单条学习计划的内容（标题、目标、难度）。
 * 功能与约定：
 * - planId 为必填
 * - 其他字段为可选，传空表示不更新该项
 */
@Data
public class LearningPlanUpdateParams {

    /**
     * 学习计划ID（必填）
     * 对应 AgentLearningTaskEntity.altId
     */
    private Long planId;

    /**
     * 新的标题（可选）
     * 对应 AgentLearningTaskEntity.altTitle
     */
    private String title;

    /**
     * 新的学习目标（可选）
     * 对应 AgentLearningTaskEntity.altObjective
     */
    private String objective;

    /**
     * 新的难度等级（可选）
     * 对应 AgentLearningTaskEntity.altDifficultyLevel
     */
    private AgentLearningDifficultyEnum difficultyLevel;

    /**
     * 是否标记为已完成（true=完成，false=未完成）
     * 对应 AgentLearningTaskEntity.altIsCompleted
     */
    private Boolean completed;
}