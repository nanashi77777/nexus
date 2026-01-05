package cn.lin037.nexus.infrastructure.common.persistent.entity.agent;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Ignore;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent学习规划记录实体
 * 用于记录学习会话中的学习任务，一条记录为一个任务
 *
 * @author Lin037
 */
@Data
@FieldNameConstants
@Table("agent_learning_tasks")
public class AgentLearningTaskEntity implements Serializable {

    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 学习任务ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long altId;

    /**
     * 用户ID
     */
    private Long altUserId;

    /**
     * 学习空间ID
     */
    private Long altLearningSpaceId;

    /**
     * 所属会话ID
     */
    private Long altSessionId;

    /**
     * 规划标题
     */
    private String altTitle;

    /**
     * 学习目标
     */
    private String altObjective;

    /**
     * 难度评估（AI评估）
     * @see AgentLearningDifficultyEnum
     * 1: 初级 (BEGINNER)
     * 2: 中级 (INTERMEDIATE)
     * 3: 高级 (ADVANCED)
     * 4: 专家级 (EXPERT)
     */
    private Integer altDifficultyLevel;

    /**
     * 是否完成 (false=未完成, true=已完成)
     */
    private Boolean altIsCompleted;

    /**
     * 创建时间
     */
    private LocalDateTime altCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime altUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime altDeletedAt;
}