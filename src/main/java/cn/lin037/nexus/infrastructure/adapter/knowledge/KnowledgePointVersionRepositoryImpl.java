package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.application.knowledge.port.KnowledgePointVersionRepository;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgePointVersionMapper;
import cn.xbatis.core.sql.executor.SubQuery;
import cn.xbatis.core.sql.executor.Where;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import db.sql.api.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KnowledgePointVersionRepositoryImpl implements KnowledgePointVersionRepository {

    private final KnowledgePointVersionMapper knowledgePointVersionMapper;

    public KnowledgePointVersionRepositoryImpl(KnowledgePointVersionMapper knowledgePointVersionMapper) {
        this.knowledgePointVersionMapper = knowledgePointVersionMapper;
    }

    @Override
    public boolean existsByIdAndPointId(Long versionId, Long pointId) {
        return QueryChain.of(knowledgePointVersionMapper)
                .eq(KnowledgePointVersionEntity::getKpvId, versionId)
                .eq(KnowledgePointVersionEntity::getKpvKnowledgePointId, pointId)
                .exists();
    }

    @Override
    public List<KnowledgePointVersionEntity> findByIds(List<Long> currentVersionIds, List<Getter<KnowledgePointVersionEntity>> getters) {
        return RepositoryUtils.getQueryChainWithFields(knowledgePointVersionMapper, getters)
                .eq(KnowledgePointVersionEntity::getKpvId, currentVersionIds)
                .list();
    }

    @Override
    public Optional<Long> save(KnowledgePointVersionEntity entity) {
        if (entity.getKpvId() == null) {
            entity.setKpvId(HutoolSnowflakeIdGenerator.generateLongId());
        }
        knowledgePointVersionMapper.save(entity);
        return Optional.ofNullable(entity.getKpvId());
    }

    @Override
    public Optional<KnowledgePointVersionEntity> saveFromVersionId(Long versionId, Long createdByUserId) {
        KnowledgePointVersionEntity targetVersion = QueryChain.of(knowledgePointVersionMapper)
                .eq(KnowledgePointVersionEntity::getKpvId, versionId)
                .limit(1)
                .get();

        if (targetVersion == null) {
            // 目标版本不存在
            // throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "目标版本不存在");
            return Optional.empty();
        }

        KnowledgePointVersionEntity newVersion = new KnowledgePointVersionEntity();
        BeanUtil.copyProperties(targetVersion, newVersion, "kpvId", "kpvCreatedByUserId", "kpvCreatedAt");
        newVersion.setKpvId(HutoolSnowflakeIdGenerator.generateLongId());
        newVersion.setKpvCreatedByUserId(createdByUserId);
        newVersion.setKpvCreatedAt(LocalDateTime.now());

        knowledgePointVersionMapper.save(newVersion);

        return Optional.of(newVersion);
    }

    @Override
    public void saveBatch(List<KnowledgePointVersionEntity> entities) {
        entities.forEach(entity -> {
            if (entity.getKpvId() == null) {
                entity.setKpvId(HutoolSnowflakeIdGenerator.generateLongId());
            }
            if (entity.getKpvCreatedAt() == null) {
                entity.setKpvCreatedAt(LocalDateTime.now());
            }
        });
        knowledgePointVersionMapper.saveBatch(entities);
    }

    @Override
    public void deleteByPointId(Long pointId) {
        knowledgePointVersionMapper
                .delete(Where.create(knowledgePointVersionMapper)
                        .eq(KnowledgePointVersionEntity::getKpvKnowledgePointId, pointId));
    }

    @Override
    public List<KnowledgePointVersionEntity> findCurrentVersionsByPointIdsAndUserId(List<Long> knowledgePointIds, Long userId, List<Getter<KnowledgePointVersionEntity>> getters) {

        return RepositoryUtils.getQueryChainWithFields(knowledgePointVersionMapper, getters)
                .connect(query -> query.in(KnowledgePointVersionEntity::getKpvKnowledgePointId, new SubQuery()
                        .select(KnowledgePointEntity::getKpId)
                        .from(KnowledgePointEntity.class)
                        .eq(KnowledgePointEntity::getKpCreatedByUserId, userId)
                        .in(KnowledgePointEntity::getKpId, knowledgePointIds)
                ))
                .list();
    }

}
