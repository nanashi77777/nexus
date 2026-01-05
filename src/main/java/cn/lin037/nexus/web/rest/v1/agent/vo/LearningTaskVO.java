package cn.lin037.nexus.web.rest.v1.agent.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent学习任务VO
 *
 * @author Lin037
 */
@Data
public class LearningTaskVO {

    /**
     * 学习任务ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习空间ID
     */
    private Long learningSpaceId;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 规划标题
     */
    private String title;

    /**
     * 学习目标
     */
    private String objective;

    /**
     * 难度评估
     * 1: 初级 (BEGINNER)
     * 2: 中级 (INTERMEDIATE)
     * 3: 高级 (ADVANCED)
     * 4: 专家级 (EXPERT)
     */
    private AgentLearningDifficultyEnum difficultyLevel;

    /**
     * 是否完成
     */
    private Boolean isCompleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从Entity转换为VO
     *
     * @param entity AgentLearningTaskEntity实体
     * @return LearningTaskVO
     */
    public static LearningTaskVO fromEntity(AgentLearningTaskEntity entity) {
        if (entity == null) {
            return null;
        }
        
        LearningTaskVO vo = new LearningTaskVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setId(entity.getAltId());
        vo.setUserId(entity.getAltUserId());
        vo.setLearningSpaceId(entity.getAltLearningSpaceId());
        vo.setSessionId(entity.getAltSessionId());
        vo.setTitle(entity.getAltTitle());
        vo.setObjective(entity.getAltObjective());
        vo.setDifficultyLevel(AgentLearningDifficultyEnum.fromCode(entity.getAltDifficultyLevel()));
        vo.setIsCompleted(entity.getAltIsCompleted());
        vo.setCreatedAt(entity.getAltCreatedAt());
        vo.setUpdatedAt(entity.getAltUpdatedAt());
        return vo;
    }
}