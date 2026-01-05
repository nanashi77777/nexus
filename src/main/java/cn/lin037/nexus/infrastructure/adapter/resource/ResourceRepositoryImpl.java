package cn.lin037.nexus.infrastructure.adapter.resource;

import cn.lin037.nexus.application.resource.port.ResourceRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.LearningSpaceMapper;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.resource.ResourceMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import db.sql.api.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 资源仓储接口实现
 *
 * @author LinSanQi
 */
@Repository
public class ResourceRepositoryImpl implements ResourceRepository {

    private final ResourceMapper resourceMapper;
    private final LearningSpaceMapper learningSpaceMapper;

    public ResourceRepositoryImpl(ResourceMapper resourceMapper,LearningSpaceMapper learningSpaceMapper) {
        this.resourceMapper = resourceMapper;
        this.learningSpaceMapper = learningSpaceMapper;
    }


    @Override
    public boolean existsByLearningSpaceIdAndTitle(Long learningSpaceId, String title) {
        return QueryChain.of(resourceMapper)
                .eq(ResourceEntity::getRsLearningSpaceId, learningSpaceId)
                .eq(ResourceEntity::getRsTitle, title)
                .exists();
    }

    @Override
    public Boolean existsById(Long resourceId) {
        return QueryChain.of(resourceMapper)
                .eq(ResourceEntity::getRsId, resourceId)
                .exists();
    }

    @Override
    public Optional<ResourceEntity> findById(Long resourceId, List<Getter<ResourceEntity>> getters) {
        ResourceEntity resourceEntity = RepositoryUtils.getQueryChainWithFields(resourceMapper, getters)
                .eq(ResourceEntity::getRsId, resourceId)
                .limit(1)
                .get();
        return Optional.ofNullable(resourceEntity);
    }

    @Override
    public List<ResourceEntity> findByLearningSpaceId(Long learningSpaceId, List<Getter<ResourceEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(resourceMapper, getters)
                .eq(ResourceEntity::getRsLearningSpaceId, learningSpaceId)
                .list();
    }

    @Override
    public void save(@NotNull ResourceEntity resourceEntity) {
        if (resourceEntity.getRsId() == null) {
            resourceEntity.setRsId(HutoolSnowflakeIdGenerator.generateLongId());
        }
        LocalDateTime now = LocalDateTime.now();
        resourceEntity.setRsCreatedAt(now);
        resourceEntity.setRsUpdatedAt(now);
        resourceMapper.save(resourceEntity);
    }

    @Override
    public void updateById(Long resourceId, Consumer<ResourceEntity> updater) {
        ResourceEntity resourceEntity = new ResourceEntity();
        resourceEntity.setRsId(resourceId);
        resourceEntity.setRsUpdatedAt(LocalDateTime.now());
        updater.accept(resourceEntity);
        resourceMapper.update(resourceEntity);
    }

    @Override
    public boolean deleteById(Long resourceId) {
        return resourceMapper.deleteById(resourceId) > 0;
    }
}
