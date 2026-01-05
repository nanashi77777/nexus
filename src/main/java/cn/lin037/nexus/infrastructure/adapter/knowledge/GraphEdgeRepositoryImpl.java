package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.GraphEdgeRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.GraphEdgeMapper;
import cn.xbatis.core.sql.executor.Where;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 图谱边仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class GraphEdgeRepositoryImpl implements GraphEdgeRepository {

    private final GraphEdgeMapper mapper;

    public GraphEdgeRepositoryImpl(GraphEdgeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(mapper)
                .eq(GraphEdgeEntity::getGeId, id)
                .eq(GraphEdgeEntity::getGeCreatedByUserId, userId)
                .exists();
    }

    @Override
    public Optional<GraphEdgeEntity> findById(Long id, List<Getter<GraphEdgeEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(mapper, getters)
                        .eq(GraphEdgeEntity::getGeId, id)
                        .get()
        );
    }

    @Override
    public List<GraphEdgeEntity> findByGraphId(Long graphId, Long userId, List<Getter<GraphEdgeEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphEdgeEntity::getGeGraphId, graphId)
                .eq(GraphEdgeEntity::getGeCreatedByUserId, userId)
                .list();
    }

    @Override
    public List<GraphEdgeEntity> findByRelationId(Long relationId, List<Getter<GraphEdgeEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphEdgeEntity::getGeRelationEntityRelationId, relationId)
                .eq(GraphEdgeEntity::getGeIsProjection, true)
                .list();
    }

    @Override
    public void save(GraphEdgeEntity entity) {
        mapper.save(entity);
    }

    @Override
    public void updateById(Long id, Consumer<UpdateChain> updater) {
        UpdateChain updateChain = UpdateChain.of(mapper).eq(GraphEdgeEntity::getGeId, id);
        updater.accept(updateChain);
        updateChain.execute();
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(mapper)
                .set(GraphEdgeEntity::getGeDeletedAt, LocalDateTime.now())
                .eq(GraphEdgeEntity::getGeId, id)
                .execute() > 0;
    }

    @Override
    public void materialize(Long edgeId, Long relationId) {
        UpdateChain.of(mapper)
                .set(GraphEdgeEntity::getGeIsProjection, true)
                .set(GraphEdgeEntity::getGeRelationEntityRelationId, relationId)
                .set(GraphEdgeEntity::getGeUpdatedAt, LocalDateTime.now())
                .eq(GraphEdgeEntity::getGeId, edgeId)
                .execute();
    }

    @Override
    public void disassociateRelation(Long relationId) {
        // 当知识点关系实体被删除时，将所有引用该关系的投影边的relation_entity_relation_id设为null，is_projection设为false
        UpdateChain.of(mapper)
                .set(GraphEdgeEntity::getGeRelationEntityRelationId, null, true)
                .set(GraphEdgeEntity::getGeIsProjection, false)
                .set(GraphEdgeEntity::getGeUpdatedAt, LocalDateTime.now())
                .eq(GraphEdgeEntity::getGeRelationEntityRelationId, relationId)
                .eq(GraphEdgeEntity::getGeIsProjection, true)
                .execute();
    }

    @Override
    public void deleteByGraphId(Long graphId) {
        // 批量删除该图谱下的所有边
        UpdateChain.of(mapper)
                .set(GraphEdgeEntity::getGeDeletedAt, LocalDateTime.now())
                .eq(GraphEdgeEntity::getGeGraphId, graphId)
                .execute();
    }

    @Override
    public void deleteByNodeId(Long nodeId) {
        // 删除与指定节点相关的所有边（使用虚体节点ID或投影节点ID进行匹配）
        mapper.delete(Where.create(mapper)
                .eq(GraphEdgeEntity::getGeSourceVirtualNodeId, nodeId)
                .or()
                .eq(GraphEdgeEntity::getGeTargetVirtualNodeId, nodeId)
                .or()
                .eq(GraphEdgeEntity::getGeSourceProjectionNodeId, nodeId)
                .or()
                .eq(GraphEdgeEntity::getGeTargetProjectionNodeId, nodeId)
        );
    }

    @Override
    public List<GraphEdgeEntity> findByNodeId(Long nodeId, Long userId, List<Getter<GraphEdgeEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphEdgeEntity::getGeCreatedByUserId, userId)
                .andNested(
                        query -> query
                                .eq(GraphEdgeEntity::getGeSourceVirtualNodeId, nodeId)
                                .or()
                                .eq(GraphEdgeEntity::getGeTargetVirtualNodeId, nodeId)
                )
                .list();
    }

    @Override
    public void saveBatch(List<GraphEdgeEntity> entities) {
        mapper.saveBatch(entities);
    }

    @Override
    @Transactional
    public void batchMaterialize(List<Long> edgeIds, List<Long> relationIds) {
        if (edgeIds.size() != relationIds.size()) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.RELATION_NOT_FOUND, "edgeId 和 relationIds 必须具有相同的大小");
        }

        for (int i = 0; i < edgeIds.size(); i++) {
            materialize(edgeIds.get(i), relationIds.get(i));
        }
    }

    @Override
    public List<GraphEdgeEntity> findBetweenNodeIds(List<Long> nodeIds, Long userId, List<Getter<GraphEdgeEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(mapper, getters)
                .eq(GraphEdgeEntity::getGeCreatedByUserId, userId)
                .in(GraphEdgeEntity::getGeSourceVirtualNodeId, nodeIds)
                .in(GraphEdgeEntity::getGeTargetVirtualNodeId, nodeIds)
                .list();
    }
} 