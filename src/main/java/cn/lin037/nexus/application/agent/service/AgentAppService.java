package cn.lin037.nexus.application.agent.service;

import cn.lin037.nexus.web.rest.v1.agent.req.*;
import cn.lin037.nexus.web.rest.v1.agent.vo.*;

import java.util.List;

/**
 * Agent应用服务接口
 *
 * @author Lin037
 */
public interface AgentAppService {

    // ==================== Session相关方法 ====================

    /**
     * 创建会话
     *
     * @param req 创建会话请求
     * @return 会话VO
     */
    SessionVO createSession(CreateSessionReq req);

    /**
     * 更新会话
     *
     * @param id 会话ID
     * @param req 更新会话请求
     * @return 会话VO
     */
    SessionVO updateSession(Long id, UpdateSessionReq req);

    /**
     * 删除会话
     *
     * @param id 会话ID
     */
    void deleteSession(Long id);

    // ==================== Memory相关方法 ====================

    /**
     * 创建记忆
     *
     * @param req 创建记忆请求
     * @return 记忆VO
     */
    MemoryVO createMemory(CreateMemoryReq req);

    /**
     * 更新记忆
     *
     * @param id 记忆ID
     * @param req 更新记忆请求
     * @return 记忆VO
     */
    MemoryVO updateMemory(Long id, UpdateMemoryReq req);

    /**
     * 删除记忆
     *
     * @param id 记忆ID
     */
    void deleteMemory(Long id);

    // ==================== LearningTask相关方法 ====================

    /**
     * 创建学习任务
     *
     * @param req 创建学习任务请求
     * @return 学习任务VO
     */
    LearningTaskVO createLearningTask(CreateLearningTaskReq req);

    /**
     * 更新学习任务
     *
     * @param id 学习任务ID
     * @param req 更新学习任务请求
     * @return 学习任务VO
     */
    LearningTaskVO updateLearningTask(Long id, UpdateLearningTaskReq req);

    /**
     * 删除学习任务
     *
     * @param id 学习任务ID
     */
    void deleteLearningTask(Long id);

}
