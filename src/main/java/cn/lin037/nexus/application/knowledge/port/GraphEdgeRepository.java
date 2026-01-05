package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 图谱边仓储接口
 *
 * @author LinSanQi
 */
public interface GraphEdgeRepository {

    /**
     * 根据ID查找边
     *
     * @param id      边ID
     * @param getters 查询字段
     * @return 边实体
     */
    Optional<GraphEdgeEntity> findById(Long id, List<Getter<GraphEdgeEntity>> getters);

    /**
     * 根据图谱ID查找边列表
     *
     * @param graphId 图谱ID
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 边列表
     */
    List<GraphEdgeEntity> findByGraphId(Long graphId, Long userId, List<Getter<GraphEdgeEntity>> getters);

    /**
     * 保存边
     *
     * @param entity 边实体
     */
    void save(GraphEdgeEntity entity);

    /**
     * 更新边
     *
     * @param id      边ID
     * @param updater 更新器
     */
    void updateById(Long id, Consumer<UpdateChain> updater);

    /**
     * 删除边
     *
     * @param id 边ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据ID和用户ID判断边是否存在
     *
     * @param id     边ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 实体化边（将虚体边转换为知识点关系实体的投影）
     *
     * @param edgeId     边ID
     * @param relationId 知识点关系实体ID
     */
    void materialize(Long edgeId, Long relationId);

    /**
     * 解除边与知识点关系实体的关联
     *
     * @param relationId 知识点关系实体ID
     */
    void disassociateRelation(Long relationId);

    /**
     * 根据图谱ID删除边
     *
     * @param graphId 图谱ID
     */
    void deleteByGraphId(Long graphId);

    /**
     * 根据节点ID删除相关边
     *
     * @param nodeId 节点ID
     */
    void deleteByNodeId(Long nodeId);

    /**
     * 根据关系实体ID查找所有投影边
     *
     * @param relationId 关系实体ID
     * @param getters    查询字段
     * @return 边列表
     */
    List<GraphEdgeEntity> findByRelationId(Long relationId, List<Getter<GraphEdgeEntity>> getters);

    /**
     * 根据节点ID查找相关边
     *
     * @param nodeId  节点ID
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 边列表
     */
    List<GraphEdgeEntity> findByNodeId(Long nodeId, Long userId, List<Getter<GraphEdgeEntity>> getters);

    /**
     * 批量保存边
     *
     * @param entities 边实体列表
     */
    void saveBatch(List<GraphEdgeEntity> entities);

    /**
     * 批量实体化边
     *
     * @param edgeIds     边ID列表
     * @param relationIds 对应的关系实体ID列表
     */
    void batchMaterialize(List<Long> edgeIds, List<Long> relationIds);

    /**
     * 根据节点ID列表查找其中涉及到的边
     *
     * @param nodeIds 节点ID列表
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 边列表
     */
    List<GraphEdgeEntity> findBetweenNodeIds(List<Long> nodeIds, Long userId, List<Getter<GraphEdgeEntity>> getters);
}