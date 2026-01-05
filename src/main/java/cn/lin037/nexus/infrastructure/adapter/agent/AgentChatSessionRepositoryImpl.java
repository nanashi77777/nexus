package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.lin037.nexus.application.agent.port.AgentChatSessionRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionStatusEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentChatSessionMapper;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum.PARAM_ERROR;

/**
 * Agent聊天会话Repository实现类
 * 提供Agent聊天会话的数据访问操作
 *
 * @author Lin037
 */
@Repository
public class AgentChatSessionRepositoryImpl implements AgentChatSessionRepository {

    private final AgentChatSessionMapper sessionMapper;

    /**
     * 构造函数
     *
     * @param sessionMapper 会话Mapper
     */
    public AgentChatSessionRepositoryImpl(AgentChatSessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    /**
     * 保存聊天会话
     *
     * @param entity 聊天会话实体
     * @return 保存后的实体
     */
    @Override
    public AgentChatSessionEntity save(AgentChatSessionEntity entity) {
        sessionMapper.save(entity);
        return entity;
    }

    /**
     * 根据ID查询聊天会话
     *
     * @param id      会话ID
     * @param getters 需要查询的字段
     * @return 聊天会话实体
     */
    @Override
    public Optional<AgentChatSessionEntity> findById(Long id, List<Getter<AgentChatSessionEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(sessionMapper, getters)
                .eq(AgentChatSessionEntity::getAcsId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据ID和用户ID查询聊天会话
     *
     * @param id      会话ID
     * @param userId  用户ID
     * @param getters 需要查询的字段
     * @return 聊天会话实体
     */
    @Override
    public Optional<AgentChatSessionEntity> findByIdAndUserId(Long id, Long userId, List<Getter<AgentChatSessionEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(sessionMapper, getters)
                .eq(AgentChatSessionEntity::getAcsId, id)
                .eq(AgentChatSessionEntity::getAcsUserId, userId)
                .limit(1)
                .get());
    }

    /**
     * 更新聊天会话
     *
     * @param entity 聊天会话实体
     * @return 更新后的实体
     */
    @Override
    public AgentChatSessionEntity updateById(AgentChatSessionEntity entity) {
        sessionMapper.saveOrUpdate(entity);
        return entity;
    }

    /**
     * 原子性更新会话状态
     *
     * @param id             会话ID
     * @param expectedStatus 期望的旧状态
     * @param newStatus      新状态
     * @return 是否更新成功
     */
    @Override
    public boolean updateStatusAtomically(Long id, AgentChatSessionStatusEnum expectedStatus, AgentChatSessionStatusEnum newStatus) {
        UpdateChain updateChain = UpdateChain.of(sessionMapper)
                .set(AgentChatSessionEntity::getAcsStatus, newStatus.getCode())
                .set(AgentChatSessionEntity::getAcsUpdatedAt, LocalDateTime.now())
                .eq(AgentChatSessionEntity::getAcsId, id)
                .eq(AgentChatSessionEntity::getAcsStatus, expectedStatus.getCode());
        return updateChain.execute() > 0;
    }

    @Override
    public boolean updateStatus(Long id, AgentChatSessionStatusEnum status) {

        return UpdateChain.of(sessionMapper)
                .set(AgentChatSessionEntity::getAcsStatus, status.getCode())
                .set(AgentChatSessionEntity::getAcsUpdatedAt, LocalDateTime.now())
                .eq(AgentChatSessionEntity::getAcsId, id)
                .execute() > 0;
    }

    /**
     * 删除聊天会话（逻辑删除）
     *
     * @param id 会话ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        UpdateChain updateChain = UpdateChain.of(sessionMapper)
                .set(AgentChatSessionEntity::getAcsDeletedAt, LocalDateTime.now())
                .set(AgentChatSessionEntity::getAcsUpdatedAt, LocalDateTime.now())
                .eq(AgentChatSessionEntity::getAcsId, id)
                .isNull(AgentChatSessionEntity::getAcsDeletedAt);
        return updateChain.execute() > 0;
    }

    /**
     * 分页查询聊天会话
     *
     * @param userId 用户ID
     * @param query  分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<AgentChatSessionEntity> findPageByUserId(@NotNull Long userId, @NotNull AgentSessionPageQuery query) {
        QueryChain<AgentChatSessionEntity> queryChain = QueryChain.of(sessionMapper)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentChatSessionEntity::getAcsUserId, userId);

        // 可选的学习空间ID过滤
        queryChain.eq(AgentChatSessionEntity::getAcsLearningSpaceId, query.getLearningSpaceId());

        // 关键词搜索
        queryChain.like(AgentChatSessionEntity::getAcsTitle, query.getKeyword());

        // 可选的会话状态过滤
        if (query.getStatus() != null) {
            AgentChatSessionStatusEnum agentChatSessionStatusEnum = AgentChatSessionStatusEnum.fromCode(query.getStatus());
            if (agentChatSessionStatusEnum == null) {
                throw new ApplicationException(PARAM_ERROR, "请求的会话状态不存在");
            }
            queryChain.eq(AgentChatSessionEntity::getAcsStatus, agentChatSessionStatusEnum.getCode());
        }

        // 排序 - xbatis的QueryChain使用orderByDesc方法，默认按创建时间降序
        if (query.getSortField() != null) {
            switch (query.getSortField()) {
                case "title" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsTitle, query.getSortDirection());
                case "status" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsStatus, query.getSortDirection());
                case "updatedAt" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsUpdatedAt, query.getSortDirection());
                default -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentChatSessionEntity::getAcsCreatedAt);
        }

        Pager<AgentChatSessionEntity> paging = queryChain.paging(Pager.of(query.getPageNum(), query.getPageSize()));
        return PageResult.of(paging.getResults(), paging.getTotal(), query);
    }

}
