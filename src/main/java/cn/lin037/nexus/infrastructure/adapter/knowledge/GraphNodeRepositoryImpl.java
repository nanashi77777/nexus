package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.port.GraphNodeRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.GraphNodeMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 图谱节点仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class GraphNodeRepositoryImpl implements GraphNodeRepository {

    private final GraphNodeMapper mapper;

    public GraphNodeRepositoryImpl(GraphNodeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(mapper)
                .eq(GraphNodeEntity::getGnId, id)
                .eq(GraphNodeEntity::getGnCreatedByUserId, userId)
                .exists();
    }

    @Override
    public Optional<GraphNodeEntity> findById(Long id, List<Getter<GraphNodeEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(mapper, getters)
                        .eq(GraphNodeEntity::getGnId, id)
                        .get()
        );
    }

    @Override
    public Optional<GraphNodeEntity> findByIdAndUserId(Long id, Long userId, List<Getter<GraphNodeEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphNodeEntity::getGnId, id)
                .eq(GraphNodeEntity::getGnCreatedByUserId, userId)
                .limit(1)
                .get());
    }

    @Override
    public List<GraphNodeEntity> findByEntityId(Long entityId, List<Getter<GraphNodeEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphNodeEntity::getGnEntityId, entityId)
                .eq(GraphNodeEntity::getGnIsProjection, true)
                .list();
    }

    @Override
    public Optional<List<GraphNodeEntity>> findByIdsAndUserId(List<Long> ids, Long userId, List<Getter<GraphNodeEntity>> getters) {
        return Optional.ofNullable(RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .in(GraphNodeEntity::getGnId, ids)
                .eq(GraphNodeEntity::getGnCreatedByUserId, userId)
                .list());
    }

    @Override
    public void save(GraphNodeEntity entity) {
        mapper.save(entity);
    }

    @Override
    public void saveBatch(List<GraphNodeEntity> entities) {
        mapper.saveBatch(entities);
    }

    @Override
    public void updateById(Long id, Consumer<UpdateChain> updater) {
        UpdateChain updateChain = UpdateChain.of(mapper).eq(GraphNodeEntity::getGnId, id);
        updater.accept(updateChain);
        updateChain.execute();
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(mapper)
                .set(GraphNodeEntity::getGnDeletedAt, LocalDateTime.now())
                .eq(GraphNodeEntity::getGnId, id)
                .execute() > 0;
    }

    @Override
    public void disassociateEntity(Long entityId) {
        // 当知识点实体被删除时，将所有引用该实体的投影节点的entity_id设为null，is_projection设为false
        UpdateChain.of(mapper)
                .set(GraphNodeEntity::getGnEntityId, (Long) null)
                .set(GraphNodeEntity::getGnIsProjection, false)
                .set(GraphNodeEntity::getGnUpdatedAt, LocalDateTime.now())
                .eq(GraphNodeEntity::getGnEntityId, entityId)
                .eq(GraphNodeEntity::getGnIsProjection, true)
                .execute();
    }

    @Override
    public void deleteByGraphId(Long graphId) {
        // 批量删除该图谱下的所有节点（级联删除相关边的逻辑已在deleteById中处理）
        UpdateChain.of(mapper)
                .set(GraphNodeEntity::getGnDeletedAt, LocalDateTime.now())
                .eq(GraphNodeEntity::getGnGraphId, graphId)
                .execute();
    }
} 