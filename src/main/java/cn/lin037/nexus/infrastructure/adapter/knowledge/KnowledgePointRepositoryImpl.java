package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.port.KnowledgePointRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgePointMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 知识点仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class KnowledgePointRepositoryImpl implements KnowledgePointRepository {

    private final KnowledgePointMapper pointMapper;

    public KnowledgePointRepositoryImpl(KnowledgePointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(pointMapper)
                .eq(KnowledgePointEntity::getKpId, id)
                .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                .exists();
    }

    @Override
    public boolean existsByLearningSpaceAndPointIds(Long pointId1, Long pointId2, Long userId, Long learningSpaceId) {
        return QueryChain.of(pointMapper)
                .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                .eq(KnowledgePointEntity::getKpLearningSpaceId, learningSpaceId)
                .in(KnowledgePointEntity::getKpId, List.of(pointId1, pointId2))
                .count() == 2;
    }

    @Override
    public Optional<KnowledgePointEntity> findById(Long id, List<Getter<KnowledgePointEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(pointMapper, getters)
                        .eq(KnowledgePointEntity::getKpId, id)
                        .get()
        );
    }

    @Override
    public List<KnowledgePointEntity> findByFolderId(Long folderId, Long userId, List<Getter<KnowledgePointEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(pointMapper, getters)
                .eq(KnowledgePointEntity::getKpFolderId, folderId)
                .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                .list();
    }


    @Override
    public List<Long> findDontExistIdsByIds(List<Long> list) {
        return QueryChain.of(pointMapper)
                .in(KnowledgePointEntity::getKpId, list)
                .notIn(KnowledgePointEntity::getKpId, QueryChain.of(pointMapper)
                        .in(KnowledgePointEntity::getKpId, list)
                        .select(KnowledgePointEntity::getKpId))
                .list()
                .stream()
                .map(KnowledgePointEntity::getKpId)
                .toList();
    }

    @Override
    public Optional<KnowledgePointEntity> findByIdAndUserId(Long pointId, Long userId, List<Getter<KnowledgePointEntity>> getters) {

        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(pointMapper, getters)
                .eq(KnowledgePointEntity::getKpId, pointId)
                .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                .limit(1)
                .get());
    }

    @Override
    public List<KnowledgePointEntity> findByUserIdAndIds(Long userId, Collection<Long> list, List<Getter<KnowledgePointEntity>> getters) {

        return RepositoryUtils.getQueryChainWithFields(pointMapper, getters)
                .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                .in(KnowledgePointEntity::getKpId, list)
                .list();
    }

    @Override
    public void save(KnowledgePointEntity entity) {
        pointMapper.save(entity);
    }

    @Override
    public void saveBatch(List<KnowledgePointEntity> entities) {
        pointMapper.saveBatch(entities);
    }

    @Override
    public void updateById(Long id, Consumer<UpdateChain> updater) {
        UpdateChain updateChain = UpdateChain.of(pointMapper).eq(KnowledgePointEntity::getKpId, id);
        updater.accept(updateChain);
        updateChain.execute();
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(pointMapper)
                .set(KnowledgePointEntity::getKpDeletedAt, LocalDateTime.now())
                .eq(KnowledgePointEntity::getKpId, id)
                .execute() > 0;
    }

} 