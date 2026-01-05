package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;

public interface KnowledgePointVersionRepository {

    /**
     * 判断指定版本ID是否存在
     *
     * @param versionId 版本ID
     * @param pointId   用户ID
     * @return 是否存在
     */
    boolean existsByIdAndPointId(Long versionId, Long pointId);

    /**
     * 根据版本ID列表查询版本信息
     *
     * @param currentVersionIds 版本ID列表
     * @param getters           获取字段列表
     * @return 版本信息
     */
    List<KnowledgePointVersionEntity> findByIds(List<Long> currentVersionIds, List<Getter<KnowledgePointVersionEntity>> getters);

    /**
     * 保存知识点版本
     *
     * @param entity 版本实体
     * @return 版本ID
     */
    Optional<Long> save(KnowledgePointVersionEntity entity);

    /**
     * 批量保存知识点版本
     *
     * @param entities 知识点版本实体列表
     */
    void saveBatch(List<KnowledgePointVersionEntity> entities);

    /**
     * 保存指定版本
     *
     * @param versionId       版本ID
     * @param createdByUserId 用户ID
     * @return 版本，如果为空则表示versionId的版本不存在
     */
    Optional<KnowledgePointVersionEntity> saveFromVersionId(Long versionId, Long createdByUserId);

    /**
     * 删除指定知识点的版本
     *
     * @param pointId 知识点ID
     */
    void deleteByPointId(Long pointId);

    /**
     * 根据知识点ID列表和学习空间ID查询知识点当前版本的信息
     *
     * @param knowledgePointIds 知识点ID列表
     * @param userId            用户ID
     * @param getters           获取字段列表
     * @return 知识点的当前版本列表
     */
    List<KnowledgePointVersionEntity> findCurrentVersionsByPointIdsAndUserId(List<Long> knowledgePointIds, Long userId, List<Getter<KnowledgePointVersionEntity>> getters);
}
