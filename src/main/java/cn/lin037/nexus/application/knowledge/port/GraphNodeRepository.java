package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 图谱节点仓储接口
 *
 * @author LinSanQi
 */
public interface GraphNodeRepository {

    /**
     * 根据ID查找节点
     *
     * @param id      节点ID
     * @param getters 查询字段
     * @return 节点实体
     */
    Optional<GraphNodeEntity> findById(Long id, List<Getter<GraphNodeEntity>> getters);

    /**
     * 根据ID与用户ID查找节点
     *
     * @param id      节点ID
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 节点列表
     */
    Optional<GraphNodeEntity> findByIdAndUserId(Long id, Long userId, List<Getter<GraphNodeEntity>> getters);

    /**
     * 保存节点
     *
     * @param entity 节点实体
     */
    void save(GraphNodeEntity entity);

    /**
     * 批量保存节点
     *
     * @param entities 节点实体列表
     */
    void saveBatch(List<GraphNodeEntity> entities);

    /**
     * 更新节点
     *
     * @param id      节点ID
     * @param updater 更新器
     */
    void updateById(Long id, Consumer<UpdateChain> updater);

    /**
     * 删除节点
     *
     * @param id 节点ID
     * @return 是否成功
     */
    boolean deleteById(Long id);


    /**
     * 解除节点与知识点实体的关联
     *
     * @param entityId 知识点实体ID
     */
    void disassociateEntity(Long entityId);

    /**
     * 根据图谱ID删除节点
     *
     * @param graphId 图谱ID
     */
    void deleteByGraphId(Long graphId);

    /**
     * 根据用户ID判断节点是否存在
     *
     * @param id     节点ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 根据知识点实体ID查找所有投影节点
     *
     * @param entityId 知识点实体ID
     * @param getters  查询字段
     * @return 节点列表
     */
    List<GraphNodeEntity> findByEntityId(Long entityId, List<Getter<GraphNodeEntity>> getters);

    /**
     * 根据ID列表查找节点
     *
     * @param ids     节点ID列表
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 节点列表
     */
    Optional<List<GraphNodeEntity>> findByIdsAndUserId(List<Long> ids, Long userId, List<Getter<GraphNodeEntity>> getters);
}
