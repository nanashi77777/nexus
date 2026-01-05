package cn.lin037.nexus.infrastructure.adapter.knowledge.impl;

import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.*;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.knowledge.KnowledgeAiGenerationService;
import cn.lin037.nexus.infrastructure.adapter.knowledge.constant.KnowledgeTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiGenerationResult;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgePoint;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgeRelation;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.KnowledgeGraphDto;
import cn.lin037.nexus.infrastructure.adapter.knowledge.params.AiGenerateKnowledgeTaskParameters;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import cn.lin037.nexus.infrastructure.common.task.repository.AsyncTaskRepository;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识生成任务执行器
 * 负责处理AI生成知识点和关系的任务执行、事务管理和持久化逻辑
 *
 * @author LinSanQi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeAiGenerateTaskExecutor implements TaskExecutor<AiGenerateKnowledgeTaskParameters, TokenUsageAccumulator> {

    private final KnowledgeAiGenerationService knowledgeGenerationService;
    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgePointVersionRepository knowledgePointVersionRepository;
    private final KnowledgePointRelationRepository knowledgePointRelationRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final AsyncTaskRepository asyncTaskRepository;

    @Override
    public String getTaskType() {
        return KnowledgeTaskConstant.TASK_TYPE_KNOWLEDGE_AI_GENERATE;
    }

    @Override
    public Class<AiGenerateKnowledgeTaskParameters> getParametersType() {
        return AiGenerateKnowledgeTaskParameters.class;
    }

    @Override
    public Class<TokenUsageAccumulator> getResultType() {
        return TokenUsageAccumulator.class;
    }

    @Override
    @Transactional
    public TaskResult<TokenUsageAccumulator> execute(AiGenerateKnowledgeTaskParameters params, TaskContext context) {
        log.info("开始执行知识生成任务，类型: {}, 用户ID: {}, 学习空间ID: {}",
                params.getGenerationType(), params.getUserId(), params.getLearningSpaceId());

        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();
        try {
            TokenUsage tokenUsage = switch (params.getGenerationType()) {
                case GENERATE_KNOWLEDGE -> executeGraphGeneration(params);
                case EXPAND_KNOWLEDGE -> executeExpansion(params);
                case CONNECT_KNOWLEDGE -> executeConnection(params);
            };

            tokenAccumulator.add(tokenUsage);
            log.info("知识生成任务完成，类型: {}, Token使用量: {}", params.getGenerationType(), tokenUsage);
            return TaskResult.success("知识生成任务完成", tokenAccumulator);
        } catch (Exception e) {
            log.error("知识生成任务执行失败，类型: {}", params.getGenerationType(), e);
            // 在异常中携带已使用的TokenUsageAccumulator
            if (e instanceof ApplicationException) {
                throw e; // 如果已经是ApplicationException，直接抛出
            } else {
                // 将异常包装为ApplicationException并携带TokenUsageAccumulator
                throw new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED,
                        "知识生成任务执行失败: " + e.getMessage(),
                        tokenAccumulator, TokenUsageAccumulator.class);
            }
        }
    }

    /**
     * 执行完整图谱生成工作流
     */
    private TokenUsage executeGraphGeneration(AiGenerateKnowledgeTaskParameters params) {
        log.info("执行完整图谱生成工作流");

        // 调用知识生成服务
        AiGenerationResult<KnowledgeGraphDto> generationResult = knowledgeGenerationService.generateKnowledgeGraph(
                params.getPrompt(), params.getChunks()).orElseThrow(
                () -> new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "知识生成服务返回空结果")
        );

        KnowledgeGraphDto knowledgeGraph = generationResult.getResult();
        if (knowledgeGraph == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "生成的知识图谱为空");
        }

        // 批量持久化知识图谱
        persistKnowledgeGraphBatch(params, knowledgeGraph);

        log.info("完整图谱生成完成，生成了 {} 个知识点和 {} 条关系",
                knowledgeGraph.getPoints().size(),
                knowledgeGraph.getRelations().size());

        return generationResult.getTokenUsage();
    }

    /**
     * 执行知识点扩展工作流
     */
    private TokenUsage executeExpansion(AiGenerateKnowledgeTaskParameters params) {
        log.info("执行知识点扩展工作流");

        if (params.getKnowledgePoints() == null || params.getKnowledgePoints().isEmpty()) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "扩展工作流需要提供原始知识点列表");
        }

        // 调用知识扩展服务，返回完整的知识图谱（新生成的知识点和关系）
        AiGenerationResult<KnowledgeGraphDto> expansionResult = knowledgeGenerationService.expandKnowledgePoints(
                params.getPrompt(), params.getKnowledgePoints(), params.getChunks()).orElseThrow(
                () -> new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "知识点扩展服务返回空结果")
        );

        KnowledgeGraphDto knowledgeGraph = expansionResult.getResult();
        if (knowledgeGraph == null) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "生成的知识图谱为空");
        }

        // 持久化新生成的知识图谱
        persistKnowledgeGraphBatch(params, knowledgeGraph);

        log.info("知识点扩展完成，扩展了 {} 个新知识点和 {} 条关系",
                knowledgeGraph.getPoints().size(), knowledgeGraph.getRelations().size());
        return expansionResult.getTokenUsage();
    }

    /**
     * 执行知识点连接工作流
     */
    private TokenUsage executeConnection(AiGenerateKnowledgeTaskParameters params) {
        log.info("执行知识点连接工作流");

        if (params.getKnowledgePoints() == null || params.getKnowledgePoints().isEmpty()) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.AI_GENERATION_FAILED, "连接工作流需要提供现有知识点列表");
        }

        if (params.getKnowledgePoints().size() < 2) {
            log.info("知识点数量少于2个，跳过关系生成");
            return new TokenUsage(0, 0, 0);
        }

        // 直接使用KnowledgeAiGenerationService中的generateRelationsUntilComplete方法
        // 需要先将知识点转换为AiKnowledgePoint并调用关系生成
        log.warn("知识点连接功能需要进一步实现 - 调用KnowledgeAiGenerationService的generateRelationsUntilComplete方法");

        return new TokenUsage(0, 0, 0);
    }

    /**
     * 在新事务中更新任务状态
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTaskStatusInNewTransaction(Long taskId, String status, String message) {
        log.info("更新任务状态，任务ID: {}, 状态: {}, 消息: {}", taskId, status, message);
        // 根据状态字符串转换为TaskStatusEnum并更新
        TaskStatusEnum newStatus = "COMPLETED".equals(status) ? TaskStatusEnum.COMPLETED : TaskStatusEnum.FAILED;
        TaskStatusEnum expectedStatus = TaskStatusEnum.RUNNING;
        asyncTaskRepository.compareAndSetStatus(taskId, newStatus, expectedStatus);
    }

    /**
     * 批量持久化知识图谱数据
     */
    private void persistKnowledgeGraphBatch(AiGenerateKnowledgeTaskParameters params, KnowledgeGraphDto graphDto) {
        if (graphDto.getPoints().isEmpty()) {
            log.info("没有知识点需要保存");
            return;
        }

        if (!params.getIsVirtual()) {
            // 实体模式：批量创建KnowledgePoint和KnowledgePointVersion
            persistRealKnowledgeGraphBatch(params, graphDto);
        } else {
            // 虚拟模式：批量创建GraphNode和GraphEdge
            persistVirtualKnowledgeGraphBatch(params, graphDto);
        }
    }

    /**
     * 批量持久化实体知识图谱
     */
    private void persistRealKnowledgeGraphBatch(AiGenerateKnowledgeTaskParameters params, KnowledgeGraphDto graphDto) {
        List<KnowledgePointEntity> pointEntities = new ArrayList<>();
        List<KnowledgePointVersionEntity> versionEntities = new ArrayList<>();
        List<KnowledgePointRelationEntity> relationEntities = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        // 建立AI生成的知识点ID到新生成知识点ID的映射
        Map<Long, Long> aiPointIdToNewIdMap = new HashMap<>();

        // 准备知识点和版本实体
        for (AiKnowledgePoint aiPoint : graphDto.getPoints()) {
            // 生成新的知识点ID（使用Snowflake）
            Long newPointId = HutoolSnowflakeIdGenerator.generateLongId();
            Long versionId = HutoolSnowflakeIdGenerator.generateLongId();

            // 记录映射
            aiPointIdToNewIdMap.put(aiPoint.getId(), newPointId);

            // 创建知识点实体
            KnowledgePointEntity pointEntity = KnowledgePointEntity.builder()
                    .kpId(newPointId)
                    .kpCreatedByUserId(params.getUserId())
                    .kpLearningSpaceId(params.getLearningSpaceId())
                    .kpFolderId(params.getTargetDepositId())
                    .kpCurrentVersionId(versionId)
                    .kpCreatedAt(now)
                    .kpUpdatedAt(now)
                    .build();
            pointEntities.add(pointEntity);

            // 创建版本实体
            KnowledgePointVersionEntity versionEntity = KnowledgePointVersionEntity.builder()
                    .kpvId(versionId)
                    .kpvKnowledgePointId(newPointId)
                    .kpvCreatedByUserId(params.getUserId())
                    .kpvTitle(aiPoint.getTitle())
                    .kpvDefinition(aiPoint.getDefinition())
                    .kpvExplanation(aiPoint.getExplanation())
                    .kpvFormulaOrCode(aiPoint.getFormulaOrCode())
                    .kpvExample(aiPoint.getExample())
                    .kpvCreatedAt(now)
                    .build();
            versionEntities.add(versionEntity);
        }

        // 准备关系实体
        for (AiKnowledgeRelation aiRelation : graphDto.getRelations()) {
            // 将AI生成的知识点ID转换为新生成的知识点ID
            Long newSourceId = aiPointIdToNewIdMap.get(aiRelation.getSourceKnowledgeId());
            Long newTargetId = aiPointIdToNewIdMap.get(aiRelation.getTargetKnowledgeId());

            // 如果映射中不存在，则跳过该关系（可能引用了不存在的知识点）
            if (newSourceId == null || newTargetId == null) {
                log.warn("跳过关系，因为源知识点ID或目标知识点ID不存在于当前生成的知识点中。源ID: {}, 目标ID: {}",
                        aiRelation.getSourceKnowledgeId(), aiRelation.getTargetKnowledgeId());
                continue;
            }

            KnowledgePointRelationEntity relationEntity = KnowledgePointRelationEntity.builder()
                    .kprId(HutoolSnowflakeIdGenerator.generateLongId())
                    .kprCreatedByUserId(params.getUserId())
                    .kprLearningSpaceId(params.getLearningSpaceId())
                    .kprSourcePointId(newSourceId)
                    .kprTargetPointId(newTargetId)
                    .kprRelationType(aiRelation.getRelationType())
                    .kprDescription(aiRelation.getRelationDescription())
                    .kprCreatedAt(now)
                    .kprUpdatedAt(now)
                    .build();
            relationEntities.add(relationEntity);
        }

        // 批量保存
        knowledgePointRepository.saveBatch(pointEntities);
        knowledgePointVersionRepository.saveBatch(versionEntities);
        if (!relationEntities.isEmpty()) {
            knowledgePointRelationRepository.saveBatch(relationEntities);
        }

        log.info("批量保存完成: {} 个知识点, {} 条关系", pointEntities.size(), relationEntities.size());
    }

    /**
     * 批量持久化虚拟知识图谱
     */
    private void persistVirtualKnowledgeGraphBatch(AiGenerateKnowledgeTaskParameters params, KnowledgeGraphDto graphDto) {
        List<GraphNodeEntity> nodeEntities = new ArrayList<>();
        List<GraphEdgeEntity> edgeEntities = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> titleToIdMap = new java.util.HashMap<>();

        // 准备图节点实体
        for (AiKnowledgePoint aiPoint : graphDto.getPoints()) {
            Long nodeId = HutoolSnowflakeIdGenerator.generateLongId();

            // 记录标题到ID的映射
            titleToIdMap.put(aiPoint.getTitle(), nodeId);

            GraphNodeEntity nodeEntity = GraphNodeEntity.builder()
                    .gnId(nodeId)
                    .gnLearningSpaceId(params.getLearningSpaceId())
                    .gnCreatedByUserId(params.getUserId())
                    .gnGraphId(params.getTargetDepositId())
                    .gnTitle(aiPoint.getTitle())
                    .gnDefinition(aiPoint.getDefinition())
                    .gnExplanation(aiPoint.getExplanation())
                    .gnFormulaOrCode(aiPoint.getFormulaOrCode())
                    .gnExample(aiPoint.getExample())
                    .gnIsProjection(false)
                    .gnCreatedAt(now)
                    .gnUpdatedAt(now)
                    .build();
            nodeEntities.add(nodeEntity);
        }

        // 准备图边实体
        for (AiKnowledgeRelation aiRelation : graphDto.getRelations()) {
            String sourceTitle = findTitleByAiPointId(graphDto.getPoints(), aiRelation.getSourceKnowledgeId());
            String targetTitle = findTitleByAiPointId(graphDto.getPoints(), aiRelation.getTargetKnowledgeId());

            Long sourceId = titleToIdMap.get(sourceTitle);
            Long targetId = titleToIdMap.get(targetTitle);

            if (sourceId != null && targetId != null) {
                GraphEdgeEntity edgeEntity = GraphEdgeEntity.builder()
                        .geId(HutoolSnowflakeIdGenerator.generateLongId())
                        .geLearningSpaceId(params.getLearningSpaceId())
                        .geCreatedByUserId(params.getUserId())
                        .geGraphId(params.getTargetDepositId())
                        .geSourceVirtualNodeId(sourceId)
                        .geTargetVirtualNodeId(targetId)
                        .geRelationType(aiRelation.getRelationType())
                        .geDescription(aiRelation.getRelationDescription())
                        .geIsProjection(false)
                        .geCreatedAt(now)
                        .geUpdatedAt(now)
                        .build();
                edgeEntities.add(edgeEntity);
            }
        }

        // 批量保存
        graphNodeRepository.saveBatch(nodeEntities);
        if (!edgeEntities.isEmpty()) {
            graphEdgeRepository.saveBatch(edgeEntities);
        }

        log.info("批量保存完成: {} 个图节点, {} 条图边", nodeEntities.size(), edgeEntities.size());
    }

    /**
     * 根据AiKnowledgePoint的ID查找对应的标题
     */
    private String findTitleByAiPointId(List<AiKnowledgePoint> points, Long aiPointId) {
        return points.stream()
                .filter(point -> point.getId().equals(aiPointId))
                .map(AiKnowledgePoint::getTitle)
                .findFirst()
                .orElse("未知知识点");
    }
}
