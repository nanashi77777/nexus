package cn.lin037.nexus.web.rest.v1.agent.req;

import cn.lin037.nexus.common.model.query.BasePageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent学习任务分页查询参数类
 * 继承基础分页查询参数，添加学习任务相关的筛选条件
 *
 * @author Lin037
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent学习任务分页查询参数类")
public class AgentLearningTaskPageQuery extends BasePageQuery {

    /**
     * 学习空间ID
     */
    @Schema(description = "学习空间ID")
    private Long learningSpaceId;

    /**
     * 所属会话ID
     */
    @Schema(description = "所属会话ID")
    private Long sessionId;

    /**
     * 难度评估
     * 1: 初级 (BEGINNER)
     * 2: 中级 (INTERMEDIATE)
     * 3: 高级 (ADVANCED)
     * 4: 专家级 (EXPERT)
     */
    @Schema(description = "难度评估")
    private Integer difficultyLevel;

    /**
     * 是否完成 (false=未完成, true=已完成)
     */
    @Schema(description = "是否完成")
    private Boolean isCompleted;
}