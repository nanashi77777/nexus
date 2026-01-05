package cn.lin037.nexus.query.isolation.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.common.constant.enums.result.impl.CommonResultCodeEnum;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.LearningSpaceEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.LearningSpaceMapper;
import cn.lin037.nexus.query.isolation.LearningSpaceQuery;
import cn.lin037.nexus.query.isolation.vo.LearningSpaceDetailVO;
import cn.lin037.nexus.query.isolation.vo.LearningSpacePageVO;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import org.springframework.stereotype.Service;

@Service
public class LearningSpaceQueryImpl implements LearningSpaceQuery {

    private final LearningSpaceMapper learningSpaceMapper;

    public LearningSpaceQueryImpl(LearningSpaceMapper learningSpaceMapper) {
        this.learningSpaceMapper = learningSpaceMapper;
    }

    @Override
    public LearningSpaceDetailVO findUserSpaceDetail(Long learningSpaceId) {
        long userId = StpUtil.getLoginIdAsLong();

        LearningSpaceDetailVO learningSpaceDetailVO = QueryChain.of(learningSpaceMapper)
                .select(LearningSpaceDetailVO.class)
                .eq(LearningSpaceEntity::getLsId, learningSpaceId)
                .eq(LearningSpaceEntity::getLsUserId, userId)
                .returnType(LearningSpaceDetailVO.class)
                .limit(1)
                .get();
        if (learningSpaceDetailVO == null) {
            throw new ApplicationException(CommonResultCodeEnum.NOT_FOUND, "学习空间不存在");
        }
        return learningSpaceDetailVO;
    }

    @Override
    public Pager<LearningSpacePageVO> findUserSpacesPage(LearningSpacePageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();
        QueryChain<LearningSpaceEntity> queryChain = QueryChain.of(learningSpaceMapper)
                .select(LearningSpacePageVO.class)
                .eq(LearningSpaceEntity::getLsUserId, userId);

        // 关键词搜索 - 使用OR连接多个字段的模糊查询
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryChain.andNested(chain -> chain.like(LearningSpaceEntity::getLsName, query.getKeyword())
                    .or()
                    .like(LearningSpaceEntity::getLsDescription, query.getKeyword())
                    .or()
                    .like(LearningSpaceEntity::getLsSpacePrompt, query.getKeyword()));
        }

        // 排序处理
        if (query.getSortField() != null) {
            switch (query.getSortField()) {
                case "id" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, LearningSpaceEntity::getLsId, query.getSortDirection());
                case "name" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, LearningSpaceEntity::getLsName, query.getSortDirection());
                default ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, LearningSpaceEntity::getLsCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(LearningSpaceEntity::getLsCreatedAt);
        }
        return queryChain.returnType(LearningSpacePageVO.class).paging(query.buildPager());
    }
}
