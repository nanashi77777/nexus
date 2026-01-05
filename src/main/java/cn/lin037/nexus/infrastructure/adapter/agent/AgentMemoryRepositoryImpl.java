package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.application.agent.port.AgentMemoryRepository;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMemoryPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.lin037.nexus.common.model.query.BasePageQuery;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentMemoryMapper;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Agent记忆Repository实现类
 * 提供Agent记忆记录的数据访问操作
 *
 * @author Lin037
 */
@Repository
public class AgentMemoryRepositoryImpl implements AgentMemoryRepository {

    private final AgentMemoryMapper memoryMapper;

    /**
     * 构造函数
     *
     * @param memoryMapper 记忆Mapper
     */
    public AgentMemoryRepositoryImpl(AgentMemoryMapper memoryMapper) {
        this.memoryMapper = memoryMapper;
    }

    /**
     * 保存记忆记录
     *
     * @param entity 记忆实体
     */
    @Override
    public void save(AgentMemoryEntity entity) {
        memoryMapper.save(entity);
    }

    /**
     * 根据ID查询记忆记录
     *
     * @param id 记忆ID
     * @return 记忆实体
     */
    @Override
    public Optional<AgentMemoryEntity> findById(Long id) {
        return Optional.ofNullable(QueryChain.of(memoryMapper)
                .eq(AgentMemoryEntity::getAmId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据ID查询记忆记录
     *
     * @param id 记忆ID
     * @param getters 需要查询的字段
     * @return 记忆实体
     */
    @Override
    public Optional<AgentMemoryEntity> findById(Long id, List<Getter<AgentMemoryEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(memoryMapper, getters)
                .eq(AgentMemoryEntity::getAmId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据会话ID查询记忆记录列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 记忆记录列表
     */
    @Override
    public List<AgentMemoryEntity> findBySessionId(Long sessionId, List<Getter<AgentMemoryEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(memoryMapper, getters)
                .eq(AgentMemoryEntity::getAmSessionId, sessionId)
                .orderByDesc(AgentMemoryEntity::getAmImportanceScore)
                .orderByDesc(AgentMemoryEntity::getAmCreatedAt)
                .list();
    }

    /**
     * 根据学习空间ID和会话ID为空查询记忆记录列表
     *
     * @param learningSpaceId 学习空间ID
     * @param getters 需要查询的字段
     * @return 记忆记录列表
     */
    @Override
    public List<AgentMemoryEntity> findByLearningSpaceIdAndSessionIdIsNull(Long learningSpaceId, List<Getter<AgentMemoryEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(memoryMapper, getters)
                .eq(AgentMemoryEntity::getAmLearningSpaceId, learningSpaceId)
                .isNull(AgentMemoryEntity::getAmSessionId)
                .orderByDesc(AgentMemoryEntity::getAmImportanceScore)
                .orderByDesc(AgentMemoryEntity::getAmLastAccessedAt)
                .list();
    }

    /**
     * 更新记忆记录
     *
     * @param id      记忆ID
     * @param updater 更新操作
     */
    @Override
    public void updateById(Long id, Consumer<AgentMemoryEntity> updater) {
        Optional<AgentMemoryEntity> entityOpt = findById(id);
        if (entityOpt.isPresent()) {
            AgentMemoryEntity entity = entityOpt.get();
            updater.accept(entity);
            entity.setAmUpdatedAt(LocalDateTime.now());
            memoryMapper.saveOrUpdate(entity);
        }
    }

    /**
     * 更新记忆访问信息
     *
     * @param id           记忆ID
     * @param accessCount  访问次数
     * @param lastAccessedAt 最后访问时间
     * @return 是否更新成功
     */
    @Override
    public boolean updateAccessInfo(Long id, Integer accessCount, LocalDateTime lastAccessedAt) {
        UpdateChain updateChain = UpdateChain.of(memoryMapper)
                .set(AgentMemoryEntity::getAmAccessCount, accessCount)
                .set(AgentMemoryEntity::getAmLastAccessedAt, lastAccessedAt)
                .set(AgentMemoryEntity::getAmUpdatedAt, LocalDateTime.now())
                .eq(AgentMemoryEntity::getAmId, id);
        return updateChain.execute() > 0;
    }

    /**
     * 删除记忆记录（逻辑删除）
     *
     * @param id 记忆ID
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteById(Long id) {
        UpdateChain updateChain = UpdateChain.of(memoryMapper)
                .set(AgentMemoryEntity::getAmDeletedAt, LocalDateTime.now())
                .eq(AgentMemoryEntity::getAmId, id)
                .isNull(AgentMemoryEntity::getAmDeletedAt);
        return updateChain.execute() > 0;
    }

    /**
     * 根据会话ID删除记忆记录（逻辑删除）
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteBySessionId(Long sessionId) {
        UpdateChain.of(memoryMapper)
                .set(AgentMemoryEntity::getAmDeletedAt, LocalDateTime.now())
                .eq(AgentMemoryEntity::getAmSessionId, sessionId)
                .isNull(AgentMemoryEntity::getAmDeletedAt)
                .execute();
    }

    /**
     * 检查记忆记录是否存在
     *
     * @param id 记忆ID
     * @return 是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return QueryChain.of(memoryMapper)
                .eq(AgentMemoryEntity::getAmId, id)
                .exists();
    }

    /**
     * 分页查询记忆记录
     *
     * @param userId 用户ID
     * @param query 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<AgentMemoryEntity> findPageByUserId(@NotNull Long userId, @NotNull AgentMemoryPageQuery query) {
        QueryChain<AgentMemoryEntity> queryChain = QueryChain.of(memoryMapper)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentMemoryEntity::getAmUserId, userId)
                .isNull(AgentMemoryEntity::getAmDeletedAt);

        // 可选的学习空间ID过滤
        queryChain.eq(AgentMemoryEntity::getAmLearningSpaceId, query.getLearningSpaceId());

        // 可选的会话ID过滤
        queryChain.eq(AgentMemoryEntity::getAmSessionId, query.getSessionId());

        // 关键词搜索 - 使用OR连接多个字段的模糊查询
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.andNested(queryChain1 -> queryChain1
                    .like(AgentMemoryEntity::getAmContent, query.getKeyword())
                    .or()
                    .like(AgentMemoryEntity::getAmTitle, query.getKeyword())
            );
        }

        // 排序 - xbatis的QueryChain使用orderByDesc方法，默认按创建时间降序
        if (query.getSortField() != null) {
            switch (query.getSortField()) {
                case "title" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmTitle, query.getSortDirection());
                case "content" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmContent, query.getSortDirection());
                case "updatedAt" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmUpdatedAt, query.getSortDirection());
                default -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentMemoryEntity::getAmCreatedAt);
        }

        Pager<AgentMemoryEntity> paging = queryChain.paging(Pager.of(query.getPageNum(), query.getPageSize()));
        return PageResult.of(paging.getResults(), paging.getTotal(), query);
    }
}