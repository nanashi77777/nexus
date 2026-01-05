package cn.lin037.nexus.application.explanation.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 讲解小节Repository接口
 *
 * @author LinSanQi
 */
public interface ExplanationSubsectionRepository {

    /**
     * 保存讲解小节
     *
     * @param entity 讲解小节实体
     * @return 保存后的实体
     */
    ExplanationSubsectionEntity save(ExplanationSubsectionEntity entity);

    /**
     * 根据ID查询讲解小节
     *
     * @param id 小节ID
     * @return 讲解小节实体
     */
    Optional<ExplanationSubsectionEntity> findById(Long id);

    /**
     * 根据章节ID查询小节列表
     *
     * @param sectionId 章节ID
     * @return 小节列表
     */
    List<ExplanationSubsectionEntity> findBySectionId(Long sectionId);

    /**
     * 更新讲解小节
     *
     * @param entity 讲解小节实体
     * @return 更新后的实体
     */
    ExplanationSubsectionEntity update(ExplanationSubsectionEntity entity);

    /**
     * 更新小节的章节归属
     *
     * @param id        小节ID
     * @param sectionId 新的章节ID
     * @return 是否更新成功
     */
    boolean updateSectionId(Long id, Long sectionId);

    /**
     * 删除讲解小节
     *
     * @param id 小节ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据讲解文档ID删除所有小节
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 删除的小节数量
     */
    int deleteByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 批量保存小节
     *
     * @param subsectionEntities 小节实体列表
     */
    int saveBatch(List<ExplanationSubsectionEntity> subsectionEntities);
}