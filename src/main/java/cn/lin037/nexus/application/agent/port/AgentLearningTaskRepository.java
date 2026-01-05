package cn.lin037.nexus.application.agent.port;

import cn.lin037.nexus.web.rest.v1.agent.req.AgentLearningTaskPageQuery;
import jakarta.validation.constraints.NotNull;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Agent学习任务Repository接口
 * 提供Agent学习任务的管理操作
 *
 * @author Lin037
 */
public interface AgentLearningTaskRepository {

    /**
     * 保存学习任务
     *
     * @param entity 学习任务实体
     */
    void save(AgentLearningTaskEntity entity);

    /**
     * 根据ID查询学习任务
     *
     * @param id 学习任务ID
     * @return 学习任务实体
     */
    Optional<AgentLearningTaskEntity> findById(Long id);

    /**
     * 根据ID查询学习任务
     *
     * @param id 学习任务ID
     * @param getters 需要查询的字段
     * @return 学习任务实体
     */
    Optional<AgentLearningTaskEntity> findById(Long id, List<Getter<AgentLearningTaskEntity>> getters);

    /**
     * 根据会话ID查询学习任务列表
     *
     * @param sessionId 会话ID
     * @return 学习任务列表
     */
    List<AgentLearningTaskEntity> findBySessionId(Long sessionId);

    /**
     * 根据会话ID查询学习任务列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 学习任务列表
     */
    List<AgentLearningTaskEntity> findBySessionId(Long sessionId, List<Getter<AgentLearningTaskEntity>> getters);

    /**
     * 根据用户ID和学习空间ID查询学习任务列表
     *
     * @param userId 用户ID
     * @param learningSpaceId 学习空间ID
     * @param getters 需要查询的字段
     * @return 学习任务列表
     */
    List<AgentLearningTaskEntity> findByUserIdAndLearningSpaceId(Long userId, Long learningSpaceId, List<Getter<AgentLearningTaskEntity>> getters);

    /**
     * 更新学习任务
     *
     * @param entity 学习任务实体
     */
    void updateById(AgentLearningTaskEntity entity);

    /**
     * 更新学习任务完成状态
     *
     * @param id              学习任务ID
     * @param isCompleted     是否完成
     * @param completedByMessageId 完成消息ID
     * @return 是否更新成功
     */
    boolean updateCompletionStatus(Long id, Integer isCompleted, Long completedByMessageId);

    /**
     * 删除学习任务
     *
     * @param id 学习任务ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据会话ID删除学习任务
     *
     * @param sessionId 会话ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * 检查学习任务是否存在
     *
     * @param id 学习任务ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 分页查询学习任务
     *
     * @param userId 用户ID
     * @param query  分页查询参数
     * @return 分页结果
     */
    PageResult<AgentLearningTaskEntity> findPageByUserId(@NotNull Long userId, @NotNull AgentLearningTaskPageQuery query);
}