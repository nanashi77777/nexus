package cn.lin037.nexus.query.agent.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentChatMessageMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentChatSessionMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentLearningTaskMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentMemoryMapper;
import cn.lin037.nexus.query.agent.AgentQuery;
import cn.lin037.nexus.query.agent.vo.LearningTaskPageVO;
import cn.lin037.nexus.query.agent.vo.MemoryPageVO;
import cn.lin037.nexus.query.agent.vo.MessagePageVO;
import cn.lin037.nexus.query.agent.vo.SessionPageVO;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentLearningTaskPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMemoryPageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentMessagePageQuery;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentSessionPageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Agent查询实现类
 * 提供Agent相关数据的查询功能
 *
 * @author Lin037
 */
@Service
@RequiredArgsConstructor
public class AgentQueryImpl implements AgentQuery {

    private final AgentChatSessionMapper sessionMapper;
    private final AgentMemoryMapper memoryMapper;
    private final AgentLearningTaskMapper learningTaskMapper;
    private final AgentChatMessageMapper messageMapper;

    @Override
    public Pager<SessionPageVO> findUserSessionsPage(AgentSessionPageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryChain<AgentChatSessionEntity> queryChain = QueryChain.of(sessionMapper)
                .select(SessionPageVO.class)
                .eq(AgentChatSessionEntity::getAcsUserId, userId)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentChatSessionEntity::getAcsLearningSpaceId, query.getLearningSpaceId())
                .eq(AgentChatSessionEntity::getAcsStatus, query.getStatus());

        // 关键词搜索
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.like(AgentChatSessionEntity::getAcsTitle, query.getKeyword());
        }

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            switch (query.getSortField()) {
                case "title" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsTitle, query.getSortDirection());
                case "updatedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsUpdatedAt, query.getSortDirection());
                case "createdAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatSessionEntity::getAcsCreatedAt, query.getSortDirection());
                default -> queryChain.orderByDesc(AgentChatSessionEntity::getAcsCreatedAt);
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentChatSessionEntity::getAcsCreatedAt);
        }

        return queryChain.returnType(SessionPageVO.class).paging(query.buildPager());
    }

    @Override
    public Pager<MemoryPageVO> findUserMemoriesPage(AgentMemoryPageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryChain<AgentMemoryEntity> queryChain = QueryChain.of(memoryMapper)
                .select(MemoryPageVO.class)
                .eq(AgentMemoryEntity::getAmUserId, userId)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentMemoryEntity::getAmLearningSpaceId, query.getLearningSpaceId())
                .eq(AgentMemoryEntity::getAmLevel, query.getLevel())
                .eq(AgentMemoryEntity::getAmSource, query.getSource())
                .eq(AgentMemoryEntity::getAmSessionId, query.getSessionId());

        // 关键词搜索
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.andNested(chain -> chain.like(AgentMemoryEntity::getAmTitle, query.getKeyword())
                    .or()
                    .like(AgentMemoryEntity::getAmContent, query.getKeyword()));
        }

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            switch (query.getSortField()) {
                case "title" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmTitle, query.getSortDirection());
                case "updatedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmUpdatedAt, query.getSortDirection());
                case "createdAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmCreatedAt, query.getSortDirection());
                case "importanceScore" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmImportanceScore, query.getSortDirection());
                case "lastAccessedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentMemoryEntity::getAmLastAccessedAt, query.getSortDirection());
                default -> queryChain.orderByDesc(AgentMemoryEntity::getAmCreatedAt);
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentMemoryEntity::getAmCreatedAt);
        }

        return queryChain.returnType(MemoryPageVO.class).paging(query.buildPager());
    }

    @Override
    public Pager<LearningTaskPageVO> findUserLearningTasksPage(AgentLearningTaskPageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryChain<AgentLearningTaskEntity> queryChain = QueryChain.of(learningTaskMapper)
                .select(LearningTaskPageVO.class)
                .eq(AgentLearningTaskEntity::getAltUserId, userId)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentLearningTaskEntity::getAltLearningSpaceId, query.getLearningSpaceId())
                .eq(AgentLearningTaskEntity::getAltSessionId, query.getSessionId())
                .eq(AgentLearningTaskEntity::getAltDifficultyLevel, query.getDifficultyLevel())
                .eq(AgentLearningTaskEntity::getAltIsCompleted, query.getIsCompleted());

        // 关键词搜索
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.andNested(chain -> chain.like(AgentLearningTaskEntity::getAltTitle, query.getKeyword())
                    .or()
                    .like(AgentLearningTaskEntity::getAltObjective, query.getKeyword()));
        }

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            switch (query.getSortField()) {
                case "title" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltTitle, query.getSortDirection());
                case "updatedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltUpdatedAt, query.getSortDirection());
                case "createdAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltCreatedAt, query.getSortDirection());
                default -> queryChain.orderByDesc(AgentLearningTaskEntity::getAltCreatedAt);
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentLearningTaskEntity::getAltCreatedAt);
        }

        return queryChain.returnType(LearningTaskPageVO.class).paging(query.buildPager());
    }

    @Override
    public Pager<MessagePageVO> findUserMessagesPage(AgentMessagePageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryChain<AgentChatMessageEntity> queryChain = QueryChain.of(messageMapper)
                .select(MessagePageVO.class)
                .eq(AgentChatMessageEntity::getAcmUserId, userId)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentChatMessageEntity::getAcmLearningSpaceId, query.getLearningSpaceId())
                .eq(AgentChatMessageEntity::getAcmSessionId, query.getSessionId());

        // 关键词搜索
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.like(AgentChatMessageEntity::getAcmContent, query.getKeyword());
        }

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            switch (query.getSortField()) {
                case "updatedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatMessageEntity::getAcmUpdatedAt, query.getSortDirection());
                case "createdAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, AgentChatMessageEntity::getAcmCreatedAt, query.getSortDirection());
                default -> queryChain.orderByDesc(AgentChatMessageEntity::getAcmCreatedAt);
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentChatMessageEntity::getAcmCreatedAt);
        }

        return queryChain.returnType(MessagePageVO.class).paging(query.buildPager());
    }
}
