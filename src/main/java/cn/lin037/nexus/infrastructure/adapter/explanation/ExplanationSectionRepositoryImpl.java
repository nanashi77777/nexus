package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.application.explanation.port.ExplanationSectionRepository;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationSectionMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 讲解章节仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class ExplanationSectionRepositoryImpl implements ExplanationSectionRepository {

    private final ExplanationSectionMapper sectionMapper;

    public ExplanationSectionRepositoryImpl(ExplanationSectionMapper sectionMapper) {
        this.sectionMapper = sectionMapper;
    }

    @Override
    public ExplanationSectionEntity save(ExplanationSectionEntity entity) {
        sectionMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public Optional<ExplanationSectionEntity> findById(Long id) {
        return Optional.ofNullable(
                QueryChain.of(sectionMapper)
                        .eq(ExplanationSectionEntity::getEsId, id)
                        .get()
        );
    }

    @Override
    public List<ExplanationSectionEntity> findByExplanationDocumentId(Long explanationDocumentId) {
        return QueryChain.of(sectionMapper)
                .eq(ExplanationSectionEntity::getEsExplanationDocumentId, explanationDocumentId)
                .list();
    }

    @Override
    public List<ExplanationSectionEntity> findByCreatedByUserId(Long createdByUserId) {
        return QueryChain.of(sectionMapper)
                .eq(ExplanationSectionEntity::getEsCreatedByUserId, createdByUserId)
                .list();
    }

    @Override
    public ExplanationSectionEntity update(ExplanationSectionEntity entity) {
        sectionMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(sectionMapper)
                .set(ExplanationSectionEntity::getEsDeletedAt, LocalDateTime.now())
                .eq(ExplanationSectionEntity::getEsId, id)
                .execute() > 0;
    }

    @Override
    public int deleteByExplanationDocumentId(Long explanationDocumentId) {
        return UpdateChain.of(sectionMapper)
                .set(ExplanationSectionEntity::getEsDeletedAt, LocalDateTime.now())
                .eq(ExplanationSectionEntity::getEsExplanationDocumentId, explanationDocumentId)
                .execute();
    }

    @Override
    public int saveBatch(List<ExplanationSectionEntity> sectionEntities) {
        return sectionMapper.saveBatch(sectionEntities);
    }
}
