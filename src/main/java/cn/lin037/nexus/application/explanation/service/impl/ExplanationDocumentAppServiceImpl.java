package cn.lin037.nexus.application.explanation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.application.explanation.enums.ExplanationErrorCodeEnum;
import cn.lin037.nexus.application.explanation.port.*;
import cn.lin037.nexus.application.explanation.service.ExplanationDocumentAppService;
import cn.lin037.nexus.application.isolation.port.LearningSpaceRepository;
import cn.lin037.nexus.application.knowledge.port.KnowledgePointRelationRepository;
import cn.lin037.nexus.application.knowledge.port.KnowledgePointVersionRepository;
import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.explanation.constant.ExplanationTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChunkContentForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.KnowledgePointForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.KnowledgeRelationForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.params.AiGenerateExplanationTaskParameters;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.ExplanationDocumentStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.api.AsyncTaskManager;
import cn.lin037.nexus.web.rest.v1.explanation.req.AiGenerateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.CreateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.req.UpdateExplanationDocumentReq;
import cn.lin037.nexus.web.rest.v1.explanation.vo.ExplanationDocumentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 讲解文档应用服务实现
 *
 * @author LinSanQi
 */
@Service
public class ExplanationDocumentAppServiceImpl implements ExplanationDocumentAppService {

    private final ExplanationDocumentRepository explanationDocumentRepository;
    private final ExplanationSectionRepository explanationSectionRepository;
    private final ExplanationSubsectionRepository explanationSubsectionRepository;
    private final ExplanationPointRepository explanationPointRepository;
    private final ExplanationRelationRepository explanationRelationRepository;
    private final LearningSpaceRepository learningSpaceRepository;
    private final KnowledgePointVersionRepository knowledgePointVersionRepository;
    private final KnowledgePointRelationRepository knowledgePointRelationRepository;
    private final ResourceChunkRepository resourceChunkRepository;
    private final AsyncTaskManager asyncTaskManager;

    public ExplanationDocumentAppServiceImpl(ExplanationDocumentRepository explanationDocumentRepository,
                                             ExplanationSectionRepository explanationSectionRepository,
                                             ExplanationSubsectionRepository explanationSubsectionRepository,
                                             ExplanationPointRepository explanationPointRepository,
                                             ExplanationRelationRepository explanationRelationRepository,
                                             LearningSpaceRepository learningSpaceRepository,
                                             KnowledgePointVersionRepository knowledgePointVersionRepository,
                                             KnowledgePointRelationRepository knowledgePointRelationRepository,
                                             ResourceChunkRepository resourceChunkRepository,
                                             AsyncTaskManager asyncTaskManager) {
        this.explanationDocumentRepository = explanationDocumentRepository;
        this.explanationSectionRepository = explanationSectionRepository;
        this.explanationSubsectionRepository = explanationSubsectionRepository;
        this.explanationPointRepository = explanationPointRepository;
        this.explanationRelationRepository = explanationRelationRepository;
        this.learningSpaceRepository = learningSpaceRepository;
        this.knowledgePointVersionRepository = knowledgePointVersionRepository;
        this.knowledgePointRelationRepository = knowledgePointRelationRepository;
        this.resourceChunkRepository = resourceChunkRepository;
        this.asyncTaskManager = asyncTaskManager;
    }

    @Override
    @Transactional
    public ExplanationDocumentVO createExplanationDocument(CreateExplanationDocumentReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 验证学习空间是否存在且属于当前用户
        if (!learningSpaceRepository.existsByIdAndUserId(req.getLearningSpaceId(), userId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_CREATION_FAILED, "学习空间不存在或无权限访问");
        }

        // 2. 创建讲解文档实体
        ExplanationDocumentEntity entity = ExplanationDocumentEntity.builder()
                .edId(HutoolSnowflakeIdGenerator.generateLongId())
                .edLearningSpaceId(req.getLearningSpaceId())
                .edCreatedByUserId(userId)
                .edTitle(req.getTitle())
                .edDescription(req.getDescription())
                .edStatus(ExplanationDocumentStatusEnum.DRAFT.getCode())
                .edSectionOrder(new ArrayList<>())
                .edCreatedAt(LocalDateTime.now())
                .edUpdatedAt(LocalDateTime.now())
                .build();

        // 3. 保存讲解文档
        explanationDocumentRepository.save(entity);

        // 4. 返回VO
        return toExplanationDocumentVO(entity);
    }

    @Override
    @Transactional
    public ExplanationDocumentVO updateExplanationDocument(Long documentId, UpdateExplanationDocumentReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 验证讲解文档是否存在且属于当前用户
        ExplanationDocumentEntity entity = explanationDocumentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_NOT_FOUND));

        // 2. 更新讲解文档
        entity.setEdTitle(req.getTitle());
        entity.setEdDescription(req.getDescription());
        entity.setEdUpdatedAt(LocalDateTime.now());

        ExplanationDocumentEntity updatedEntity = explanationDocumentRepository.updateById(entity);

        // 3. 返回VO
        return toExplanationDocumentVO(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteExplanationDocument(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 验证讲解文档是否存在且属于当前用户
        if (!explanationDocumentRepository.hasPermission(documentId, userId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_NOT_FOUND);
        }

        // 2. 级联删除相关数据
        // 删除知识点关系
        explanationRelationRepository.deleteByExplanationDocumentId(documentId);

        // 删除知识点
        explanationPointRepository.deleteByExplanationDocumentId(documentId);

        // 删除小节
        explanationSubsectionRepository.deleteByExplanationDocumentId(documentId);

        // 删除章节
        explanationSectionRepository.deleteByExplanationDocumentId(documentId);

        // 3. 删除讲解文档
        boolean deleted = explanationDocumentRepository.deleteById(documentId);
        if (!deleted) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_DELETE_FAILED);
        }
    }

    @Override
    @Transactional
    public Long aiGenerateExplanationDocument(AiGenerateExplanationDocumentReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 1. 验证学习空间权限
        if (!learningSpaceRepository.existsByIdAndUserId(req.getLearningSpaceId(), userId)) {
            throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_CREATION_FAILED, "学习空间不存在或无权限访问");
        }

        // 2. 验证并查询资源分片数据
        List<ChunkContentForExplanation> chunks = Collections.emptyList();
        if (req.getChunkIdList() != null && !req.getChunkIdList().isEmpty()) {
            List<ResourceChunkEntity> chunkEntities = resourceChunkRepository.findByIds(req.getChunkIdList());
            if (chunkEntities.size() != req.getChunkIdList().size()) {
                List<Long> existIds = chunkEntities.stream().map(ResourceChunkEntity::getRcId).toList();
                List<Long> notExistIds = req.getChunkIdList().stream().filter(id -> !existIds.contains(id)).toList();
                throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_CREATION_FAILED, "部分资源分片不存在: " + notExistIds);
            }
            chunks = chunkEntities.stream().map(ChunkContentForExplanation::fromResourceChunkEntity).toList();
        }

        // 3. 验证并查询知识点数据
        List<KnowledgePointForExplanation> knowledgePoints = Collections.emptyList();
        if (req.getKnowledgePointIdList() != null && !req.getKnowledgePointIdList().isEmpty()) {
            List<KnowledgePointVersionEntity> versions = knowledgePointVersionRepository.findCurrentVersionsByPointIdsAndUserId(
                    req.getKnowledgePointIdList(), userId, List.of(
                            KnowledgePointVersionEntity::getKpvId,
                            KnowledgePointVersionEntity::getKpvTitle,
                            KnowledgePointVersionEntity::getKpvDefinition,
                            KnowledgePointVersionEntity::getKpvExplanation,
                            KnowledgePointVersionEntity::getKpvFormulaOrCode,
                            KnowledgePointVersionEntity::getKpvExample,
                            KnowledgePointVersionEntity::getKpvDifficulty
                    ));
            if (versions.size() != req.getKnowledgePointIdList().size()) {
                List<Long> existIds = versions.stream().map(KnowledgePointVersionEntity::getKpvKnowledgePointId).toList();
                List<Long> notExistIds = req.getKnowledgePointIdList().stream().filter(id -> !existIds.contains(id)).toList();
                throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_CREATION_FAILED, "部分知识点不存在: " + notExistIds);
            }
            knowledgePoints = versions.stream().map(KnowledgePointForExplanation::fromKnowledgePointVersionEntity).toList();
        }

        // 4. 验证并查询知识点关系数据
        List<KnowledgeRelationForExplanation> knowledgeRelations = Collections.emptyList();
        if (req.getKnowledgeRelationIdList() != null && !req.getKnowledgeRelationIdList().isEmpty()) {
            List<KnowledgePointRelationEntity> relationEntities = knowledgePointRelationRepository.findByIds(
                    req.getKnowledgeRelationIdList(),
                    List.of(
                            KnowledgePointRelationEntity::getKprId,
                            KnowledgePointRelationEntity::getKprLearningSpaceId,
                            KnowledgePointRelationEntity::getKprCreatedByUserId,
                            KnowledgePointRelationEntity::getKprSourcePointId,
                            KnowledgePointRelationEntity::getKprTargetPointId,
                            KnowledgePointRelationEntity::getKprRelationType,
                            KnowledgePointRelationEntity::getKprDescription
                    )
            );
            if (relationEntities.size() != req.getKnowledgeRelationIdList().size()) {
                List<Long> existIds = relationEntities.stream().map(KnowledgePointRelationEntity::getKprId).toList();
                List<Long> notExistIds = req.getKnowledgeRelationIdList().stream().filter(id -> !existIds.contains(id)).toList();
                throw new ApplicationException(ExplanationErrorCodeEnum.EXPLANATION_DOCUMENT_CREATION_FAILED, "部分知识点关系不存在: " + notExistIds);
            }
            knowledgeRelations = relationEntities.stream().map(KnowledgeRelationForExplanation::fromKnowledgePointRelationEntity).toList();
        }

        // 5. 创建空壳讲解文档
        ExplanationDocumentEntity documentEntity = ExplanationDocumentEntity.builder()
                .edId(HutoolSnowflakeIdGenerator.generateLongId())
                .edLearningSpaceId(req.getLearningSpaceId())
                .edCreatedByUserId(userId)
                .edTitle(req.getTitle())
                .edDescription(req.getDescription())
                .edStatus(ExplanationDocumentStatusEnum.AI_GENERATING.getCode())
                .edSectionOrder(new ArrayList<>())
                .edCreatedAt(LocalDateTime.now())
                .edUpdatedAt(LocalDateTime.now())
                .build();
        explanationDocumentRepository.save(documentEntity);

        // 6. 构建任务参数
        AiGenerateExplanationTaskParameters taskParams = AiGenerateExplanationTaskParameters.builder()
                .userId(userId)
                .learningSpaceId(req.getLearningSpaceId())
                .explanationDocumentId(documentEntity.getEdId())
                .userPrompt(req.getUserPrompt())
                .chunks(chunks)
                .knowledgePoints(knowledgePoints)
                .knowledgeRelations(knowledgeRelations)
                .build();

        // 7. 发布异步任务
        return asyncTaskManager.submit(ExplanationTaskConstant.TASK_TYPE_EXPLANATION_AI_GENERATE, BeanUtil.beanToMap(taskParams), String.valueOf(userId));
    }

    /**
     * 转换为VO对象
     */
    private ExplanationDocumentVO toExplanationDocumentVO(ExplanationDocumentEntity entity) {
        ExplanationDocumentVO vo = new ExplanationDocumentVO();
        vo.setId(entity.getEdId());
        vo.setLearningSpaceId(entity.getEdLearningSpaceId());
        vo.setCreatedByUserId(entity.getEdCreatedByUserId());
        vo.setTitle(entity.getEdTitle());
        vo.setDescription(entity.getEdDescription());
        vo.setStatus(entity.getEdStatus());

        // 转换章节顺序
        if (entity.getEdSectionOrder() != null) {
            List<Long> sectionOrder = entity.getEdSectionOrder().stream()
                    .map(Long::valueOf)
                    .toList();
            vo.setSectionOrder(sectionOrder);
        } else {
            vo.setSectionOrder(new ArrayList<>());
        }

        vo.setGraphConfig(entity.getEdGraphConfig());
        vo.setCreatedAt(entity.getEdCreatedAt());
        vo.setUpdatedAt(entity.getEdUpdatedAt());

        return vo;
    }
}
