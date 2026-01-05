package cn.lin037.nexus.application.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.*;
import cn.lin037.nexus.application.knowledge.service.KnowledgeAiAppService;
import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.knowledge.KnowledgeTaskAdapter;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiChunkContent;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgePoint;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgeRelation;
import cn.lin037.nexus.infrastructure.adapter.knowledge.enums.AiGenerationTypeEnum;
import cn.lin037.nexus.infrastructure.adapter.knowledge.params.AiGenerateKnowledgeTaskParameters;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiConnectKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiExpandKnowledgeReq;
import cn.lin037.nexus.web.rest.v1.knowledge.req.AiGenerateKnowledgeFromResourceReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LinSanQi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeAiAppServiceImpl implements KnowledgeAiAppService {

    private final KnowledgeTaskAdapter knowledgeTaskAdapter;
    private final ResourceChunkRepository resourceChunkRepository;
    private final KnowledgeFolderRepository knowledgeFolderRepository;
    private final KnowledgeGraphRepository knowledgeGraphRepository;
    private final KnowledgePointVersionRepository knowledgePointVersionRepository;
    private final KnowledgePointRelationRepository knowledgePointRelationRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;


    @Override
    public Long generateFromResources(AiGenerateKnowledgeFromResourceReq req) {
        return submitAiGenerationTask(
                // TODO：需要边Type名
                AiGenerationTypeEnum.GENERATE_KNOWLEDGE,
                req.getTargetDepositId(),
                req.getIsVirtual(),
                req.getPrompt(),
                req.getChunkIds(),
                Collections.emptyList()
        );
    }

    @Override
    public Long expandKnowledge(AiExpandKnowledgeReq req) {
        return submitAiGenerationTask(
                AiGenerationTypeEnum.EXPAND_KNOWLEDGE,
                req.getTargetDepositId(),
                req.getIsVirtual(),
                req.getPrompt(),
                req.getChunkIds(),
                req.getKnowledgePointIds()
        );
    }

    @Override
    public Long connectKnowledge(AiConnectKnowledgeReq req) {
        if (CollectionUtils.isEmpty(req.getKnowledgePointIds())) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "知识点id不能为空");
        }
        return submitAiGenerationTask(
                AiGenerationTypeEnum.CONNECT_KNOWLEDGE,
                req.getTargetDepositId(),
                req.getIsVirtual(),
                req.getPrompt(),
                Collections.emptyList(),
                req.getKnowledgePointIds()
        );
    }

    private Long submitAiGenerationTask(AiGenerationTypeEnum generationType,
                                        Long targetDepositId,
                                        boolean isVirtual,
                                        String prompt,
                                        List<Long> chunkIds,
                                        List<Long> knowledgePointIds) {
        // 1. 获取用户ID并验证学习空间
        UserIdAndLearningSpaceId result = validateAndGetLearningSpaceId(targetDepositId, isVirtual);
        Long userId = result.userId();
        Long learningSpaceId = result.learningSpaceId();

        // 2. 获取资源分片内容
        List<AiChunkContent> chunks = getChunks(chunkIds, learningSpaceId);

        // 3. 获取知识点和关系数据
        KnowledgeData knowledgeData = getKnowledgeData(knowledgePointIds, userId, targetDepositId, isVirtual);

        // 4. 构建任务参数
        AiGenerateKnowledgeTaskParameters parameters = AiGenerateKnowledgeTaskParameters.builder()
                .generationType(generationType)
                .userId(userId)
                .learningSpaceId(learningSpaceId)
                .prompt(prompt)
                .isVirtual(isVirtual)
                .targetDepositId(targetDepositId)
                .chunks(chunks)
                .knowledgePoints(knowledgeData.points())
                .existingRelations(knowledgeData.relations())
                .build();

        // 5. 提交任务
        return knowledgeTaskAdapter.submitAiGenerateTask(parameters, String.valueOf(userId));
    }

    private List<AiChunkContent> getChunks(List<Long> chunkIds, Long learningSpaceId) {
        if (CollectionUtils.isEmpty(chunkIds)) {
            return null;
        }
        List<ResourceChunkEntity> chunks = resourceChunkRepository.findByIdsAndLearningSpaceId(chunkIds, learningSpaceId);
        if (chunks.size() != chunkIds.size()) {
            List<Long> foundIds = chunks.stream().map(ResourceChunkEntity::getRcId).toList();
            List<Long> notFoundIds = chunkIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new ApplicationException(KnowledgeErrorCodeEnum.RESOURCE_NOT_FOUND, "部分分片资源不存在", notFoundIds);
        }
        return chunks.stream().map(chunk -> new AiChunkContent(chunk.getRcId(), chunk.getRcContent())).toList();
    }

    private KnowledgeData getKnowledgeData(List<Long> pointIds, Long userId, Long targetDepositId, boolean isVirtual) {
        if (CollectionUtils.isEmpty(pointIds)) {
            return new KnowledgeData(null, null);
        }

        List<AiKnowledgePoint> points;
        List<AiKnowledgeRelation> relations;

        if (isVirtual) {
            List<GraphNodeEntity> nodes = graphNodeRepository.findByIdsAndUserId(pointIds, userId, Collections.emptyList())
                    .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "知识图谱节点不存在"));
            if (nodes.size() != pointIds.size()) {
                List<Long> foundIds = nodes.stream().map(GraphNodeEntity::getGnId).toList();
                List<Long> notFoundIds = pointIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
                throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "部分知识图谱节点不存在", notFoundIds);
            }
            points = nodes.stream().map(AiKnowledgePoint::fromGraphNodeEntity).toList();
            relations = graphEdgeRepository.findBetweenNodeIds(pointIds, targetDepositId, Collections.emptyList())
                    .stream().map(AiKnowledgeRelation::fromGraphEdgeEntity).toList();
        } else {
            List<KnowledgePointVersionEntity> versions = knowledgePointVersionRepository.findCurrentVersionsByPointIdsAndUserId(pointIds, userId, Collections.emptyList());
            if (versions.size() != pointIds.size()) {
                List<Long> existIds = versions.stream().map(KnowledgePointVersionEntity::getKpvKnowledgePointId).toList();
                List<Long> notExistIds = pointIds.stream().filter(id -> !existIds.contains(id)).collect(Collectors.toList());
                throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "部分知识点不存在", notExistIds);
            }
            points = versions.stream().map(AiKnowledgePoint::fromKnowledgePointVersionEntity).toList();
            relations = knowledgePointRelationRepository.findBetweenPointIds(pointIds, userId, Collections.emptyList())
                    .stream().map(AiKnowledgeRelation::fromKnowledgePointRelationEntity).toList();
        }
        return new KnowledgeData(points, relations);
    }


    /**
     * 验证并获取学习空间ID
     *
     * @param targetDepositId 目标ID
     * @param isVirtual       是否是虚拟空间
     * @return 包含用户ID和学习空间ID的对象
     */
    private UserIdAndLearningSpaceId validateAndGetLearningSpaceId(Long targetDepositId, boolean isVirtual) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (isVirtual) {
            return knowledgeGraphRepository.findLearningSpaceIdByIdAndUserId(targetDepositId, userId)
                    .map(learningSpaceId -> new UserIdAndLearningSpaceId(userId, learningSpaceId))
                    .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NOT_FOUND));
        } else {
            return knowledgeFolderRepository.findLearningSpaceIdByIdAndUserId(targetDepositId, userId)
                    .map(learningSpaceId -> new UserIdAndLearningSpaceId(userId, learningSpaceId))
                    .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.TARGET_FOLDER_NOT_FOUND));
        }
    }

    /**
     * 内部类用于返回用户ID和学习空间ID
     */
    private record UserIdAndLearningSpaceId(Long userId, Long learningSpaceId) {
    }

    /**
     * 内部记录，用于封装知识点和关系
     */
    private record KnowledgeData(List<AiKnowledgePoint> points,
                                 List<AiKnowledgeRelation> relations) {
    }
}
