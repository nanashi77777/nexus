package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.application.explanation.enums.ExplanationDocumentStatusEnum;
import cn.lin037.nexus.application.explanation.port.ExplanationDocumentRepository;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationDocumentMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 讲解文档仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class ExplanationDocumentRepositoryImpl implements ExplanationDocumentRepository {

    private final ExplanationDocumentMapper documentMapper;

    public ExplanationDocumentRepositoryImpl(ExplanationDocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
    }

    @Override
    public ExplanationDocumentEntity save(ExplanationDocumentEntity entity) {
        documentMapper.save(entity);
        return entity;
    }

    @Override
    public Optional<ExplanationDocumentEntity> findById(Long id) {
        return Optional.ofNullable(QueryChain.of(documentMapper)
                .eq(ExplanationDocumentEntity::getEdId, id)
                .limit(1)
                .get());
    }

    @Override
    public Optional<ExplanationDocumentEntity> findById(Long id, List<Getter<ExplanationDocumentEntity>> getters) {
        return Optional.ofNullable(QueryChain.of(documentMapper)
                .eq(ExplanationDocumentEntity::getEdId, id)
                .limit(1)
                .get());
    }

    @Override
    public Optional<ExplanationDocumentEntity> findByIdAndUserId(Long id, Long userId) {
        return Optional.ofNullable(
                QueryChain.of(documentMapper)
                        .eq(ExplanationDocumentEntity::getEdId, id)
                        .eq(ExplanationDocumentEntity::getEdCreatedByUserId, userId)
                        .get()
        );
    }

    @Override
    public ExplanationDocumentEntity updateById(ExplanationDocumentEntity entity) {
        documentMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public boolean updateStatus(Long id, ExplanationDocumentStatusEnum status) {
        return UpdateChain.of(documentMapper)
                .set(ExplanationDocumentEntity::getEdStatus, status.getCode())
                .set(ExplanationDocumentEntity::getEdUpdatedAt, LocalDateTime.now())
                .eq(ExplanationDocumentEntity::getEdId, id)
                .execute() > 0;
    }

    @Override
    public boolean updateSectionOrder(Long id, List<String> sectionOrder) {
        return UpdateChain.of(documentMapper)
                .set(ExplanationDocumentEntity::getEdSectionOrder, sectionOrder)
                .set(ExplanationDocumentEntity::getEdUpdatedAt, LocalDateTime.now())
                .eq(ExplanationDocumentEntity::getEdId, id)
                .execute() > 0;
    }

    @Override
    public boolean updateGraphConfig(Long id, String graphConfig) {
        return UpdateChain.of(documentMapper)
                .set(ExplanationDocumentEntity::getEdGraphConfig, graphConfig)
                .set(ExplanationDocumentEntity::getEdUpdatedAt, LocalDateTime.now())
                .eq(ExplanationDocumentEntity::getEdId, id)
                .execute() > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(documentMapper)
                .set(ExplanationDocumentEntity::getEdDeletedAt, LocalDateTime.now())
                .eq(ExplanationDocumentEntity::getEdId, id)
                .execute() > 0;
    }

    @Override
    public boolean hasPermission(Long id, Long userId) {
        return QueryChain.of(documentMapper)
                .eq(ExplanationDocumentEntity::getEdId, id)
                .eq(ExplanationDocumentEntity::getEdCreatedByUserId, userId)
                .exists();
    }

    @Override
    public List<ExplanationDocumentEntity> findByLearningSpaceIdAndUserId(Long learningSpaceId, Long userId) {
        return QueryChain.of(documentMapper)
                .eq(ExplanationDocumentEntity::getEdLearningSpaceId, learningSpaceId)
                .eq(ExplanationDocumentEntity::getEdCreatedByUserId, userId)
                .list();
    }
}
