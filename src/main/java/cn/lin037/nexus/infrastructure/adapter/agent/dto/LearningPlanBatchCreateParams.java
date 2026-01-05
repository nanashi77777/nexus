package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import lombok.Data;

import java.util.List;

/**
 * 学习计划批量创建参数
 * 用于一次性创建多个学习计划，字段与 AgentLearningTaskEntity 语义保持一致。
 * 功能与约定：
 * - 支持在同一会话/学习空间下批量创建多条记录
 * - items 为必填，列表中每个元素描述一条计划
 */
@Data
public class LearningPlanBatchCreateParams {

    /**
     * 待创建的学习计划项列表（必填）
     */
    private List<CreateItem> items;

    /**
     * 单条学习计划创建项
     */
    @Data
    public static class CreateItem {
        /**
         * 规划标题（必填）
         * 对应 AgentLearningTaskEntity.altTitle
         */
        private String title;

        /**
         * 学习目标（必填）
         * 对应 AgentLearningTaskEntity.altObjective
         */
        private String objective;

        /**
         * 难度评估（可选，默认 INTERMEDIATE）
         * 对应 AgentLearningTaskEntity.altDifficultyLevel
         */
        private AgentLearningDifficultyEnum difficultyLevel = AgentLearningDifficultyEnum.INTERMEDIATE;
    }
}