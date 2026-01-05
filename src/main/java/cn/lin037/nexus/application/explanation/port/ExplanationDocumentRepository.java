package cn.lin037.nexus.application.explanation.port;

import cn.lin037.nexus.application.explanation.enums.ExplanationDocumentStatusEnum;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

/**
 * 讲解文档Repository接口
 *
 * @author LinSanQi
 */
public interface ExplanationDocumentRepository {

    /**
     * 保存讲解文档
     *
     * @param entity 讲解文档实体
     * @return 保存后的实体
     */
    ExplanationDocumentEntity save(ExplanationDocumentEntity entity);

    /**
     * 根据ID查询讲解文档
     *
     * @param id 文档ID
     * @return 讲解文档实体
     */
    Optional<ExplanationDocumentEntity> findById(Long id);

    /**
     * 根据ID查询讲解文档
     *
     * @param id 文档ID
     * @return 讲解文档实体
     */
    Optional<ExplanationDocumentEntity> findById(Long id, List<Getter<ExplanationDocumentEntity>> getters);

    /**
     * 根据ID查询讲解文档
     *
     * @param id 文档ID
     * @return 讲解文档实体
     */
    Optional<ExplanationDocumentEntity> findByIdAndUserId(Long id, Long userId);

    /**
     * 更新讲解文档
     *
     * @param entity 讲解文档实体
     * @return 更新后的实体
     */
    ExplanationDocumentEntity updateById(ExplanationDocumentEntity entity);

    /**
     * 更新文档状态
     *
     * @param id     文档ID
     * @param status 新状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, ExplanationDocumentStatusEnum status);

    /**
     * 更新章节顺序
     *
     * @param id           文档ID
     * @param sectionOrder 章节顺序
     * @return 是否更新成功
     */
    boolean updateSectionOrder(Long id, List<String> sectionOrder);

    /**
     * 更新图谱配置
     *
     * @param id          文档ID
     * @param graphConfig 图谱配置
     * @return 是否更新成功
     */
    boolean updateGraphConfig(Long id, String graphConfig);

    /**
     * 删除讲解文档
     *
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 检查用户是否有权限访问讲解文档
     *
     * @param id     文档ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long id, Long userId);

    /**
     * 根据学习空间ID查询文档列表
     *
     * @param learningSpaceId 学习空间ID
     * @param userId          用户ID
     * @return 文档列表
     */
    List<ExplanationDocumentEntity> findByLearningSpaceIdAndUserId(Long learningSpaceId, Long userId);
}