package cn.lin037.nexus.application.explanation.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;

import java.util.List;
import java.util.Optional;

/**
 * 讲解章节Repository接口
 *
 * @author LinSanQi
 */
public interface ExplanationSectionRepository {

    /**
     * 保存讲解章节
     *
     * @param entity 讲解章节实体
     * @return 保存后的实体
     */
    ExplanationSectionEntity save(ExplanationSectionEntity entity);

    /**
     * 根据ID查询讲解章节
     *
     * @param id 章节ID
     * @return 讲解章节实体
     */
    Optional<ExplanationSectionEntity> findById(Long id);

    /**
     * 根据讲解文档ID查询章节列表
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 章节列表
     */
    List<ExplanationSectionEntity> findByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 根据创建者ID查询章节列表
     *
     * @param createdByUserId 创建者用户ID
     * @return 章节列表
     */
    List<ExplanationSectionEntity> findByCreatedByUserId(Long createdByUserId);

    /**
     * 更新讲解章节
     *
     * @param entity 讲解章节实体
     * @return 更新后的实体
     */
    ExplanationSectionEntity update(ExplanationSectionEntity entity);

    /**
     * 删除讲解章节
     *
     * @param id 章节ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据讲解文档ID删除所有章节
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 删除的章节数量
     */
    int deleteByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 批量保存讲解章节
     *
     * @param sectionEntities 讲解章节实体列表
     * @return 保存成功的章节数量
     */
    int saveBatch(List<ExplanationSectionEntity> sectionEntities);
}