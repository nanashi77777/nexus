package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.port.GraphEdgeRepository;
import cn.lin037.nexus.application.knowledge.port.KnowledgePointRelationRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgePointRelationMapper;
import cn.xbatis.core.sql.executor.Where;
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
 * 知识点关系仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class KnowledgePointRelationRepositoryImpl implements KnowledgePointRelationRepository {

    private final KnowledgePointRelationMapper pointRelationMapper;
    private final GraphEdgeRepository graphEdgeRepository;

    public KnowledgePointRelationRepositoryImpl(KnowledgePointRelationMapper pointRelationMapper, GraphEdgeRepository graphEdgeRepository) {
        this.pointRelationMapper = pointRelationMapper;
        this.graphEdgeRepository = graphEdgeRepository;
    }

    @Override
    public boolean existsByIdAndUserId(Long relationId, Long userId) {
        return QueryChain.of(pointRelationMapper)
                .eq(KnowledgePointRelationEntity::getKprId, relationId)
                .eq(KnowledgePointRelationEntity::getKprCreatedByUserId, userId)
                .exists();
    }

    @Override
    public Optional<KnowledgePointRelationEntity> findById(Long id, List<Getter<KnowledgePointRelationEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(pointRelationMapper, getters)
                        .eq(KnowledgePointRelationEntity::getKprId, id)
                        .get()
        );
    }

    @Override
    public List<KnowledgePointRelationEntity> findByIds(List<Long> ids, List<Getter<KnowledgePointRelationEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(pointRelationMapper, getters)
                .eq(KnowledgePointRelationEntity::getKprId, ids)
                .list();
    }

    @Override
    public List<KnowledgePointRelationEntity> findBySourcePointId(Long sourcePointId, Long userId, List<Getter<KnowledgePointRelationEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(pointRelationMapper, getters)
                .eq(KnowledgePointRelationEntity::getKprSourcePointId, sourcePointId)
                .eq(KnowledgePointRelationEntity::getKprCreatedByUserId, userId)
                .list();
    }

    @Override
    public void save(KnowledgePointRelationEntity entity) {
        pointRelationMapper.save(entity);
    }

    @Override
    public void updateById(Long id, Consumer<UpdateChain> updater) {
        UpdateChain updateChain = UpdateChain.of(pointRelationMapper).eq(KnowledgePointRelationEntity::getKprId, id);
        updater.accept(updateChain);
        updateChain.execute();
    }

    @Override
    public boolean deleteById(Long id) {
        // 级联删除：先解除所有引用该关系实体的投影边关联
        graphEdgeRepository.disassociateRelation(id);
        
        return UpdateChain.of(pointRelationMapper)
                .set(KnowledgePointRelationEntity::getKprDeletedAt, LocalDateTime.now())
                .eq(KnowledgePointRelationEntity::getKprId, id)
                .execute() > 0;
    }

    @Override
    public int deleteByPointId(Long pointId) {

        return pointRelationMapper.delete(Where.create(pointRelationMapper)
                .eq(KnowledgePointRelationEntity::getKprSourcePointId, pointId)
                .or()
                .eq(KnowledgePointRelationEntity::getKprTargetPointId, pointId)
        );
    }

    @Override
    public List<KnowledgePointRelationEntity> findBetweenPointIds(Collection<Long> pointIds, Long userId, List<Getter<KnowledgePointRelationEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(pointRelationMapper, getters)
                .eq(KnowledgePointRelationEntity::getKprCreatedByUserId, userId)
                .in(KnowledgePointRelationEntity::getKprSourcePointId, pointIds)
                .in(KnowledgePointRelationEntity::getKprTargetPointId, pointIds)
                .list();
    }

    @Override
    public void saveBatch(List<KnowledgePointRelationEntity> entities) {
        pointRelationMapper.saveBatch(entities);
    }
} 