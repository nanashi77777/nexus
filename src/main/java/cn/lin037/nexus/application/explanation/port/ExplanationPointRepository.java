package cn.lin037.nexus.application.explanation.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationPointEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 讲解知识点Repository接口
 *
 * @author LinSanQi
 */
public interface ExplanationPointRepository {

    /**
     * 保存讲解知识点
     *
     * @param entity 讲解知识点实体
     * @return 保存后的实体
     */
    ExplanationPointEntity save(ExplanationPointEntity entity);

    /**
     * 根据ID查询讲解知识点
     *
     * @param id 知识点ID
     * @return 讲解知识点实体
     */
    Optional<ExplanationPointEntity> findById(Long id);

    /**
     * 根据讲解文档ID查询知识点列表
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 知识点列表
     */
    List<ExplanationPointEntity> findByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 根据创建者ID查询知识点列表
     *
     * @param createdByUserId 创建者用户ID
     * @return 知识点列表
     */
    List<ExplanationPointEntity> findByCreatedByUserId(Long createdByUserId);

    /**
     * 根据讲解文档ID和创建者ID查询知识点列表
     *
     * @param explanationDocumentId 讲解文档ID
     * @param createdByUserId       创建者用户ID
     * @return 知识点列表
     */
    List<ExplanationPointEntity> findByExplanationDocumentIdAndCreatedByUserId(Long explanationDocumentId, Long createdByUserId);

    /**
     * 根据知识点ID列表查询知识点
     *
     * @param pointIds 知识点ID列表
     * @return 知识点列表
     */
    List<ExplanationPointEntity> findByIds(List<Long> pointIds);

    /**
     * 更新讲解知识点
     *
     * @param entity 讲解知识点实体
     * @return 更新后的实体
     */
    ExplanationPointEntity update(ExplanationPointEntity entity);

    /**
     * 更新节点样式配置
     *
     * @param id              知识点ID
     * @param nodeStyleConfig 节点样式配置
     * @return 是否更新成功
     */
    boolean updateNodeStyleConfig(Long id, String nodeStyleConfig);

    /**
     * 批量更新节点样式配置
     *
     * @param styleConfigMap 知识点ID与样式配置的映射
     * @return 更新成功的数量
     */
    int batchUpdateNodeStyleConfig(Map<Long, String> styleConfigMap);

    /**
     * 删除讲解知识点
     *
     * @param id 知识点ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 根据讲解文档ID删除所有知识点
     *
     * @param explanationDocumentId 讲解文档ID
     * @return 删除的知识点数量
     */
    int deleteByExplanationDocumentId(Long explanationDocumentId);

    /**
     * 根据知识点ID列表批量删除
     *
     * @param pointIds 知识点ID列表
     * @return 删除的知识点数量
     */
    int deleteBatchByIds(List<Long> pointIds);

    /**
     * 检查讲解知识点是否存在
     *
     * @param id 知识点ID
     * @return 是否存在
     */
    boolean existsById(Long id);

    /**
     * 检查用户是否有权限访问讲解知识点
     *
     * @param id     知识点ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long id, Long userId);

    /**
     * 检查知识点是否属于指定的讲解文档
     *
     * @param pointId               知识点ID
     * @param explanationDocumentId 讲解文档ID
     * @return 是否属于
     */
    boolean belongsToDocument(Long pointId, Long explanationDocumentId);

    /**
     * 保存多个讲解点
     *
     * @param explanationPointEntities 讲解点实体列表
     */
    void saveBatch(List<ExplanationPointEntity> explanationPointEntities);
}