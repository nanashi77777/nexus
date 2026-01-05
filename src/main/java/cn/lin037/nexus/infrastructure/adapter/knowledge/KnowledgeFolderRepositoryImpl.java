package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.KnowledgeFolderRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.utils.RepositoryUtils;
import cn.lin037.nexus.infrastructure.common.persistent.entity.condition.KnowledgeFolderCondition;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import cn.lin037.nexus.infrastructure.common.persistent.mapper.knowledge.KnowledgeFolderMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import com.sun.istack.NotNull;
import db.sql.api.Getter;
import db.sql.api.cmd.UpdateStrategy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 知识点文件夹仓储实现
 *
 * @author LinSanQi
 */
@Repository
public class KnowledgeFolderRepositoryImpl implements KnowledgeFolderRepository {

    private static final Integer MAX_FOLDER_LEVEL = 5;
    private static final Integer MAX_FOLDER_COUNT = 100;
    private final KnowledgeFolderMapper knowledgeFolderMapper;

    public KnowledgeFolderRepositoryImpl(KnowledgeFolderMapper knowledgeFolderMapper) {
        this.knowledgeFolderMapper = knowledgeFolderMapper;
    }

    @Override
    public boolean existsByIdAndUserId(Long id, Long userId) {
        return QueryChain.of(knowledgeFolderMapper)
                .eq(KnowledgeFolderEntity::getKfId, id)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                .exists();
    }

    @Override
    public boolean existsByParentIdAndNameAndUserId(@NotNull Long parentId, String name, Long userId) {
        return QueryChain.of(knowledgeFolderMapper)
                .eq(KnowledgeFolderEntity::getKfParentId, parentId)
                .eq(KnowledgeFolderEntity::getKfName, name)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                .exists();
    }

    @Override
    public Optional<KnowledgeFolderEntity> findByIdAndUserId(Long id, Long userId, List<Getter<KnowledgeFolderEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(knowledgeFolderMapper, getters)
                        .eq(KnowledgeFolderEntity::getKfId, id)
                        .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                        .limit(1)
                        .get()
        );
    }

    @Override
    public Optional<KnowledgeFolderEntity> findById(Long id, List<Getter<KnowledgeFolderEntity>> getters) {
        return Optional.ofNullable(
                RepositoryUtils.getQueryChainWithFields(knowledgeFolderMapper, getters)
                        .eq(KnowledgeFolderEntity::getKfId, id)
                        .limit(1)
                        .get()
        );
    }

    @Override
    public List<KnowledgeFolderEntity> findByParentId(Long parentId, Long userId, List<Getter<KnowledgeFolderEntity>> getters) {
        QueryChain<KnowledgeFolderEntity> queryChain = RepositoryUtils.getQueryChainWithFields(knowledgeFolderMapper, getters)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId);
        if (parentId == null) {
            queryChain.isNull(KnowledgeFolderEntity::getKfParentId);
        } else {
            queryChain.eq(KnowledgeFolderEntity::getKfParentId, parentId);
        }
        return queryChain.list();
    }

    @Override
    public List<KnowledgeFolderEntity> findByLearningSpaceIdAndParentId(Long learningSpaceId, Long parentId, Long userId, List<Getter<KnowledgeFolderEntity>> getters) {
        QueryChain<KnowledgeFolderEntity> queryChain = RepositoryUtils.getQueryChainWithFields(knowledgeFolderMapper, getters)
                .eq(KnowledgeFolderEntity::getKfLearningSpaceId, learningSpaceId)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId);
        if (parentId == null) {
            queryChain.isNull(KnowledgeFolderEntity::getKfParentId);
        } else {
            queryChain.eq(KnowledgeFolderEntity::getKfParentId, parentId);
        }
        return queryChain.list();
    }

    @Override
    public int countByParentIdAndUserId(Long parentId, Long userId) {
        QueryChain<KnowledgeFolderEntity> queryChain = QueryChain.of(knowledgeFolderMapper)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId);
        if (parentId == null) {
            queryChain.isNull(KnowledgeFolderEntity::getKfParentId);
        } else {
            queryChain.eq(KnowledgeFolderEntity::getKfParentId, parentId);
        }
        return queryChain.count();
    }

    @Override
    public void save(KnowledgeFolderEntity entity) {
        Long parentId = entity.getKfParentId();
        Long userId = entity.getKfCreatedByUserId();

        // 1. 校验父文件夹是否存在，并校验层级
        if (parentId != null && parentId >= 0L) {
            // 2. 获取父文件夹的层级
            Integer kfLevel = findByIdAndUserId(parentId, userId, List.of(
                    KnowledgeFolderEntity::getKfLevel
            )).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.PARENT_FOLDER_NOT_FOUND))
                    .getKfLevel();
            if (kfLevel >= MAX_FOLDER_LEVEL) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_LEVEL_EXCEEDS_LIMIT);
            }
            // 3. 校验同级目录下是否存在同名文件夹
            if (existsByParentIdAndNameAndUserId(parentId, entity.getKfName(), userId)) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
            }
            // 4. 校验同级目录下子文件夹数量
            if (countByParentIdAndUserId(parentId, userId) >= MAX_FOLDER_COUNT) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_COUNT_EXCEEDS_LIMIT);
            }
            entity.setKfLevel(kfLevel + 1);
        } else {
            // 2. 根目录
            entity.setKfParentId(null);
            entity.setKfLevel(0);
        }

        knowledgeFolderMapper.save(entity);
    }

    @Override
    @Deprecated
    public void move(@NotNull Long folderId, Long newParentId, Long userId) {
        // 1. 校验要移动的文件夹是否存在
        KnowledgeFolderEntity knowledgeFolderEntity = QueryChain.of(knowledgeFolderMapper)
                .select(KnowledgeFolderEntity::getKfName, KnowledgeFolderEntity::getKfLevel, KnowledgeFolderEntity::getKfLearningSpaceId)
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                .limit(1)
                .get();
        if (knowledgeFolderEntity == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.TARGET_FOLDER_NOT_FOUND);
        }

        if (newParentId == null) {
            // 2. 校验目标父文件夹是否存在
            List<KnowledgeFolderEntity> subfolderList = QueryChain.of(knowledgeFolderMapper)
                    .eq(KnowledgeFolderEntity::getKfLearningSpaceId, knowledgeFolderEntity.getKfLearningSpaceId())
                    .isNull(KnowledgeFolderEntity::getKfParentId)
                    .list();
            // 3. 父文件夹的子文件夹数不能超过`MAX_FOLDER_COUNT`个
            if (subfolderList.size() >= MAX_FOLDER_COUNT) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_COUNT_EXCEEDS_LIMIT);
            }
            // 4. 父文件夹下不能有同名的子文件夹
            subfolderList.stream().filter(sub -> knowledgeFolderEntity.getKfName().equals(sub.getKfName()))
                    .findFirst()
                    .ifPresent(sub -> {
                        throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
                    });
            // 5. 更新文件夹父ID
            UpdateChain.of(knowledgeFolderMapper)
                    .set(KnowledgeFolderEntity::getKfParentId, UpdateStrategy.NULL_TO_NULL)
                    .set(KnowledgeFolderEntity::getKfLevel, 0)
                    .set(KnowledgeFolderEntity::getKfUpdatedAt, LocalDateTime.now())
                    .eq(KnowledgeFolderEntity::getKfId, folderId)
                    .execute();
        } else {
            // 2. 校验目标父文件夹是否存在，并校验层级
            KnowledgeFolderEntity parentFolderEntity = QueryChain.of(knowledgeFolderMapper)
                    .select(KnowledgeFolderEntity::getKfName, KnowledgeFolderEntity::getKfLevel)
                    .eq(KnowledgeFolderEntity::getKfId, folderId)
                    .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                    .limit(1)
                    .get();
            if (parentFolderEntity == null) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.PARENT_FOLDER_NOT_FOUND);
            }
            if (parentFolderEntity.getKfLevel() == null || parentFolderEntity.getKfLevel() >= MAX_FOLDER_LEVEL - 1) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_LEVEL_EXCEEDS_LIMIT);
            }

            // 3. 校验同级目录下子文件夹数量
            if (countByParentIdAndUserId(newParentId, userId) >= MAX_FOLDER_COUNT) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_COUNT_EXCEEDS_LIMIT);
            }

            // 4. 校验目标文件夹下是否存在同名文件夹
            boolean exists = QueryChain.of(knowledgeFolderMapper)
                    .eq(KnowledgeFolderEntity::getKfParentId, newParentId)
                    .eq(KnowledgeFolderEntity::getKfName, knowledgeFolderEntity.getKfName())
                    .exists();
            if (exists) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
            }

            // 5. 更新父文件夹ID
            UpdateChain.of(knowledgeFolderMapper)
                    .set(KnowledgeFolderEntity::getKfParentId, newParentId)
                    .set(KnowledgeFolderEntity::getKfLevel, parentFolderEntity.getKfLevel() + 1)
                    .set(KnowledgeFolderEntity::getKfUpdatedAt, LocalDateTime.now())
                    .eq(KnowledgeFolderEntity::getKfId, folderId)
                    .execute();
        }
    }

    /**
     * 移动文件夹
     *
     * @param folderId    文件夹ID
     * @param newParentId 新的父文件夹ID
     * @param userId      用户ID
     */
    public void moveFolderOptimized(Long folderId, Long newParentId, Long userId) {
        // 1. 校验要移动的文件夹是否存在
        KnowledgeFolderCondition folderCondition = new KnowledgeFolderCondition();
        folderCondition.setFolderId(folderId);
        folderCondition.setUserId(userId);

        KnowledgeFolderEntity knowledgeFolderEntity = QueryChain.of(knowledgeFolderMapper)
                .where(folderCondition)
                .select(KnowledgeFolderEntity::getKfName,
                        KnowledgeFolderEntity::getKfLevel,
                        KnowledgeFolderEntity::getKfLearningSpaceId)
                .get();

        if (knowledgeFolderEntity == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.TARGET_FOLDER_NOT_FOUND);
        }

        if (newParentId == null) {
            // 移动到根目录的情况
            handleMoveToRoot(knowledgeFolderEntity, folderId);
        } else {
            // 移动到指定父文件夹的情况
            handleMoveToParent(knowledgeFolderEntity, folderId, newParentId, userId);
        }
    }

    private void handleMoveToRoot(KnowledgeFolderEntity knowledgeFolderEntity, Long folderId) {
        // 2. 使用单个查询检查根目录下的子文件夹
        KnowledgeFolderCondition rootCondition = new KnowledgeFolderCondition();
        rootCondition.setLearningSpaceId(knowledgeFolderEntity.getKfLearningSpaceId());
        rootCondition.setParentId(null);

        List<KnowledgeFolderEntity> subfolderList = QueryChain.of(knowledgeFolderMapper)
                .where(rootCondition)
                .select(KnowledgeFolderEntity::getKfName)
                .list();

        // 3. 校验文件夹数量限制
        if (subfolderList.size() >= MAX_FOLDER_COUNT) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_COUNT_EXCEEDS_LIMIT);
        }

        // 4. 校验同名文件夹 - 使用exists()优化
        boolean nameExists = subfolderList.stream()
                .anyMatch(sub -> knowledgeFolderEntity.getKfName().equals(sub.getKfName()));
        if (nameExists) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
        }

        // 5. 更新文件夹
        UpdateChain.of(knowledgeFolderMapper)
                .set(KnowledgeFolderEntity::getKfParentId, null, UpdateStrategy.NULL_TO_NULL)
                .set(KnowledgeFolderEntity::getKfLevel, 0)
                .set(KnowledgeFolderEntity::getKfUpdatedAt, LocalDateTime.now())
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .execute();
    }

    private void handleMoveToParent(KnowledgeFolderEntity knowledgeFolderEntity,
                                    Long folderId, Long newParentId, Long userId) {
        // 2. 校验目标父文件夹是否存在并获取层级信息
        KnowledgeFolderCondition parentRequest = new KnowledgeFolderCondition();
        parentRequest.setFolderId(newParentId);
        parentRequest.setUserId(userId);

        KnowledgeFolderEntity parentFolderEntity = QueryChain.of(knowledgeFolderMapper)
                .where(parentRequest)
                .select(KnowledgeFolderEntity::getKfLevel)
                .get();

        if (parentFolderEntity == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.PARENT_FOLDER_NOT_FOUND);
        }

        if (parentFolderEntity.getKfLevel() == null ||
                parentFolderEntity.getKfLevel() >= MAX_FOLDER_LEVEL - 1) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_LEVEL_EXCEEDS_LIMIT);
        }

        // 3. 使用exists()优化同名检查和数量检查
        KnowledgeFolderCondition siblingRequest = new KnowledgeFolderCondition();
        siblingRequest.setParentId(newParentId);

        // 检查同级文件夹数量
        int siblingCount = QueryChain.of(knowledgeFolderMapper)
                .where(siblingRequest)
                .count();

        if (siblingCount >= MAX_FOLDER_COUNT) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_COUNT_EXCEEDS_LIMIT);
        }

        // 4. 检查同名文件夹
        siblingRequest.setFolderName(knowledgeFolderEntity.getKfName());
        boolean exists = QueryChain.of(knowledgeFolderMapper)
                .where(siblingRequest)
                .exists();

        if (exists) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
        }

        // 5. 更新父文件夹ID
        UpdateChain.of(knowledgeFolderMapper)
                .set(KnowledgeFolderEntity::getKfParentId, newParentId)
                .set(KnowledgeFolderEntity::getKfLevel, parentFolderEntity.getKfLevel() + 1)
                .set(KnowledgeFolderEntity::getKfUpdatedAt, LocalDateTime.now())
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .execute();
    }

    @Override
    @Transactional
    public void rename(Long folderId, Long userId, String newName) {
        // 校验文件夹是否存在
        KnowledgeFolderEntity knowledgeFolderEntity = QueryChain.of(knowledgeFolderMapper)
                .select(KnowledgeFolderEntity::getKfParentId, KnowledgeFolderEntity::getKfName,
                        KnowledgeFolderEntity::getKfLearningSpaceId)
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                .limit(1).get();
        if (knowledgeFolderEntity == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_NOT_FOUND);
        }

        // 校验同级目录下是否存在同名文件夹
        // 如果是根目录
        boolean exists;
        if (knowledgeFolderEntity.getKfParentId() == null) {
            exists = QueryChain.of(knowledgeFolderMapper)
                    .isNull(KnowledgeFolderEntity::getKfParentId)
                    .eq(KnowledgeFolderEntity::getKfName, newName)
                    .eq(KnowledgeFolderEntity::getKfLearningSpaceId, knowledgeFolderEntity.getKfLearningSpaceId())
                    .exists();
        } else {
            exists = QueryChain.of(knowledgeFolderMapper)
                    .eq(KnowledgeFolderEntity::getKfParentId, knowledgeFolderEntity.getKfParentId())
                    .eq(KnowledgeFolderEntity::getKfName, newName)
                    .exists();
        }
        if (exists) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_ALREADY_EXISTS);
        }

        // 更新文件夹名称
        UpdateChain.of(knowledgeFolderMapper)
                .set(KnowledgeFolderEntity::getKfName, newName)
                .set(KnowledgeFolderEntity::getKfUpdatedAt, LocalDateTime.now())
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .execute();
    }

    @Override
    public boolean deleteById(Long id) {
        // TODO: 级联删除文件夹下的所有文件和子文件夹，以及文件夹下的所有文件
        return UpdateChain.of(knowledgeFolderMapper)
                .set(KnowledgeFolderEntity::getKfDeletedAt, LocalDateTime.now())
                .eq(KnowledgeFolderEntity::getKfId, id)
                .execute() > 0;
    }

    @Override
    public Optional<Long> findLearningSpaceIdByIdAndUserId(Long folderId, Long userId) {
        KnowledgeFolderEntity knowledgeFolderEntity = QueryChain.of(knowledgeFolderMapper)
                .eq(KnowledgeFolderEntity::getKfId, folderId)
                .eq(KnowledgeFolderEntity::getKfCreatedByUserId, userId)
                .select(KnowledgeFolderEntity::getKfLearningSpaceId)
                .limit(1)
                .get();
        if (knowledgeFolderEntity != null && knowledgeFolderEntity.getKfLearningSpaceId() != null) {
            return Optional.of(knowledgeFolderEntity.getKfLearningSpaceId());
        }
        return Optional.empty();
    }
}
