package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.application.explanation.port.ExplanationPointRepository;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationPointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationPointMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 讲解知识点仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class ExplanationPointRepositoryImpl implements ExplanationPointRepository {

    private final ExplanationPointMapper pointMapper;

    public ExplanationPointRepositoryImpl(ExplanationPointMapper pointMapper) {
        this.pointMapper = pointMapper;
    }

    @Override
    public ExplanationPointEntity save(ExplanationPointEntity entity) {
        pointMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public Optional<ExplanationPointEntity> findById(Long id) {
        return Optional.ofNullable(pointMapper.getById(id));
    }

    @Override
    public List<ExplanationPointEntity> findByExplanationDocumentId(Long explanationDocumentId) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpExplanationDocumentId, explanationDocumentId)
                .list();
    }

    @Override
    public List<ExplanationPointEntity> findByCreatedByUserId(Long createdByUserId) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpCreatedByUserId, createdByUserId)
                .list();
    }

    @Override
    public List<ExplanationPointEntity> findByExplanationDocumentIdAndCreatedByUserId(Long explanationDocumentId, Long createdByUserId) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpExplanationDocumentId, explanationDocumentId)
                .eq(ExplanationPointEntity::getEpCreatedByUserId, createdByUserId)
                .list();
    }

    @Override
    public List<ExplanationPointEntity> findByIds(List<Long> pointIds) {
        if (pointIds.isEmpty()) {
            return List.of();
        }
        return QueryChain.of(pointMapper)
                .in(ExplanationPointEntity::getEpId, pointIds)
                .list();
    }

    @Override
    public ExplanationPointEntity update(ExplanationPointEntity entity) {
        pointMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public boolean updateNodeStyleConfig(Long id, String nodeStyleConfig) {
        return UpdateChain.of(pointMapper)
                .set(ExplanationPointEntity::getEpStyleConfig, nodeStyleConfig)
                .set(ExplanationPointEntity::getEpUpdatedAt, LocalDateTime.now())
                .eq(ExplanationPointEntity::getEpId, id)
                .execute() > 0;
    }

    @Override
    public int batchUpdateNodeStyleConfig(Map<Long, String> styleConfigMap) {
        if (styleConfigMap.isEmpty()) {
            return 0;
        }

        int updateCount = 0;
        for (Map.Entry<Long, String> entry : styleConfigMap.entrySet()) {
            if (updateNodeStyleConfig(entry.getKey(), entry.getValue())) {
                updateCount++;
            }
        }
        return updateCount;
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(pointMapper)
                .set(ExplanationPointEntity::getEpDeletedAt, LocalDateTime.now())
                .eq(ExplanationPointEntity::getEpId, id)
                .execute() > 0;
    }

    @Override
    public int deleteByExplanationDocumentId(Long explanationDocumentId) {
        return UpdateChain.of(pointMapper)
                .set(ExplanationPointEntity::getEpDeletedAt, LocalDateTime.now())
                .eq(ExplanationPointEntity::getEpExplanationDocumentId, explanationDocumentId)
                .execute();
    }

    @Override
    public int deleteBatchByIds(List<Long> pointIds) {
        if (pointIds.isEmpty()) {
            return 0;
        }
        return UpdateChain.of(pointMapper)
                .set(ExplanationPointEntity::getEpDeletedAt, LocalDateTime.now())
                .in(ExplanationPointEntity::getEpId, pointIds)
                .execute();
    }

    @Override
    public boolean existsById(Long id) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpId, id)
                .exists();
    }

    @Override
    public boolean hasPermission(Long id, Long userId) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpId, id)
                .eq(ExplanationPointEntity::getEpCreatedByUserId, userId)
                .exists();
    }

    @Override
    public boolean belongsToDocument(Long pointId, Long explanationDocumentId) {
        return QueryChain.of(pointMapper)
                .eq(ExplanationPointEntity::getEpId, pointId)
                .eq(ExplanationPointEntity::getEpExplanationDocumentId, explanationDocumentId)
                .exists();
    }

    @Override
    public void saveBatch(List<ExplanationPointEntity> explanationPointEntities) {
        pointMapper.saveBatch(explanationPointEntities);
    }
}
