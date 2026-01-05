package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.application.agent.port.AgentChatMessageRepository;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageRoleEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentChatMessageMapper;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMessagePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Agent聊天消息Repository实现类
 * 提供Agent聊天消息的数据访问操作
 *
 * @author Lin037
 */
@Repository
public class AgentChatMessageRepositoryImpl implements AgentChatMessageRepository {

    private final AgentChatMessageMapper messageMapper;

    /**
     * 构造函数
     *
     * @param messageMapper 消息Mapper
     */
    public AgentChatMessageRepositoryImpl(AgentChatMessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 保存聊天消息
     *
     * @param entity 聊天消息实体
     * @return 保存后的实体
     */
    @Override
    public AgentChatMessageEntity save(AgentChatMessageEntity entity) {
        if (entity.getAcmId() == null) {
            entity.setAcmId(Long.parseLong(HutoolSnowflakeIdGenerator.generateId()));
        }
        messageMapper.save(entity);
        return entity;
    }

    /**
     * 根据ID查询聊天消息
     *
     * @param id 聊天消息ID
     * @return 聊天消息实体
     */
    @Override
    public Optional<AgentChatMessageEntity> findById(Long id) {
        return Optional.ofNullable(QueryChain.of(messageMapper)
                .eq(AgentChatMessageEntity::getAcmId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据ID查询聊天消息
     *
     * @param id 聊天消息ID
     * @param getters 需要查询的字段
     * @return 聊天消息实体
     */
    @Override
    public Optional<AgentChatMessageEntity> findById(Long id, List<Getter<AgentChatMessageEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(messageMapper, getters)
                .eq(AgentChatMessageEntity::getAcmId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据会话ID查询聊天消息列表
     *
     * @param sessionId 会话ID
     * @return 聊天消息列表
     */
    @Override
    public List<AgentChatMessageEntity> findBySessionId(Long sessionId) {
        return QueryChain.of(messageMapper)
                .eq(AgentChatMessageEntity::getAcmSessionId, sessionId)
                .orderBy(AgentChatMessageEntity::getAcmCreatedAt)
                .list();
    }

    /**
     * 根据会话ID查询聊天消息列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 聊天消息列表
     */
    @Override
    public List<AgentChatMessageEntity> findBySessionId(Long sessionId, List<Getter<AgentChatMessageEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(messageMapper, getters)
                .eq(AgentChatMessageEntity::getAcmSessionId, sessionId)
                .orderBy(AgentChatMessageEntity::getAcmCreatedAt)
                .list();
    }

    /**
     * 更新聊天消息
     *
     * @param entity 聊天消息实体
     * @return 更新后的实体
     */
    @Override
    public AgentChatMessageEntity updateById(AgentChatMessageEntity entity) {
        entity.setAcmUpdatedAt(LocalDateTime.now());
        messageMapper.saveOrUpdate(entity);
        return entity;
    }


    /**
     * 删除聊天消息（逻辑删除）
     *
     * @param id 消息ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        UpdateChain updateChain = UpdateChain.of(messageMapper)
                .set(AgentChatMessageEntity::getAcmDeletedAt, LocalDateTime.now())
                .eq(AgentChatMessageEntity::getAcmId, id)
                .isNull(AgentChatMessageEntity::getAcmDeletedAt);
        return updateChain.execute() > 0;
    }

    /**
     * 根据会话ID删除聊天消息（逻辑删除）
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteBySessionId(Long sessionId) {
        UpdateChain.of(messageMapper)
                .set(AgentChatMessageEntity::getAcmDeletedAt, LocalDateTime.now())
                .eq(AgentChatMessageEntity::getAcmSessionId, sessionId)
                .isNull(AgentChatMessageEntity::getAcmDeletedAt)
                .execute();
    }

    /**
     * 检查聊天消息是否存在
     *
     * @param id 消息ID
     * @return 是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return QueryChain.of(messageMapper)
                .eq(AgentChatMessageEntity::getAcmId, id)
                .exists();
    }

    @Override
    public Optional<AgentChatMessageEntity> findLatestMessageBySessionIdAndRole(Long sessionId, AgentChatMessageRoleEnum role) {
        return Optional.ofNullable(QueryChain.of(messageMapper)
                .eq(AgentChatMessageEntity::getAcmSessionId, sessionId)
                .eq(AgentChatMessageEntity::getAcmRole, role.getRole())
                .orderByDesc(AgentChatMessageEntity::getAcmCreatedAt)
                .limit(1)
                .get());
    }

    /**
     * 根据用户ID分页查询聊天消息
     *
     * @param query 分页查询参数
     * @param userId 用户ID
     * @return 分页结果
     */
    @Override
    public PageResult<AgentChatMessageEntity> findPageByUserId(Long userId, AgentMessagePageQuery query) {
        QueryChain<AgentChatMessageEntity> queryChain = QueryChain.of(messageMapper)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true) 
                .trimStringInCondition(true)
                .eq(AgentChatMessageEntity::getAcmUserId, userId)
                .isNull(AgentChatMessageEntity::getAcmDeletedAt);

        // 可选的会话ID过滤
        queryChain.eq(AgentChatMessageEntity::getAcmSessionId, query.getSessionId());

        // 关键词搜索 - 使用OR连接多个字段的模糊查询
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.like(AgentChatMessageEntity::getAcmContent, query.getKeyword());
        }

        // 排序处理
        if (query.getSortField() != null) {
            switch (query.getSortField()) {
                case "tokenUsage" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatMessageEntity::getAcmTokens, query.getSortDirection());
                case "updatedAt" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatMessageEntity::getAcmUpdatedAt, query.getSortDirection());
                default -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatMessageEntity::getAcmCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentChatMessageEntity::getAcmCreatedAt);
        }

        Pager<AgentChatMessageEntity> paging = queryChain.paging(Pager.of(query.getPageNum(), query.getPageSize()));
        return PageResult.of(paging.getResults(), paging.getTotal(), query);
    }
}