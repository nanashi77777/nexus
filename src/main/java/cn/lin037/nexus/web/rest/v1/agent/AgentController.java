package cn.lin037.nexus.web.rest.v1.agent;

import cn.lin037.nexus.application.agent.service.AgentAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.agent.req.*;
import cn.lin037.nexus.web.rest.v1.agent.vo.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * Agent控制器
 *
 * @author Lin037
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final AgentAppService agentAppService;

    public AgentController(AgentAppService agentAppService) {
        this.agentAppService = agentAppService;
    }

    // ==================== Session相关接口 ====================

    /**
     * 创建会话
     *
     * @param req 创建会话请求
     * @return 新创建的会话
     */
    @PostMapping("/sessions")
    public ResultVO<SessionVO> createSession(@Valid @RequestBody CreateSessionReq req) {
        SessionVO session = agentAppService.createSession(req);
        return ResultVO.success(session);
    }

    /**
     * 更新会话
     *
     * @param sessionId 会话ID
     * @param req       更新会话请求
     * @return 更新后的会话
     */
    @PutMapping("/sessions/{sessionId}")
    public ResultVO<SessionVO> updateSession(@PathVariable Long sessionId, @Valid @RequestBody UpdateSessionReq req) {
        SessionVO session = agentAppService.updateSession(sessionId, req);
        return ResultVO.success(session);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 删除成功
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResultVO<Void> deleteSession(@PathVariable Long sessionId) {
        agentAppService.deleteSession(sessionId);
        return ResultVO.success();
    }

    // ==================== Memory相关接口 ====================

    /**
     * 创建记忆
     *
     * @param req 创建记忆请求
     * @return 新创建的记忆
     */
    @PostMapping("/memories")
    public ResultVO<MemoryVO> createMemory(@Valid @RequestBody CreateMemoryReq req) {
        MemoryVO memory = agentAppService.createMemory(req);
        return ResultVO.success(memory);
    }

    /**
     * 更新记忆
     *
     * @param memoryId 记忆ID
     * @param req      更新记忆请求
     * @return 更新后的记忆
     */
    @PutMapping("/memories/{memoryId}")
    public ResultVO<MemoryVO> updateMemory(@PathVariable Long memoryId, @Valid @RequestBody UpdateMemoryReq req) {
        MemoryVO memory = agentAppService.updateMemory(memoryId, req);
        return ResultVO.success(memory);
    }

    /**
     * 删除记忆
     *
     * @param memoryId 记忆ID
     * @return 删除成功
     */
    @DeleteMapping("/memories/{memoryId}")
    public ResultVO<Void> deleteMemory(@PathVariable Long memoryId) {
        agentAppService.deleteMemory(memoryId);
        return ResultVO.success();
    }

    // ==================== LearningTask相关接口 ====================

    /**
     * 创建学习任务
     *
     * @param req 创建学习任务请求
     * @return 新创建的学习任务
     */
    @PostMapping("/learning-tasks")
    public ResultVO<LearningTaskVO> createLearningTask(@Valid @RequestBody CreateLearningTaskReq req) {
        LearningTaskVO learningTask = agentAppService.createLearningTask(req);
        return ResultVO.success(learningTask);
    }

    /**
     * 更新学习任务
     *
     * @param taskId 学习任务ID
     * @param req    更新学习任务请求
     * @return 更新后的学习任务
     */
    @PutMapping("/learning-tasks/{taskId}")
    public ResultVO<LearningTaskVO> updateLearningTask(@PathVariable Long taskId, @Valid @RequestBody UpdateLearningTaskReq req) {
        LearningTaskVO learningTask = agentAppService.updateLearningTask(taskId, req);
        return ResultVO.success(learningTask);
    }

    /**
     * 删除学习任务
     *
     * @param taskId 学习任务ID
     * @return 删除成功
     */
    @DeleteMapping("/learning-tasks/{taskId}")
    public ResultVO<Void> deleteLearningTask(@PathVariable Long taskId) {
        agentAppService.deleteLearningTask(taskId);
        return ResultVO.success();
    }

}
