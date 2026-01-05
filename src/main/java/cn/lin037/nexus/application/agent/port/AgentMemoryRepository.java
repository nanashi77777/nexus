package cn.lin037.nexus.application.agent.port;

import cn.lin037.nexus.web.rest.v1.agent.req.AgentMemoryPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.lin037.nexus.common.model.query.BasePageQuery;
import jakarta.validation.constraints.NotNull;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Agent记忆Repository接口
 * 提供Agent记忆记录的管理操作
 *
 * @author Lin037
 */
public interface AgentMemoryRepository {

    /**
     * 保存记忆记录
     *
     * @param entity 记忆实体
     */
    void save(AgentMemoryEntity entity);

    /**
     * 根据ID查询记忆记录
     *
     * @param id 记忆ID
     * @return 记忆实体
     */
    Optional<AgentMemoryEntity> findById(Long id);

    /**
     * 根据ID查询记忆记录
     *
     * @param id 记忆ID
     * @param getters 需要查询的字段
     * @return 记忆实体
     */
    Optional<AgentMemoryEntity> findById(Long id, List<Getter<AgentMemoryEntity>> getters);

    /**
     * 根据会话ID查询记忆记录列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 记忆记录列表
     */
    List<AgentMemoryEntity> findBySessionId(Long sessionId, List<Getter<AgentMemoryEntity>> getters);

    /**
     * 根据学习空间ID和会话ID为空查询记忆记录列表
     *
     * @param learningSpaceId 学习空间ID
     * @param getters 需要查询的字段
     * @return 记忆记录列表
     */
    List<AgentMemoryEntity> findByLearningSpaceIdAndSessionIdIsNull(Long learningSpaceId, List<Getter<AgentMemoryEntity>> getters);

    /**
     * 更新记忆记录
     *
     * @param id      记忆ID
     * @param updater 更新操作
     */
    void updateById(Long id, Consumer<AgentMemoryEntity> updater);

    /**
     * 更新记忆访问信息
     *
     * @param id           记忆ID
     * @param accessCount  访问次数
     * @param lastAccessedAt 最后访问时间
     * @return 是否更新成功
     */
    boolean updateAccessInfo(Long id, Integer accessCount, java.time.LocalDateTime lastAccessedAt);

    /**
     * 删除记忆记录
     *
     * @param id 记忆ID
     * @return 是否删除成功
     */
    Boolean deleteById(Long id);

    /**
     * 根据会话ID删除记忆记录
     *
     * @param sessionId 会话ID
     */
    void deleteBySessionId(Long sessionId);

    /**
     * 检查记忆记录是否存在
     *
     * @param id 记忆ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 分页查询记忆
     *
     * @param userId 用户ID
     * @param query 分页查询参数
     * @return 分页结果
     */
    PageResult<AgentMemoryEntity> findPageByUserId(@NotNull Long userId, @NotNull AgentMemoryPageQuery query);

}