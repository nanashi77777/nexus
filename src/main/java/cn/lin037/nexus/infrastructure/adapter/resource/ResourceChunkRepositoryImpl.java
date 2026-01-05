package cn.lin037.nexus.infrastructure.adapter.resource;

import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.resource.ResourceChunkMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import db.sql.api.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 资源分片仓储接口实现
 *
 * @author LinSanQi
 */
@Repository
public class ResourceChunkRepositoryImpl implements ResourceChunkRepository {

    private final ResourceChunkMapper resourceChunkMapper;

    public ResourceChunkRepositoryImpl(ResourceChunkMapper resourceChunkMapper) {
        this.resourceChunkMapper = resourceChunkMapper;
    }

    @Override
    public Optional<ResourceChunkEntity> findById(Long chunkId, List<Getter<ResourceChunkEntity>> getters) {
        ResourceChunkEntity chunkEntity = RepositoryUtils.getQueryChainWithFields(resourceChunkMapper, getters)
                .eq(ResourceChunkEntity::getRcId, chunkId)
                .limit(1)
                .get();
        return Optional.ofNullable(chunkEntity);
    }

    @Override
    public List<ResourceChunkEntity> findByIds(List<Long> chunkIds) {
        return QueryChain.of(resourceChunkMapper)
                .in(ResourceChunkEntity::getRcId, chunkIds)
                .list();
    }

    @Override
    public void save(@NotNull ResourceChunkEntity chunkEntity) {
        if (chunkEntity.getRcId() == null) {
            chunkEntity.setRcId(HutoolSnowflakeIdGenerator.generateLongId());
        }
        chunkEntity.setRcCreatedAt(LocalDateTime.now());
        chunkEntity.setRcUpdatedAt(LocalDateTime.now());
        resourceChunkMapper.save(chunkEntity);
    }

    @Override
    public void saveBatch(List<ResourceChunkEntity> chunkEntities) {
        List<ResourceChunkEntity> list = chunkEntities.stream().peek(
                chunkEntity -> {
                    if (chunkEntity.getRcId() == null) {
                        chunkEntity.setRcId(HutoolSnowflakeIdGenerator.generateLongId());
                    }
                    chunkEntity.setRcCreatedAt(LocalDateTime.now());
                    chunkEntity.setRcUpdatedAt(LocalDateTime.now());
                }
        ).toList();
        resourceChunkMapper.saveBatch(list);
    }

    @Override
    public void updateById(Long chunkId, Consumer<ResourceChunkEntity> updater) {
        ResourceChunkEntity chunkEntity = new ResourceChunkEntity();
        chunkEntity.setRcId(chunkId);
        updater.accept(chunkEntity);
        chunkEntity.setRcUpdatedAt(LocalDateTime.now());
        resourceChunkMapper.update(chunkEntity);
    }

    @Override
    public boolean deleteById(Long chunkId) {
        return resourceChunkMapper.deleteById(chunkId) > 0;
    }

    @Override
    public Boolean deleteByResourceId(Long resourceId) {
        return UpdateChain.of(resourceChunkMapper)
                .eq(ResourceChunkEntity::getRcResourceId, resourceId)
                .set(ResourceChunkEntity::getRcDeletedAt, LocalDateTime.now())
                .execute() > 0;
    }

    @Override
    public Boolean areAllChunksInSameLearningSpace(List<Long> chunkIds, Long learningSpaceId) {
        long count = QueryChain.of(resourceChunkMapper)
                .in(ResourceChunkEntity::getRcId, chunkIds)
                .eq(ResourceChunkEntity::getRcLearningSpaceId, learningSpaceId)
                .count();
        return count == chunkIds.size();
    }

    @Override
    public List<ResourceChunkEntity> findByIdsAndLearningSpaceId(List<Long> chunkIds, Long learningSpaceId) {
        return QueryChain.of(resourceChunkMapper)
                .eq(ResourceChunkEntity::getRcLearningSpaceId, learningSpaceId)
                .in(ResourceChunkEntity::getRcId, chunkIds)
                .limit(100)
                .list();
    }

    @Override
    public List<String> findContentsByIds(List<Long> chunkIds) {
        return QueryChain.of(resourceChunkMapper)
                .in(ResourceChunkEntity::getRcId, chunkIds)
                .select(ResourceChunkEntity::getRcContent)
                .list()
                .stream()
                .map(ResourceChunkEntity::getRcContent)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceChunkEntity> findByResourceId(Long resourceId) {
        QueryChain<ResourceChunkEntity> queryChain = QueryChain.of(resourceChunkMapper)
                .eq(ResourceChunkEntity::getRcResourceId, resourceId)
                .orderBy(ResourceChunkEntity::getRcPageIndex, ResourceChunkEntity::getRcChunkIndex);

        return queryChain.list();
    }
}
