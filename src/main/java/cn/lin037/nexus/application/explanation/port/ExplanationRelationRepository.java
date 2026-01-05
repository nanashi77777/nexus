package cn.lin037.nexus.application.explanation.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationRelationEntity;

import java.util.List;
import java.util.Optional;

/**
 * 讲解关系Repository接口
 *
 * @author LinSanQi
 */
public interface ExplanationRelationRepository {

    /**
     * 保存讲解关系
     *
     * @param entity 讲解关系实体
     * @return 保存后的实体
     */
    ExplanationRelationEntity save(ExplanationRelationEntity entity);

    /**
     * 根据ID查询讲解关系
     *
     * @param id 关系ID
     * @return 讲解关系实体
     */
    Optional<ExplanationRelationEntity> findById(Long id);

    /**
     * 更新讲解关系
     *
     * @param entity 讲解关系实体
     * @return 更新后的实体
     */
    ExplanationRelationEntity update(ExplanationRelationEntity entity);

    /**
     * 删除讲解关系
     *
     * @param id 关系ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据讲解文档ID删除所有关系
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 删除的关系数量
     */
    int deleteByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 批量新增
     *
     * @param explanationRelationEntities 关系列表
     */
    void saveBatch(List<ExplanationRelationEntity> explanationRelationEntities);
}