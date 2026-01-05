package cn.lin037.nexus.application.resource.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 资源仓储接口
 *
 * @author LinSanQi
 */
public interface ResourceRepository {

    // ====== 查询操作 ======

    /**
     * 根据学习空间ID和资源标题检查资源是否存在
     *
     * @param learningSpaceId 学习空间ID
     * @param title           资源标题
     * @return boolean
     */
    boolean existsByLearningSpaceIdAndTitle(Long learningSpaceId, String title);

    Boolean existsById(Long resourceId);

    /**
     * 根据ID查询资源
     *
     * @param resourceId 资源ID
     * @param getters    查询字段
     * @return 资源实体
     */
    Optional<ResourceEntity> findById(Long resourceId, List<Getter<ResourceEntity>> getters);

    /**
     * 根据学习空间ID查询资源列表
     *
     * @param learningSpaceId 学习空间ID
     * @param getters         需要查询的字段
     * @return 资源列表
     */
    List<ResourceEntity> findByLearningSpaceId(Long learningSpaceId, List<Getter<ResourceEntity>> getters);

    // ====== 写操作 ======

    /**
     * 保存资源
     *
     * @param resourceEntity 资源实体
     */
    void save(ResourceEntity resourceEntity);

    /**
     * 更新资源
     *
     * @param updater    更新器
     * @param resourceId 资源ID
     */
    void updateById(Long resourceId, Consumer<ResourceEntity> updater);

    /**
     * 根据ID删除资源
     *
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean deleteById(Long resourceId);
}
