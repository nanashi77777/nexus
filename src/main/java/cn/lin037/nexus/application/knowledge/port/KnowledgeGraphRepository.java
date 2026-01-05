package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.KnowledgeGraphEntity;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 知识图谱仓储接口
 *
 * @author LinSanQi
 */
public interface KnowledgeGraphRepository {

    /**
     * 根据ID和用户ID判断知识图谱是否存在
     *
     * @param id     知识图谱ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 根据ID查找知识图谱
     *
     * @param id      知识图谱ID
     * @param getters 查询字段
     * @return 知识图谱实体
     */
    Optional<KnowledgeGraphEntity> findById(Long id, List<Getter<KnowledgeGraphEntity>> getters);

    /**
     * 根据用户ID查找知识图谱列表
     *
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 知识图谱列表
     */
    List<KnowledgeGraphEntity> findByUserId(Long userId, List<Getter<KnowledgeGraphEntity>> getters);

    /**
     * 保存知识图谱
     *
     * @param entity 知识图谱实体
     */
    void save(KnowledgeGraphEntity entity);

    /**
     * 更新知识图谱
     *
     * @param id      知识图谱ID
     * @param updater 更新器
     */
    void updateById(Long id, Consumer<UpdateChain> updater);

    /**
     * 删除知识图谱
     *
     * @param id 知识图谱ID
     * @return 是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据学习空间ID删除知识图谱
     *
     * @param learningSpaceId 学习空间ID
     */
    void deleteByLearningSpaceId(Long learningSpaceId);

    /**
     * 根据ID和用户ID查找学习空间ID
     *
     * @param graphId 图谱ID
     * @param userId  用户ID
     * @return 学习空间ID
     */
    Optional<Long> findLearningSpaceIdByIdAndUserId(Long graphId, Long userId);
}