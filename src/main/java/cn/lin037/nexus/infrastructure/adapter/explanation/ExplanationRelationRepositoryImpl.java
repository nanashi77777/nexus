package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.application.explanation.port.ExplanationRelationRepository;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationRelationMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 讲解关系仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class ExplanationRelationRepositoryImpl implements ExplanationRelationRepository {

    private final ExplanationRelationMapper relationMapper;

    public ExplanationRelationRepositoryImpl(ExplanationRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Override
    public ExplanationRelationEntity save(ExplanationRelationEntity entity) {
        relationMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public Optional<ExplanationRelationEntity> findById(Long id) {
        return Optional.ofNullable(
                QueryChain.of(relationMapper)
                        .eq(ExplanationRelationEntity::getErId, id)
                        .get()
        );
    }

    @Override
    public ExplanationRelationEntity update(ExplanationRelationEntity entity) {
        relationMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(relationMapper)
                .set(ExplanationRelationEntity::getErDeletedAt, LocalDateTime.now())
                .eq(ExplanationRelationEntity::getErId, id)
                .execute() > 0;
    }

    @Override
    public int deleteByExplanationDocumentId(Long explanationDocumentId) {
        return UpdateChain.of(relationMapper)
                .set(ExplanationRelationEntity::getErDeletedAt, LocalDateTime.now())
                .eq(ExplanationRelationEntity::getErExplanationDocumentId, explanationDocumentId)
                .execute();
    }

    @Override
    public void saveBatch(List<ExplanationRelationEntity> explanationRelationEntities) {
        relationMapper.saveBatch(explanationRelationEntities);
    }
}
