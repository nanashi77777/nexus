package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.port.KnowledgeGraphRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.KnowledgeGraphEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.GraphEdgeMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.GraphNodeMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.KnowledgeGraphMapper;
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
 * 知识图谱仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class KnowledgeGraphRepositoryImpl implements KnowledgeGraphRepository {

    private final KnowledgeGraphMapper knowledgeGraphMapper;
    private final GraphNodeMapper graphNodeMapper;
    private final GraphEdgeMapper graphEdgeMapper;

    public KnowledgeGraphRepositoryImpl(KnowledgeGraphMapper knowledgeGraphMapper,
                                        GraphNodeMapper graphNodeMapper, GraphEdgeMapper graphEdgeMapper) {
        this.knowledgeGraphMapper = knowledgeGraphMapper;
        this.graphNodeMapper = graphNodeMapper;
        this.graphEdgeMapper = graphEdgeMapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(knowledgeGraphMapper)
                .eq(KnowledgeGraphEntity::getKgId, id)
                .eq(KnowledgeGraphEntity::getKgCreatedByUserId, userId)
                .exists();
    }

    @Override
    public Optional<KnowledgeGraphEntity> findById(Long id, List<Getter<KnowledgeGraphEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(knowledgeGraphMapper, getters)
                        .eq(KnowledgeGraphEntity::getKgId, id)
                        .get()
        );
    }

    @Override
    public List<KnowledgeGraphEntity> findByUserId(Long userId, List<Getter<KnowledgeGraphEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(knowledgeGraphMapper, getters)
                .eq(KnowledgeGraphEntity::getKgCreatedByUserId, userId)
                .list();
    }

    @Override
    public void save(KnowledgeGraphEntity entity) {
        if (entity.getKgId() == null) {
            entity.setKgId(HutoolSnowflakeIdGenerator.generateLongId());
        }
        knowledgeGraphMapper.save(entity);
    }

    @Override
    public void updateById(Long id, Consumer<UpdateChain> updater) {
        UpdateChain updateChain = UpdateChain.of(knowledgeGraphMapper).eq(KnowledgeGraphEntity::getKgId, id);
        updater.accept(updateChain);
        updateChain.execute();
    }

    @Override
    @Transactional
    public boolean deleteById(Long id) {
        // 级联删除：先删除该图谱下的所有边，再删除所有节点，最后删除图谱本身
        UpdateChain.of(graphNodeMapper)
                .set(GraphNodeEntity::getGnDeletedAt, LocalDateTime.now())
                .eq(GraphNodeEntity::getGnId, id)
                .execute();

        UpdateChain.of(graphEdgeMapper)
                .set(GraphEdgeEntity::getGeDeletedAt, LocalDateTime.now())
                .eq(GraphEdgeEntity::getGeId, id)
                .execute();

        return UpdateChain.of(knowledgeGraphMapper)
                .set(KnowledgeGraphEntity::getKgDeletedAt, LocalDateTime.now())
                .eq(KnowledgeGraphEntity::getKgId, id)
                .execute() > 0;
    }

    @Override
    public void deleteByLearningSpaceId(Long learningSpaceId) {
        // 这个方法用于清理学习空间时的级联删除
        UpdateChain.of(knowledgeGraphMapper)
                .set(KnowledgeGraphEntity::getKgDeletedAt, LocalDateTime.now())
                .eq(KnowledgeGraphEntity::getKgLearningSpaceId, learningSpaceId)
                .execute();
    }

    @Override
    public Optional<Long> findLearningSpaceIdByIdAndUserId(Long graphId, Long userId) {
        KnowledgeGraphEntity knowledgeGraphEntity = QueryChain.of(knowledgeGraphMapper)
                .select(KnowledgeGraphEntity::getKgLearningSpaceId)
                .eq(KnowledgeGraphEntity::getKgId, graphId)
                .eq(KnowledgeGraphEntity::getKgCreatedByUserId, userId)
                .limit(1)
                .get();
        if (knowledgeGraphEntity != null && knowledgeGraphEntity.getKgLearningSpaceId() != null) {
            return Optional.of(knowledgeGraphEntity.getKgLearningSpaceId());
        }
        return Optional.empty();
    }
} 