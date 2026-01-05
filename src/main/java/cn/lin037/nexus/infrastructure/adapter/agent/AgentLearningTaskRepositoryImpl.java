package cn.lin037.nexus.infrastructure.adapter.agent;

import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.application.agent.port.AgentLearningTaskRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.agent.AgentLearningTaskMapper;
import cn.lin037.nexus.web.rest.v1.agent.req.AgentLearningTaskPageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;

import static cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum.PARAM_ERROR;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Agent学习任务Repository实现类
 * 提供Agent学习任务的数据访问操作
 *
 * @author Lin037
 */
@Repository
public class AgentLearningTaskRepositoryImpl implements AgentLearningTaskRepository {

    private final AgentLearningTaskMapper taskMapper;

    /**
     * 构造函数
     *
     * @param taskMapper 学习任务Mapper
     */
    public AgentLearningTaskRepositoryImpl(AgentLearningTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    /**
     * 保存学习任务
     *
     * @param entity 学习任务实体
     */
    @Override
    public void save(AgentLearningTaskEntity entity) {
        taskMapper.save(entity);
    }

    /**
     * 根据ID查询学习任务
     *
     * @param id 学习任务ID
     * @return 学习任务实体
     */
    @Override
    public Optional<AgentLearningTaskEntity> findById(Long id) {
        return Optional.ofNullable(QueryChain.of(taskMapper)
                .eq(AgentLearningTaskEntity::getAltId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据ID查询学习任务
     *
     * @param id 学习任务ID
     * @param getters 需要查询的字段
     * @return 学习任务实体
     */
    @Override
    public Optional<AgentLearningTaskEntity> findById(Long id, List<Getter<AgentLearningTaskEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(taskMapper, getters)
                .eq(AgentLearningTaskEntity::getAltId, id)
                .limit(1)
                .get());
    }

    /**
     * 根据会话ID查询学习任务列表
     *
     * @param sessionId 会话ID
     * @return 学习任务列表
     */
    @Override
    public List<AgentLearningTaskEntity> findBySessionId(Long sessionId) {
        return QueryChain.of(taskMapper)
                .eq(AgentLearningTaskEntity::getAltSessionId, sessionId)
                .orderBy(AgentLearningTaskEntity::getAltCreatedAt)
                .list();
    }

    /**
     * 根据会话ID查询学习任务列表
     *
     * @param sessionId 会话ID
     * @param getters 需要查询的字段
     * @return 学习任务列表
     */
    @Override
    public List<AgentLearningTaskEntity> findBySessionId(Long sessionId, List<Getter<AgentLearningTaskEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(taskMapper, getters)
                .eq(AgentLearningTaskEntity::getAltSessionId, sessionId)
                .orderBy(AgentLearningTaskEntity::getAltCreatedAt)
                .list();
    }

    /**
     * 根据用户ID和学习空间ID查询学习任务列表
     *
     * @param userId 用户ID
     * @param learningSpaceId 学习空间ID
     * @param getters 需要查询的字段
     * @return 学习任务列表
     */
    @Override
    public List<AgentLearningTaskEntity> findByUserIdAndLearningSpaceId(Long userId, Long learningSpaceId, List<Getter<AgentLearningTaskEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(taskMapper, getters)
                .eq(AgentLearningTaskEntity::getAltUserId, userId)
                .eq(AgentLearningTaskEntity::getAltLearningSpaceId, learningSpaceId)
                .orderByDesc(AgentLearningTaskEntity::getAltCreatedAt)
                .list();
    }

    /**
     * 更新学习任务
     *
     * @param entity 学习任务实体
     */
    @Override
    public void updateById(AgentLearningTaskEntity entity) {
        taskMapper.saveOrUpdate(entity);
    }

    /**
     * 更新学习任务完成状态
     *
     * @param id              学习任务ID
     * @param isCompleted     是否完成
     * @param completedByMessageId 完成消息ID
     * @return 是否更新成功
     */
    @Override
    public boolean updateCompletionStatus(Long id, Integer isCompleted, Long completedByMessageId) {
        UpdateChain updateChain = UpdateChain.of(taskMapper)
                .set(AgentLearningTaskEntity::getAltIsCompleted, isCompleted)
                .set(AgentLearningTaskEntity::getAltUpdatedAt, LocalDateTime.now())
                .eq(AgentLearningTaskEntity::getAltId, id);
        return updateChain.execute() > 0;
    }

    /**
     * 删除学习任务（逻辑删除）
     *
     * @param id 学习任务ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        UpdateChain updateChain = UpdateChain.of(taskMapper)
                .set(AgentLearningTaskEntity::getAltDeletedAt, LocalDateTime.now())
                .eq(AgentLearningTaskEntity::getAltId, id)
                .isNull(AgentLearningTaskEntity::getAltDeletedAt);
        return updateChain.execute() > 0;
    }

    /**
     * 根据会话ID删除学习任务（逻辑删除）
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteBySessionId(Long sessionId) {
        UpdateChain.of(taskMapper)
                .set(AgentLearningTaskEntity::getAltDeletedAt, LocalDateTime.now())
                .eq(AgentLearningTaskEntity::getAltSessionId, sessionId)
                .isNull(AgentLearningTaskEntity::getAltDeletedAt)
                .execute();
    }

    /**
     * 检查学习任务是否存在
     *
     * @param id 学习任务ID
     * @return 是否存在
     */
    @Override
    public boolean existsById(Long id) {
        return QueryChain.of(taskMapper)
                .eq(AgentLearningTaskEntity::getAltId, id)
                .exists();
    }

    /**
     * 分页查询学习任务
     *
     * @param userId 用户ID
     * @param query  分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<AgentLearningTaskEntity> findPageByUserId(@NotNull Long userId, @NotNull AgentLearningTaskPageQuery query) {
        QueryChain<AgentLearningTaskEntity> queryChain = QueryChain.of(taskMapper)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(AgentLearningTaskEntity::getAltUserId, userId)
                .isNull(AgentLearningTaskEntity::getAltDeletedAt);

        // 可选的学习空间ID过滤
        queryChain.eq(AgentLearningTaskEntity::getAltLearningSpaceId, query.getLearningSpaceId());

        // 可选的会话ID过滤
        queryChain.eq(AgentLearningTaskEntity::getAltSessionId, query.getSessionId());

        // 可选的完成状态过滤
        queryChain.eq(AgentLearningTaskEntity::getAltIsCompleted, query.getIsCompleted());

        // 可选的难度等级过滤
        if (query.getDifficultyLevel() != null) {
            AgentLearningDifficultyEnum difficultyEnum = AgentLearningDifficultyEnum.fromCode(query.getDifficultyLevel());
            if (difficultyEnum == null) {
                throw new ApplicationException(PARAM_ERROR, "请求的学习任务难度等级不存在");
            }
            queryChain.eq(AgentLearningTaskEntity::getAltDifficultyLevel, difficultyEnum);
        }

        // 关键词搜索 - 使用OR连接多个字段的模糊查询
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.andNested(queryChain1 -> queryChain1
                    .like(AgentLearningTaskEntity::getAltTitle, query.getKeyword())
                    .or()
                    .like(AgentLearningTaskEntity::getAltObjective, query.getKeyword())
            );
        }

        // 排序 - xbatis的QueryChain使用orderByDesc方法，默认按创建时间降序
        if (query.getSortField() != null) {
            switch (query.getSortField()) {
                case "title" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltTitle, query.getSortDirection());
                case "difficultyLevel" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltDifficultyLevel, query.getSortDirection());
                case "isCompleted" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltIsCompleted, query.getSortDirection());
                case "updatedAt" -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltUpdatedAt, query.getSortDirection());
                default -> RepositoryUtils.setSortDirectionCondition(queryChain, AgentLearningTaskEntity::getAltCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(AgentLearningTaskEntity::getAltCreatedAt);
        }

        Pager<AgentLearningTaskEntity> paging = queryChain.paging(Pager.of(query.getPageNum(), query.getPageSize()));
        return PageResult.of(paging.getResults(), paging.getTotal(), query);
    }
}