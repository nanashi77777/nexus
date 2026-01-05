package cn.lin037.nexus.web.rest.v1.agent.req;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建Agent学习任务请求
 *
 * @author Lin037
 */
@Data
public class CreateLearningTaskReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 所属会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 规划标题
     */
    @NotBlank(message = "规划标题不能为空")
    @Size(max = 200, message = "规划标题长度不能超过200个字符")
    private String title;

    /**
     * 学习目标
     */
    @NotBlank(message = "学习目标不能为空")
    @Size(max = 2000, message = "学习目标长度不能超过2000个字符")
    private String objective;

    /**
     * 难度评估
     * 1: 初级 (BEGINNER)
     * 2: 中级 (INTERMEDIATE)
     * 3: 高级 (ADVANCED)
     * 4: 专家级 (EXPERT)
     */
    private AgentLearningDifficultyEnum difficultyLevel;
}