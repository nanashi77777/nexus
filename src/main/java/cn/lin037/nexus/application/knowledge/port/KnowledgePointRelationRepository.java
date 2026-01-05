package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 知识点关系仓储接口
 *
 * @author LinSanQi
 */
public interface KnowledgePointRelationRepository {

    /**
     * 根据ID和用户ID判断关系是否存在
     *
     * @param relationId 关系ID
     * @param userId     用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long relationId, Long userId);

    /**
     * 根据ID查找关系
     *
     * @param id      关系ID
     * @param getters 查询字段
     * @return 关系实体
     */
    Optional<KnowledgePointRelationEntity> findById(Long id, List<Getter<KnowledgePointRelationEntity>> getters);

    /**
     * 根据ID列表查找关系
     *
     * @param ids     关系ID列表
     * @param getters 获取字段
     * @return 关系实体列表
     */
    List<KnowledgePointRelationEntity> findByIds(List<Long> ids, List<Getter<KnowledgePointRelationEntity>> getters);

    /**
     * 根据源知识点ID查找关系列表
     *
     * @param sourcePointId 源知识点ID
     * @param userId        用户ID
     * @param getters       查询字段
     * @return 关系列表
     */
    List<KnowledgePointRelationEntity> findBySourcePointId(Long sourcePointId, Long userId, List<Getter<KnowledgePointRelationEntity>> getters);


    /**
     * 保存关系
     *
     * @param entity 关系实体
     */
    void save(KnowledgePointRelationEntity entity);

    /**
     * 更新关系
     *
     * @param id      关系ID
     * @param updater 更新器
     */
    void updateById(Long id, Consumer<UpdateChain> updater);

    /**
     * 删除关系
     *
     * @param id 关系ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据知识点ID删除关系
     *
     * @param pointId 知识点ID
     * @return 删除数量
     */
    int deleteByPointId(Long pointId);

    /**
     * 根据知识点ID集合查找相互之间的关系
     *
     * @param pointIds 知识点ID集合
     * @param userId   用户ID
     * @param getters  查询字段
     * @return 关系列表
     */
    List<KnowledgePointRelationEntity> findBetweenPointIds(Collection<Long> pointIds, Long userId, List<Getter<KnowledgePointRelationEntity>> getters);

    /**
     * 批量保存关系
     *
     * @param entities 关系实体列表
     */
    void saveBatch(List<KnowledgePointRelationEntity> entities);
}