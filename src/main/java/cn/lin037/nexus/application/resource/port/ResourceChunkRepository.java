package cn.lin037.nexus.application.resource.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import db.sql.api.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 资源分片仓储接口
 *
 * @author LinSanQi
 */
public interface ResourceChunkRepository {

    /**
     * 根据ID查找分片
     *
     * @param chunkId 分片ID
     * @param getters 查询字段
     * @return 分片实体
     */
    Optional<ResourceChunkEntity> findById(Long chunkId, List<Getter<ResourceChunkEntity>> getters);

    /**
     * 根据ID列表查找分片
     *
     * @param chunkIds 分片ID列表
     * @return 分片实体列表
     */
    List<ResourceChunkEntity> findByIds(List<Long> chunkIds);

    /**
     * 保存分片
     *
     * @param chunkEntity 分片实体
     */
    void save(@NotNull ResourceChunkEntity chunkEntity);

    /**
     * 批量保存分片
     *
     * @param chunkEntities 分片实体列表
     */
    void saveBatch(List<ResourceChunkEntity> chunkEntities);

    /**
     * 根据ID更新分片
     *
     * @param chunkId 分片ID
     * @param updater 更新器
     */
    void updateById(Long chunkId, Consumer<ResourceChunkEntity> updater);

    /**
     * 根据ID删除分片
     *
     * @param chunkId 分片ID
     * @return 是否成功
     */
    boolean deleteById(Long chunkId);

    /**
     * 根据资源ID删除所有分片
     *
     * @param resourceId 资源ID
     * @return 是否成功
     */
    Boolean deleteByResourceId(Long resourceId);

    /**
     * 检查所有分片是否属于同一个学习空间
     *
     * @param chunkIds        分片ID列表
     * @param learningSpaceId 学习空间ID
     * @return 是否属于同一个学习空间
     */
    Boolean areAllChunksInSameLearningSpace(List<Long> chunkIds, Long learningSpaceId);

    /**
     * 根据分片ID列表和学习空间ID查询分片
     *
     * @param chunkIds        分片ID列表
     * @param learningSpaceId 学习空间ID
     * @return 分片列表
     */
    List<ResourceChunkEntity> findByIdsAndLearningSpaceId(List<Long> chunkIds, Long learningSpaceId);

    /**
     * 根据分片ID列表查询分片内容
     *
     * @param chunkIds 分片ID列表
     * @return 分片内容列表
     */
    List<String> findContentsByIds(List<Long> chunkIds);

    /**
     * 根据资源ID查找分片
     * @param resourceId 资源ID
     * @return 分片实体列表
     */
    List<ResourceChunkEntity> findByResourceId(Long resourceId);
}