package cn.lin037.nexus.application.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.*;
import cn.lin037.nexus.application.knowledge.service.KnowledgeAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.CreateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointRelationReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.UpdateKnowledgePointReq;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointRelationVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgePointVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 知识模块应用服务实现
 *
 * @author LinSanQi
 */
@Service
public class KnowledgeAppServiceImpl implements KnowledgeAppService {

    private final KnowledgeFolderRepository folderRepository;
    private final KnowledgePointRepository pointRepository;
    private final KnowledgePointVersionRepository pointVersionRepository;
    private final KnowledgePointRelationRepository relationRepository;
    private final GraphNodeRepository graphNodeRepository;

    public KnowledgeAppServiceImpl(KnowledgeFolderRepository folderRepository,
                                   KnowledgePointRepository pointRepository,
                                   KnowledgePointVersionRepository pointVersionRepository,
                                   KnowledgePointRelationRepository relationRepository,
                                   GraphNodeRepository graphNodeRepository) {
        this.folderRepository = folderRepository;
        this.pointRepository = pointRepository;
        this.pointVersionRepository = pointVersionRepository;
        this.relationRepository = relationRepository;
        this.graphNodeRepository = graphNodeRepository;
    }

    /**
     * 创建知识点
     *
     * @param req 创建知识点请求参数
     * @return 创建后的知识点视图对象
     * @throws ApplicationException 当目标文件夹不存在或保存知识点失败时抛出异常
     */
    @Override
    @Transactional
    public KnowledgePointVO createKnowledgePoint(CreateKnowledgePointReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查找文件夹并获取学习空间ID
        KnowledgeFolderEntity folderEntity = folderRepository.findByIdAndUserId(req.getFolderId(), userId, List.of(
                KnowledgeFolderEntity::getKfId,
                KnowledgeFolderEntity::getKfLearningSpaceId)
        ).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.TARGET_FOLDER_NOT_FOUND));

        // 构建知识点实体
        KnowledgePointEntity entity = KnowledgePointEntity.builder()
                .kpId(HutoolSnowflakeIdGenerator.generateLongId())
                .kpCreatedByUserId(userId)
                .kpLearningSpaceId(folderEntity.getKfLearningSpaceId())
                .kpFolderId(folderEntity.getKfId())
                .kpCreatedAt(LocalDateTime.now())
                .kpUpdatedAt(LocalDateTime.now())
                .build();

        KnowledgePointVersionEntity versionEntity = KnowledgePointVersionEntity.builder()
                .kpvCreatedByUserId(userId)
                .kpvTitle(req.getTitle())
                .kpvDefinition(req.getDefinition())
                .kpvExplanation(req.getExplanation())
                .kpvFormulaOrCode(req.getFormulaOrCode())
                .kpvExample(req.getExample())
                .kpvDifficulty(req.getDifficulty())
                .kpvKnowledgePointId(entity.getKpId())
                .kpvCreatedAt(LocalDateTime.now())
                .build();
        // 保存知识点版本并获取版本ID
        Long versionId = pointVersionRepository.save(versionEntity)
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_SAVE_FAILED));

        // 设置当前版本ID并保存知识点
        entity.setKpCurrentVersionId(versionId);
        pointRepository.save(entity);

        // 返回知识点视图对象
        return KnowledgePointVO.fromEntity(entity, versionEntity);
    }

    @Override
    @Transactional
    public KnowledgePointVO updateKnowledgePoint(Long pointId, UpdateKnowledgePointReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        KnowledgePointEntity entity = pointRepository.findById(pointId, List.of(
                        KnowledgePointEntity::getKpId,
                        KnowledgePointEntity::getKpFolderId,
                        KnowledgePointEntity::getKpCreatedByUserId
                ))
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        if (!userId.equals(entity.getKpCreatedByUserId())) {
            // 权限校验，即便是无权限，也返回找不到，避免信息泄露
            throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_NOT_FOUND);
        }
        KnowledgePointVersionEntity version = KnowledgePointVersionEntity.builder()
                .kpvKnowledgePointId(pointId)
                .kpvCreatedByUserId(userId)
                .kpvTitle(req.getTitle())
                .kpvDefinition(req.getDefinition())
                .kpvExplanation(req.getExplanation())
                .kpvFormulaOrCode(req.getFormulaOrCode())
                .kpvExample(req.getExample())
                .kpvDifficulty(req.getDifficulty())
                .kpvCreatedAt(LocalDateTime.now())
                .build();
        Long versionId = pointVersionRepository.save(version)
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_SAVE_FAILED));
        pointRepository.updateById(pointId, chain -> {
            chain.set(KnowledgePointEntity::getKpUpdatedAt, LocalDateTime.now());
            chain.set(KnowledgePointEntity::getKpCurrentVersionId, versionId);
        });

        return KnowledgePointVO.fromEntity(entity, version);
    }

    /**
     * 恢复知识点到指定版本
     *
     * @param pointId   知识点ID
     * @param versionId 版本ID
     * @return 恢复后的知识点视图对象
     * @throws ApplicationException 当知识点或版本不存在时抛出异常
     */
    @Override
    @Transactional
    public KnowledgePointVO revertToVersion(Long pointId, Long versionId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验目标知识点是否存在
        if (!pointRepository.existsByIdAndUserId(pointId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "目标知识点不存在");
        }

        // 获取知识点基础信息
        KnowledgePointEntity pointEntity = pointRepository.findByIdAndUserId(pointId, userId, List.of(
                KnowledgePointEntity::getKpId,
                KnowledgePointEntity::getKpCreatedByUserId)
        ).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        // 校验目标版本是否存在
        if (!pointVersionRepository.existsByIdAndPointId(versionId, pointId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "目标版本不存在");
        }

        // 保存新版本并获取实体
        KnowledgePointVersionEntity versionEntity = pointVersionRepository.saveFromVersionId(versionId, userId)
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));

        // 更新知识点的当前版本和更新时间
        pointRepository.updateById(pointId, chain -> chain.set(KnowledgePointEntity::getKpCurrentVersionId, versionEntity.getKpvId())
                .set(KnowledgePointEntity::getKpUpdatedAt, LocalDateTime.now()));

        // 返回恢复后的知识点视图
        return KnowledgePointVO.fromEntity(pointEntity, versionEntity);
    }

    /**
     * 删除知识点
     *
     * @param pointId 知识点ID
     * @throws ApplicationException 当知识点不存在时抛出异常
     */
    @Override
    @Transactional
    public void deleteKnowledgePoint(Long pointId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean isPointExist = pointRepository.existsByIdAndUserId(pointId, userId);
        if (!isPointExist) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }

        if (pointRepository.deleteById(pointId)) {
            // 1. 删除知识点版本
            pointVersionRepository.deleteByPointId(pointId);

            // 2. 处理知识点关系的删除
            // 2.1. 先删除知识点关系（deleteByPointId方法内部会先解除Graph边的投影关联）
            relationRepository.deleteByPointId(pointId);

            // 3. 处理Graph节点的投影解除
            // 3.1. 解除所有投影节点与该知识点的关联，将其变回虚体节点
            graphNodeRepository.disassociateEntity(pointId);
        }
    }

    @Override
    @Transactional
    public KnowledgePointRelationVO createKnowledgePointRelation(CreateKnowledgePointRelationReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!pointRepository.existsByLearningSpaceAndPointIds(req.getTargetPointId(), req.getSourcePointId(), userId, req.getLearningSpaceId())) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }
        KnowledgePointRelationEntity entity = KnowledgePointRelationEntity.builder()
                .kprId(HutoolSnowflakeIdGenerator.generateLongId())
                .kprCreatedByUserId(userId)
                .kprLearningSpaceId(req.getLearningSpaceId())
                .kprSourcePointId(req.getSourcePointId())
                .kprTargetPointId(req.getTargetPointId())
                .kprRelationType(req.getRelationType())
                .kprDescription(req.getDescription())
                .kprCreatedAt(LocalDateTime.now())
                .kprUpdatedAt(LocalDateTime.now())
                .build();

        relationRepository.save(entity);

        return KnowledgePointRelationVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public KnowledgePointRelationVO updateKnowledgePointRelation(Long relationId, UpdateKnowledgePointRelationReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (!relationRepository.existsByIdAndUserId(relationId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.RELATION_NOT_FOUND);
        }

        relationRepository.updateById(relationId, chain -> {
            chain.set(KnowledgePointRelationEntity::getKprRelationType, req.getRelationType());
            chain.set(KnowledgePointRelationEntity::getKprDescription, req.getDescription());
            chain.set(KnowledgePointRelationEntity::getKprUpdatedAt, LocalDateTime.now());
        });

        // 这里的查询是有必要的，因为需要返回完整的视图对象
        KnowledgePointRelationEntity entity = relationRepository.findById(relationId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return KnowledgePointRelationVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteKnowledgePointRelation(Long relationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        KnowledgePointRelationEntity entity = relationRepository.findById(relationId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.RELATION_NOT_FOUND));

        if (!entity.getKprCreatedByUserId().equals(userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FORBIDDEN);
        }

        relationRepository.deleteById(relationId);
    }
} 