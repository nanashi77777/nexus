package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

import java.util.List;

/**
 * 学习计划批量删除参数
 * 用于一次性删除多个学习计划。
 * <p>
 * 功能与约定：
 * - 传入待删除计划ID列表
 */
@Data
public class LearningPlanBatchDeleteParams {

    /**
     * 要删除的学习计划ID集合（必填）
     * 对应 AgentLearningTaskEntity.altId
     */
    private List<Long> planIds;
}