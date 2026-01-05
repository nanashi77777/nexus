package cn.lin037.nexus.infrastructure.adapter.isolation;

import cn.lin037.nexus.application.isolation.port.LearningSpaceRepository;
import cn.lin037.nexus.common.model.vo.PageResult;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.LearningSpaceEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.LearningSpaceMapper;
import cn.lin037.nexus.web.rest.v1.isolation.req.LearningSpacePageQuery;
import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class LearningSpaceRepositoryImpl implements LearningSpaceRepository {

    private final LearningSpaceMapper learningSpaceMapper;

    public LearningSpaceRepositoryImpl(LearningSpaceMapper learningSpaceMapper) {
        this.learningSpaceMapper = learningSpaceMapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(learningSpaceMapper)
                .eq(LearningSpaceEntity::getLsId, id)
                .eq(LearningSpaceEntity::getLsUserId, userId)
                .exists();
    }


    @Override
    public Optional<LearningSpaceEntity> findById(Long id, List<Getter<LearningSpaceEntity>> getters) {
        LearningSpaceEntity learningSpaceEntity = RepositoryUtils.getQueryChainWithFields(learningSpaceMapper, getters)
                .eq(LearningSpaceEntity::getLsId, id)
                .isNull(LearningSpaceEntity::getLsDeletedAt)
                .limit(1)
                .get();
        return Optional.ofNullable(learningSpaceEntity);
    }

    @Override
    public PageResult<LearningSpaceEntity> findPageByUserId(LearningSpacePageQuery query, Long userId) {
        Pager<LearningSpaceEntity> paging = QueryChain.of(learningSpaceMapper)
                .eq(LearningSpaceEntity::getLsUserId, userId)
                .paging(Pager.of(query.getPageNum(), query.getPageSize()));
        return PageResult.of(paging.getResults(), paging.getTotal(), query);
    }

    @Override
    public void save(LearningSpaceEntity learningSpaceEntity) {
        learningSpaceMapper.save(learningSpaceEntity);
    }

    @Override
    public void updateById(Long learningSpaceId, Consumer<LearningSpaceEntity> updater) {
        LearningSpaceEntity learningSpaceEntity = new LearningSpaceEntity();
        updater.accept(learningSpaceEntity);
        learningSpaceEntity.setLsId(learningSpaceId);
        learningSpaceMapper.update(learningSpaceEntity);
    }

    @Override
    public Boolean deleteById(Long learningSpaceId) {
        int effect = UpdateChain.of(learningSpaceMapper)
                .set(LearningSpaceEntity::getLsDeletedAt, LocalDateTime.now())
                .eq(LearningSpaceEntity::getLsId, learningSpaceId)
                .execute();
        return effect > 0;
    }
}
