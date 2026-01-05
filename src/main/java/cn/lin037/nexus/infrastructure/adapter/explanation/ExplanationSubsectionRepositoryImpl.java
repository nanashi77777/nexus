package cn.lin037.nexus.infrastructure.adapter.explanation;

import cn.lin037.nexus.application.explanation.port.ExplanationSubsectionRepository;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationSectionMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.explanation.ExplanationSubsectionMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 讲解小节仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class ExplanationSubsectionRepositoryImpl implements ExplanationSubsectionRepository {

    private final ExplanationSubsectionMapper subsectionMapper;
    private final ExplanationSectionMapper sectionMapper;

    public ExplanationSubsectionRepositoryImpl(ExplanationSubsectionMapper subsectionMapper,
                                               ExplanationSectionMapper sectionMapper) {
        this.subsectionMapper = subsectionMapper;
        this.sectionMapper = sectionMapper;
    }

    @Override
    public ExplanationSubsectionEntity save(ExplanationSubsectionEntity entity) {
        subsectionMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public Optional<ExplanationSubsectionEntity> findById(Long id) {
        return Optional.ofNullable(
                QueryChain.of(subsectionMapper)
                        .eq(ExplanationSubsectionEntity::getEssId, id)
                        .get()
        );
    }

    @Override
    public List<ExplanationSubsectionEntity> findBySectionId(Long sectionId) {
        return QueryChain.of(subsectionMapper)
                .eq(ExplanationSubsectionEntity::getEssSectionId, sectionId)
                .list();
    }

    @Override
    public ExplanationSubsectionEntity update(ExplanationSubsectionEntity entity) {
        subsectionMapper.saveOrUpdate(entity);
        return entity;
    }

    @Override
    public boolean updateSectionId(Long id, Long sectionId) {
        return UpdateChain.of(subsectionMapper)
                .set(ExplanationSubsectionEntity::getEssSectionId, sectionId)
                .set(ExplanationSubsectionEntity::getEssUpdatedAt, LocalDateTime.now())
                .eq(ExplanationSubsectionEntity::getEssId, id)
                .execute() > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return UpdateChain.of(subsectionMapper)
                .set(ExplanationSubsectionEntity::getEssDeletedAt, LocalDateTime.now())
                .eq(ExplanationSubsectionEntity::getEssId, id)
                .execute() > 0;
    }

    @Override
    public int deleteByExplanationDocumentId(Long explanationDocumentId) {
        // 先查询出所有相关章节的ID
        List<ExplanationSectionEntity> sections = QueryChain.of(sectionMapper)
                .eq(ExplanationSectionEntity::getEsExplanationDocumentId, explanationDocumentId)
                .list();

        if (sections.isEmpty()) {
            return 0;
        }

        List<Long> sectionIds = sections.stream()
                .map(ExplanationSectionEntity::getEsId)
                .toList();

        // 根据章节ID删除小节
        return UpdateChain.of(subsectionMapper)
                .set(ExplanationSubsectionEntity::getEssDeletedAt, LocalDateTime.now())
                .in(ExplanationSubsectionEntity::getEssSectionId, sectionIds)
                .execute();
    }

    @Override
    public int saveBatch(List<ExplanationSubsectionEntity> sectionEntities) {
        return subsectionMapper.saveBatch(sectionEntities);
    }
}
