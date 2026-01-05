package cn.lin037.nexus.query.resource.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.resource.ResourceChunkMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.resource.ResourceMapper;
import cn.lin037.nexus.query.resource.ResourceQuery;
import cn.lin037.nexus.query.resource.vo.ResourceChunkDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourceChunkPageVO;
import cn.lin037.nexus.query.resource.vo.ResourceDetailVO;
import cn.lin037.nexus.query.resource.vo.ResourcePageVO;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourceChunkPageQuery;
import cn.lin037.nexus.web.rest.v1.resource.req.ResourcePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceQueryImpl implements ResourceQuery {

    private final ResourceMapper resourceMapper;
    private final ResourceChunkMapper resourceChunkMapper;

    @Override
    public ResourceDetailVO findUserResourceDetail(Long resourceId) {
        long userId = StpUtil.getLoginIdAsLong();
        return QueryChain.of(resourceMapper)
                .select(ResourceDetailVO.class)
                .eq(ResourceEntity::getRsId, resourceId)
                .eq(ResourceEntity::getRsCreatedByUserId, userId)
                .returnType(ResourceDetailVO.class)
                .limit(1)
                .get();
    }

    @Override
    public Pager<ResourcePageVO> findUserResourcesPage(ResourcePageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();
        QueryChain<ResourceEntity> queryChain = QueryChain.of(resourceMapper)
                .select(ResourcePageVO.class)
                .eq(ResourceEntity::getRsCreatedByUserId, userId)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(ResourceEntity::getRsLearningSpaceId, query.getLearningSpaceId());

        queryChain.eq(ResourceEntity::getRsStatus, query.getStatus() != null ? query.getStatus().getCode() : null)
                .eq(ResourceEntity::getRsSourceType, query.getSourceType() != null ? query.getSourceType().getCode() : null);

        // 关键词搜索
        queryChain.andNested(chain -> chain.like(ResourceEntity::getRsTitle, query.getKeyword())
                .or()
                .like(ResourceEntity::getRsDescription, query.getKeyword())
                .or()
                .like(ResourceEntity::getRsPrompt, query.getKeyword()));

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            if (query.getSortField().equals("title")) {
                RepositoryUtils.setSortDirectionCondition(queryChain, ResourceEntity::getRsTitle, query.getSortDirection());
            } else if (query.getSortField().equals("updateAt")) {
                RepositoryUtils.setSortDirectionCondition(queryChain, ResourceEntity::getRsUpdatedAt, query.getSortDirection());
            } else {
                RepositoryUtils.setSortDirectionCondition(queryChain, ResourceEntity::getRsCreatedAt, query.getSortDirection());
            }
        } else {
            // 默认按创建时间降序排序
            queryChain.orderByDesc(ResourceEntity::getRsCreatedAt);
        }

        return queryChain.returnType(ResourcePageVO.class).paging(query.buildPager());
    }

    @Override
    public ResourceChunkDetailVO findUserResourceChunkDetail(Long chunkId) {
        long userId = StpUtil.getLoginIdAsLong();
        return QueryChain.of(resourceChunkMapper)
                .select(ResourceChunkDetailVO.class)
                .eq(ResourceChunkEntity::getRcId, chunkId)
                .eq(ResourceChunkEntity::getRcCreatedByUserId, userId)
                .returnType(ResourceChunkDetailVO.class)
                .limit(1)
                .get();
    }

    @Override
    public Pager<ResourceChunkPageVO> findUserResourceChunksPage(ResourceChunkPageQuery query) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryChain<ResourceChunkEntity> queryChain = QueryChain.of(resourceChunkMapper)
                .select(ResourceChunkPageVO.class)
                .ignoreNullValueInCondition(true)
                .ignoreEmptyInCondition(true)
                .trimStringInCondition(true)
                .eq(ResourceChunkEntity::getRcResourceId, query.getResourceId())
                .eq(ResourceChunkEntity::getRcLearningSpaceId, query.getLearningSpaceId())
                .eq(ResourceChunkEntity::getRcCreatedByUserId, userId)
                .eq(ResourceChunkEntity::getRcIsVectorized, query.getStatus());

        // 关键词搜索
        queryChain.like(ResourceChunkEntity::getRcContent, query.getKeyword());

        // 排序处理
        if (StrUtil.isNotBlank(query.getSortField())) {
            switch (query.getSortField()) {
                case "createdAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, ResourceChunkEntity::getRcCreatedAt, query.getSortDirection());
                case "pageIndex" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, ResourceChunkEntity::getRcPageIndex, query.getSortDirection());
                case "chunkIndex" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, ResourceChunkEntity::getRcChunkIndex, query.getSortDirection());
                case "updatedAt" ->
                        RepositoryUtils.setSortDirectionCondition(queryChain, ResourceChunkEntity::getRcUpdatedAt, query.getSortDirection());
                default ->
                        queryChain.orderBy(ResourceChunkEntity::getRcPageIndex).orderBy(ResourceChunkEntity::getRcChunkIndex);
            }
        } else {
            // 默认按页码和分片索引升序排序
            queryChain.orderBy(ResourceChunkEntity::getRcPageIndex).orderBy(ResourceChunkEntity::getRcChunkIndex);
        }

        return queryChain.returnType(ResourceChunkPageVO.class).paging(query.buildPager());
    }
}
