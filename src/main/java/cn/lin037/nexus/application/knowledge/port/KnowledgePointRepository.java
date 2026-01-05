package cn.lin037.nexus.application.knowledge.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 知识点仓储接口
 *
 * @author LinSanQi
 */
public interface KnowledgePointRepository {

    /**
     * 根据ID和用户ID判断知识点是否存在
     *
     * @param id     知识点ID
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * 根据两个知识点ID和用户ID判断两个知识点是否属于同一个学习空间
     *
     * @param pointId1        知识点ID1
     * @param pointId2        知识点ID2
     * @param userId          用户ID
     * @param learningSpaceId 学习空间ID
     * @return 是否属于同一个学习空间
     */
    boolean existsByLearningSpaceAndPointIds(Long pointId1, Long pointId2, Long userId, Long learningSpaceId);

    /**
     * 根据ID查找知识点
     *
     * @param id      知识点ID
     * @param getters 查询字段
     * @return 知识点实体
     */
    Optional<KnowledgePointEntity> findById(Long id, List<Getter<KnowledgePointEntity>> getters);

    /**
     * 根据文件夹ID查找知识点列表
     *
     * @param folderId 文件夹ID
     * @param userId   用户ID
     * @param getters  查询字段
     * @return 知识点列表
     */
    List<KnowledgePointEntity> findByFolderId(Long folderId, Long userId, List<Getter<KnowledgePointEntity>> getters);


    /**
     * 查找不存在的ID列表
     *
     * @param list ID列表
     * @return 知识点ID列表
     */
    List<Long> findDontExistIdsByIds(List<Long> list);


    /**
     * 根据ID和用户ID查找知识点
     *
     * @param pointId 知识点ID
     * @param userId  用户ID
     * @param getters 查询字段
     * @return 知识点实体
     */
    Optional<KnowledgePointEntity> findByIdAndUserId(Long pointId, Long userId, List<Getter<KnowledgePointEntity>> getters);

    /**
     * 根据用户ID和ID列表查找知识点列表
     *
     * @param userId  用户ID
     * @param list    ID列表
     * @param getters 查询字段
     * @return 知识点列表
     */
    List<KnowledgePointEntity> findByUserIdAndIds(Long userId, Collection<Long> list, List<Getter<KnowledgePointEntity>> getters);

    /**
     * 保存知识点
     *
     * @param entity 知识点实体
     */
    void save(KnowledgePointEntity entity);

    /**
     * 批量保存知识点
     *
     * @param entities 知识点实体列表
     */
    void saveBatch(List<KnowledgePointEntity> entities);

    /**
     * 更新知识点
     *
     * @param id      知识点ID
     * @param updater 更新器
     */
    void updateById(Long id, Consumer<UpdateChain> updater);

    /**
     * 删除知识点
     *
     * @param id 知识点ID
     * @return 是否成功
     */
    boolean deleteById(Long id);
}