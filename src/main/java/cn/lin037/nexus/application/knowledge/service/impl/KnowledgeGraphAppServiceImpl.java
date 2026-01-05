package cn.lin037.nexus.application.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.isolation.enums.IsolationErrorEnum;
import cn.lin037.nexus.application.isolation.port.LearningSpaceRepository;
import cn.lin037.nexus.application.knowledge.enums.KnowledgeErrorCodeEnum;
import cn.lin037.nexus.application.knowledge.port.*;
import cn.lin037.nexus.application.knowledge.service.KnowledgeGraphAppService;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphEdgeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.GraphNodeEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.KnowledgeGraphEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointRelationEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import cn.lin037.nexus.web.rest.v1.knowledge.req.*;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphEdgeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.GraphNodeVO;
import cn.lin037.nexus.web.rest.v1.knowledge.vo.KnowledgeGraphVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱应用服务实现
 *
 * @author LinSanQi
 */
@Service
public class KnowledgeGraphAppServiceImpl implements KnowledgeGraphAppService {

    private final KnowledgeGraphRepository knowledgeGraphRepository;
    private final GraphNodeRepository graphNodeRepository;
    private final GraphEdgeRepository graphEdgeRepository;
    private final KnowledgePointRepository knowledgePointRepository;
    private final KnowledgePointRelationRepository knowledgePointRelationRepository;
    private final KnowledgePointVersionRepository knowledgePointVersionRepository;
    private final LearningSpaceRepository learningSpaceRepository;
    private final KnowledgeFolderRepository knowledgeFolderRepository;

    public KnowledgeGraphAppServiceImpl(KnowledgeGraphRepository knowledgeGraphRepository,
                                        GraphNodeRepository graphNodeRepository,
                                        GraphEdgeRepository graphEdgeRepository,
                                        KnowledgePointRepository knowledgePointRepository,
                                        KnowledgePointRelationRepository knowledgePointRelationRepository,
                                        KnowledgePointVersionRepository knowledgePointVersionRepository, LearningSpaceRepository learningSpaceRepository, KnowledgeFolderRepository knowledgeFolderRepository) {
        this.knowledgeGraphRepository = knowledgeGraphRepository;
        this.graphNodeRepository = graphNodeRepository;
        this.graphEdgeRepository = graphEdgeRepository;
        this.knowledgePointRepository = knowledgePointRepository;
        this.knowledgePointRelationRepository = knowledgePointRelationRepository;
        this.knowledgePointVersionRepository = knowledgePointVersionRepository;
        this.learningSpaceRepository = learningSpaceRepository;
        this.knowledgeFolderRepository = knowledgeFolderRepository;
    }

    @Override
    @Transactional
    public KnowledgeGraphVO createKnowledgeGraph(CreateKnowledgeGraphReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!learningSpaceRepository.existsByIdAndUserId(req.getLearningSpaceId(), userId)) {
            throw new ApplicationException(IsolationErrorEnum.LEARNING_SPACE_NOT_FOUND);
        }

        KnowledgeGraphEntity entity = new KnowledgeGraphEntity();
        entity.setKgId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setKgCreatedByUserId(userId);
        entity.setKgLearningSpaceId(req.getLearningSpaceId());
        entity.setKgTitle(req.getTitle());
        entity.setKgDescription(req.getDescription());
        entity.setKgThumbnailUrl(req.getThumbnailUrl());
        entity.setKgCreatedAt(LocalDateTime.now());
        entity.setKgUpdatedAt(LocalDateTime.now());

        knowledgeGraphRepository.save(entity);

        return KnowledgeGraphVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public KnowledgeGraphVO updateKnowledgeGraph(Long graphId, UpdateKnowledgeGraphReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!knowledgeGraphRepository.existsByIdAndUserId(graphId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NOT_FOUND);
        }

        knowledgeGraphRepository.updateById(graphId, chain -> {
            chain.set(KnowledgeGraphEntity::getKgTitle, req.getTitle());
            chain.set(KnowledgeGraphEntity::getKgDescription, req.getDescription());
            chain.set(KnowledgeGraphEntity::getKgThumbnailUrl, req.getThumbnailUrl());
//            chain.set(KnowledgeGraphEntity::getKgGraphConfigData, req.getGraphConfigData());
            chain.set(KnowledgeGraphEntity::getKgUpdatedAt, LocalDateTime.now());
        });

        KnowledgeGraphEntity entity = knowledgeGraphRepository.findById(graphId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return KnowledgeGraphVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteKnowledgeGraph(Long graphId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验权限
        if (!knowledgeGraphRepository.existsByIdAndUserId(graphId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NOT_FOUND);
        }

        // 删除知识图谱（内部实现了关联内容的级联删除）
        knowledgeGraphRepository.deleteById(graphId);
    }

    @Override
    @Transactional
    public GraphNodeVO createVirtualNode(CreateVirtualNodeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验图谱是否存在
        if (!knowledgeGraphRepository.existsByIdAndUserId(req.getGraphId(), userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NOT_FOUND);
        }

        // 创建虚体节点
        GraphNodeEntity entity = new GraphNodeEntity();
        entity.setGnId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setGnCreatedByUserId(userId);
        entity.setGnGraphId(req.getGraphId());
        entity.setGnIsProjection(false);
        entity.setGnTitle(req.getTitle());
        entity.setGnStyleConfig(req.getStyleConfig().convertToNodeStyleConfig());
        entity.setGnCreatedAt(LocalDateTime.now());
        entity.setGnUpdatedAt(LocalDateTime.now());

        // 保存
        graphNodeRepository.save(entity);

        // 返回
        return GraphNodeVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public List<GraphNodeVO> importProjectionNodes(ImportProjectionNodeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验图谱是否存在
        if (!knowledgeGraphRepository.existsByIdAndUserId(req.getGraphId(), userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NOT_FOUND);
        }

        // 校验请求的实体ID列表是否均存在
        Set<Long> reqPointIds = req.getNodes().keySet();
        List<KnowledgePointEntity> pointEntities = knowledgePointRepository.findByUserIdAndIds(userId, reqPointIds, List.of(KnowledgePointEntity::getKpCurrentVersionId));
        if (pointEntities.size() != reqPointIds.size()) {
            List<KnowledgePointEntity> dontExistIds = pointEntities.stream().filter(point -> !point.getKpCreatedByUserId().equals(userId)).toList();
            throw ApplicationException.withType(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "提供的知识点ID列表中存在找不到的知识点", dontExistIds, List.class);
        }

        // TODO：当前这样子的查询存在性能问题，需要优化，应该与上面的`knowledgePointRepository.findByUserIdAndIds`合并为一次数据查询
        List<Long> currentVersionIds = pointEntities.stream().map(KnowledgePointEntity::getKpCurrentVersionId).toList();
        List<KnowledgePointVersionEntity> versionEntities = knowledgePointVersionRepository.findByIds(currentVersionIds, List.of(
                KnowledgePointVersionEntity::getKpvTitle,
                KnowledgePointVersionEntity::getKpvDefinition,
                KnowledgePointVersionEntity::getKpvExplanation,
                KnowledgePointVersionEntity::getKpvFormulaOrCode,
                KnowledgePointVersionEntity::getKpvExample,
                KnowledgePointVersionEntity::getKpvDifficulty
        ));
        if (versionEntities.size() != currentVersionIds.size()) {
            List<Long> notExistIds = versionEntities.stream().map(KnowledgePointVersionEntity::getKpvKnowledgePointId).toList();
            throw ApplicationException.withType(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND, "提供的知识点ID列表中存在找不到的知识点", notExistIds, List.class);
        }

        // 获取请求的节点样式
        Map<Long, NodeStyleConfigReq> nodes = req.getNodes();

        // 创建投影节点
        List<GraphNodeEntity> nodeEntities = versionEntities.stream().map(version -> GraphNodeEntity.builder()
                .gnId(HutoolSnowflakeIdGenerator.generateLongId())
                .gnCreatedByUserId(userId)
                .gnGraphId(req.getGraphId())
                .gnIsProjection(true)
                .gnEntityId(version.getKpvKnowledgePointId())
                .gnTitle(version.getKpvTitle())
                .gnDefinition(version.getKpvDefinition())
                .gnExplanation(version.getKpvExplanation())
                .gnFormulaOrCode(version.getKpvFormulaOrCode())
                .gnExample(version.getKpvExample())
                .gnStyleConfig(nodes.getOrDefault(version.getKpvKnowledgePointId(), null).convertToNodeStyleConfig())
                .gnCreatedAt(LocalDateTime.now())
                .gnUpdatedAt(LocalDateTime.now())
                .build()).toList();

        // 保存
        graphNodeRepository.saveBatch(nodeEntities);

        // 处理导入节点之间的关系
        // 1. 根据导入的知识点ID集合，查询它们之间所有已存在的KnowledgePointRelationEntity
        List<KnowledgePointRelationEntity> existingRelations = knowledgePointRelationRepository
                .findBetweenPointIds(reqPointIds, userId, List.of(
                        KnowledgePointRelationEntity::getKprId,
                        KnowledgePointRelationEntity::getKprSourcePointId,
                        KnowledgePointRelationEntity::getKprTargetPointId,
                        KnowledgePointRelationEntity::getKprRelationType,
                        KnowledgePointRelationEntity::getKprDescription
                ));

        // 2. 为每一条实体关系创建对应的虚体边（投影边）
        if (!existingRelations.isEmpty()) {
            // 创建节点ID到GraphNode ID的映射
            Map<Long, Long> pointIdToNodeIdMap = nodeEntities.stream()
                    .collect(Collectors.toMap(
                            node -> pointEntities.stream()
                                    .filter(point -> point.getKpId().equals(node.getGnEntityId()))
                                    .findFirst()
                                    .map(KnowledgePointEntity::getKpId)
                                    .orElse(null),
                            GraphNodeEntity::getGnId
                    ))
                    .entrySet().stream()
                    .filter(entry -> entry.getKey() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // 3. 创建投影边
            List<GraphEdgeEntity> projectionEdges = existingRelations.stream()
                    .map(relation -> {
                        Long sourceNodeId = pointIdToNodeIdMap.get(relation.getKprSourcePointId());
                        Long targetNodeId = pointIdToNodeIdMap.get(relation.getKprTargetPointId());

                        if (sourceNodeId != null && targetNodeId != null) {
                            GraphEdgeEntity edge = new GraphEdgeEntity();
                            edge.setGeId(HutoolSnowflakeIdGenerator.generateLongId());
                            edge.setGeCreatedByUserId(userId);
                            edge.setGeGraphId(req.getGraphId());
                            edge.setGeSourceVirtualNodeId(sourceNodeId);
                            edge.setGeTargetVirtualNodeId(targetNodeId);
                            edge.setGeIsProjection(true);
                            edge.setGeRelationEntityRelationId(relation.getKprId());
                            edge.setGeRelationType(relation.getKprRelationType());
                            edge.setGeDescription(relation.getKprDescription());
                            edge.setGeCreatedAt(LocalDateTime.now());
                            edge.setGeUpdatedAt(LocalDateTime.now());
                            return edge;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 4. 批量保存投影边
            if (!projectionEdges.isEmpty()) {
                graphEdgeRepository.saveBatch(projectionEdges);
            }
        }

        // 返回
        return nodeEntities.stream().map(GraphNodeVO::fromEntity).toList();
    }

    @Override
    @Transactional
    public GraphNodeVO updateGraphNode(Long nodeId, UpdateGraphNodeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!graphNodeRepository.existsByIdAndUserId(nodeId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NODE_NOT_FOUND);
        }

        graphNodeRepository.updateById(nodeId, chain ->
                chain.set(GraphNodeEntity::getGnTitle, req.getTitle())
                        .set(GraphNodeEntity::getGnDefinition, req.getDefinition())
                        .set(GraphNodeEntity::getGnExplanation, req.getExplanation())
                        .set(GraphNodeEntity::getGnFormulaOrCode, req.getFormulaOrCode())
                        .set(GraphNodeEntity::getGnExample, req.getExample())
                        .set(GraphNodeEntity::getGnStyleConfig, req.getStyleConfig())
                        .set(GraphNodeEntity::getGnUpdatedAt, LocalDateTime.now()));

        GraphNodeEntity entity = graphNodeRepository.findById(nodeId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return GraphNodeVO.fromEntity(entity);
    }

    @Override
    @Transactional
    public void deleteGraphNode(Long nodeId) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!graphNodeRepository.existsByIdAndUserId(nodeId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }

        // 先删除相关边
        graphEdgeRepository.deleteByNodeId(nodeId);
        // 再删除节点
        graphNodeRepository.deleteById(nodeId);
    }

    /**
     * 将图节点实体化为知识点
     *
     * <p>该方法将知识图谱中的虚体节点转换为实际的知识点实体。如果请求中未提供知识点ID，则创建新的知识点及版本；
     * 如果提供了知识点ID，则创建新版本并关联到现有知识点。</p>
     *
     * @param nodeId 要实体化的节点ID
     * @param req    实体化请求参数，包含目标知识点ID（可选）和文件夹ID等信息
     * @return 转换后的图节点VO对象
     * @throws ApplicationException 如果节点不存在、权限不足或保存失败
     */
    @Override
    @Transactional
    public GraphNodeVO materializeNode(Long nodeId, MaterializeNodeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验节点是否存在且属于当前用户
        if (!graphNodeRepository.existsByIdAndUserId(nodeId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NODE_NOT_FOUND);
        }
        // 查询节点基本信息
        GraphNodeEntity graphNodeEntity = graphNodeRepository.findByIdAndUserId(nodeId, userId, List.of(
                GraphNodeEntity::getGnEntityId,
                GraphNodeEntity::getGnIsProjection,
                GraphNodeEntity::getGnLearningSpaceId,
                GraphNodeEntity::getGnTitle,
                GraphNodeEntity::getGnDefinition,
                GraphNodeEntity::getGnExplanation,
                GraphNodeEntity::getGnFormulaOrCode,
                GraphNodeEntity::getGnExample
        )).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.GRAPH_NODE_NOT_FOUND));

        // 如果未提供目标知识点ID，则创建新知识点
        if (req.getKnowledgePointId() == null) {
            // 校验目标文件夹是否存在且属于当前用户
            if (!knowledgeFolderRepository.existsByIdAndUserId(req.getFolderId(), userId)) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.FOLDER_NOT_FOUND);
            }
            // 构建知识点实体
            KnowledgePointEntity entity = KnowledgePointEntity.builder()
                    .kpId(HutoolSnowflakeIdGenerator.generateLongId())
                    .kpCreatedByUserId(userId)
                    .kpLearningSpaceId(graphNodeEntity.getGnLearningSpaceId())
                    .kpFolderId(req.getFolderId())
                    .kpCreatedAt(LocalDateTime.now())
                    .kpUpdatedAt(LocalDateTime.now())
                    .build();

            // 构建知识点版本实体
            KnowledgePointVersionEntity versionEntity = KnowledgePointVersionEntity.builder()
                    .kpvCreatedByUserId(userId)
                    .kpvTitle(graphNodeEntity.getGnTitle())
                    .kpvDefinition(graphNodeEntity.getGnDefinition())
                    .kpvExplanation(graphNodeEntity.getGnExplanation())
                    .kpvFormulaOrCode(graphNodeEntity.getGnFormulaOrCode())
                    .kpvExample(graphNodeEntity.getGnExample())
                    .kpvDifficulty(req.getDifficulty() == null ? BigDecimal.valueOf(0.5) : req.getDifficulty())
                    .kpvKnowledgePointId(entity.getKpId())
                    .kpvCreatedAt(LocalDateTime.now())
                    .build();
            // 保存知识点版本并获取版本ID
            Long versionId = knowledgePointVersionRepository.save(versionEntity)
                    .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_SAVE_FAILED));

            // 设置当前版本ID并保存知识点
            entity.setKpCurrentVersionId(versionId);
            knowledgePointRepository.save(entity);

            // 更新节点为投影节点
            graphNodeRepository.updateById(nodeId, updateChain ->
                    updateChain.set(GraphNodeEntity::getGnIsProjection, true)
                            .set(GraphNodeEntity::getGnEntityId, entity.getKpId())
                            .set(GraphNodeEntity::getGnUpdatedAt, LocalDateTime.now())
            );
        } else {
            // 直接投影映射到指定知识点
            // 校验目标知识点是否存在且属于当前用户
            if (!knowledgePointRepository.existsByIdAndUserId(req.getKnowledgePointId(), userId)) {
                throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
            }

            // 构建知识点版本实体
            KnowledgePointVersionEntity versionEntity = KnowledgePointVersionEntity.builder()
                    .kpvCreatedByUserId(userId)
                    .kpvTitle(graphNodeEntity.getGnTitle())
                    .kpvDefinition(graphNodeEntity.getGnDefinition())
                    .kpvExplanation(graphNodeEntity.getGnExplanation())
                    .kpvFormulaOrCode(graphNodeEntity.getGnFormulaOrCode())
                    .kpvExample(graphNodeEntity.getGnExample())
                    .kpvDifficulty(req.getDifficulty() == null ? BigDecimal.valueOf(0.5) : req.getDifficulty())
                    .kpvKnowledgePointId(req.getKnowledgePointId())
                    .kpvCreatedAt(LocalDateTime.now())
                    .build();
            // 保存知识点版本并获取版本ID
            Long versionId = knowledgePointVersionRepository.save(versionEntity)
                    .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_SAVE_FAILED, "保存知识点版本失败"));

            // 更新知识点的当前版本
            knowledgePointRepository.updateById(req.getKnowledgePointId(),
                    updateChain -> updateChain.set(KnowledgePointEntity::getKpCurrentVersionId, versionId)
                            .set(KnowledgePointEntity::getKpUpdatedAt, LocalDateTime.now())
            );

            // 更新节点为投影节点
            graphNodeRepository.updateById(nodeId, updateChain ->
                    updateChain.set(GraphNodeEntity::getGnIsProjection, true)
                            .set(GraphNodeEntity::getGnEntityId, req.getKnowledgePointId())
                            .set(GraphNodeEntity::getGnUpdatedAt, LocalDateTime.now())
            );
        }

        // 处理与此节点相关的边的实体化
        // 1. 查询所有以此节点为源或目标的虚体边
        List<GraphEdgeEntity> relatedEdges = graphEdgeRepository.findByNodeId(nodeId, userId, List.of(
                GraphEdgeEntity::getGeId,
                GraphEdgeEntity::getGeSourceVirtualNodeId,
                GraphEdgeEntity::getGeTargetVirtualNodeId,
                GraphEdgeEntity::getGeIsProjection,
                GraphEdgeEntity::getGeRelationType,
                GraphEdgeEntity::getGeDescription
        ));

        // 2. 遍历这些边，检查是否满足实体化条件
        List<GraphEdgeEntity> edgesToMaterialize = new ArrayList<>();
        List<KnowledgePointRelationEntity> relationsToCreate = new ArrayList<>();

        // 获取当前节点的实体ID
        Long currentNodeEntityId = req.getKnowledgePointId();
        if (currentNodeEntityId == null) {
            // 如果没有指定知识点ID，说明是创建新知识点的情况
            // 需要从更新后的节点中获取实体ID
            GraphNodeEntity updatedNode = graphNodeRepository.findById(nodeId, List.of(
                    GraphNodeEntity::getGnEntityId
            )).orElse(null);
            if (updatedNode != null) {
                currentNodeEntityId = updatedNode.getGnEntityId();
            }
        }

        for (GraphEdgeEntity edge : relatedEdges) {
            // 跳过已经是投影边的边
            if (edge.getGeIsProjection()) {
                continue;
            }

            // 3. 找到邻居节点ID
            Long neighborNodeId = edge.getGeSourceVirtualNodeId().equals(nodeId) ?
                    edge.getGeTargetVirtualNodeId() : edge.getGeSourceVirtualNodeId();

            // 4. 检查邻居节点是否是投影节点
            GraphNodeEntity neighborNode = graphNodeRepository.findById(neighborNodeId, List.of(
                    GraphNodeEntity::getGnIsProjection,
                    GraphNodeEntity::getGnEntityId
            )).orElse(null);

            if (neighborNode != null && neighborNode.getGnIsProjection() && neighborNode.getGnEntityId() != null) {
                // 5. 邻居节点也是投影节点，这条边满足实体化条件
                // 确定源和目标知识点ID
                Long sourcePointId, targetPointId;
                if (edge.getGeSourceVirtualNodeId().equals(nodeId)) {
                    sourcePointId = currentNodeEntityId;
                    targetPointId = neighborNode.getGnEntityId();
                } else {
                    sourcePointId = neighborNode.getGnEntityId();
                    targetPointId = currentNodeEntityId;
                }

                // 创建知识点关系实体
                KnowledgePointRelationEntity relation = KnowledgePointRelationEntity.builder()
                        .kprId(HutoolSnowflakeIdGenerator.generateLongId())
                        .kprCreatedByUserId(userId)
                        .kprSourcePointId(sourcePointId)
                        .kprTargetPointId(targetPointId)
                        .kprRelationType(edge.getGeRelationType())
                        .kprDescription(edge.getGeDescription())
                        .kprCreatedAt(LocalDateTime.now())
                        .kprUpdatedAt(LocalDateTime.now())
                        .build();

                relationsToCreate.add(relation);
                edgesToMaterialize.add(edge);
            }
        }

        // 6. 批量保存新的知识点关系
        if (!relationsToCreate.isEmpty()) {
            knowledgePointRelationRepository.saveBatch(relationsToCreate);

            // 7. 批量实体化对应的边
            List<Long> edgeIds = edgesToMaterialize.stream().map(GraphEdgeEntity::getGeId).collect(Collectors.toList());
            List<Long> relationIds = relationsToCreate.stream().map(KnowledgePointRelationEntity::getKprId).collect(Collectors.toList());
            graphEdgeRepository.batchMaterialize(edgeIds, relationIds);
        }

        // 返回更新后的节点
        GraphNodeEntity updatedEntity = graphNodeRepository.findById(nodeId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return GraphNodeVO.fromEntity(updatedEntity);
    }

    /**
     * 创建虚体边
     * 此方法只创建纯虚体边 (geIsProjection=false)，连接两个虚体节点。不涉及任何实体化逻辑。
     *
     * @param req 创建虚体边请求
     * @return 创建的虚体边
     * @throws ApplicationException 应用异常
     */
    @Override
    @Transactional
    public GraphEdgeVO createGraphEdge(CreateGraphEdgeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!knowledgeGraphRepository.existsByIdAndUserId(req.getGraphId(), userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }

        GraphEdgeEntity entity = new GraphEdgeEntity();
        entity.setGeId(HutoolSnowflakeIdGenerator.generateLongId());
        entity.setGeCreatedByUserId(userId);
        entity.setGeGraphId(req.getGraphId());
        entity.setGeSourceVirtualNodeId(req.getSourceNodeId());
        entity.setGeTargetVirtualNodeId(req.getTargetNodeId());
        // 虚体边
        entity.setGeIsProjection(false);
        entity.setGeRelationType(req.getRelationType());
        entity.setGeDescription(req.getDescription());
        entity.setGeStyleConfig(req.getStyleConfig());
        entity.setGeCreatedAt(LocalDateTime.now());
        entity.setGeUpdatedAt(LocalDateTime.now());

        graphEdgeRepository.save(entity);

        return GraphEdgeVO.fromEntity(entity);
    }

    /**
     * 更新虚体边
     * 注：此方法只更新虚体边自身的属性（类型、描述、样式）。如果边已经是投影，则不允许修改类型和描述，因为其属性应与实体关系一致。
     *
     * @param edgeId 虚体边ID
     * @param req    更新虚体边请求
     * @return 更新的虚体边
     * @throws ApplicationException 应用异常
     */
    @Override
    @Transactional
    public GraphEdgeVO updateGraphEdge(Long edgeId, UpdateGraphEdgeReq req) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!graphEdgeRepository.existsByIdAndUserId(edgeId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }

        // 查询边的投影状态
        GraphEdgeEntity existingEdge = graphEdgeRepository.findById(edgeId, List.of(
                GraphEdgeEntity::getGeIsProjection
        )).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        // 如果边已经是投影边，则不允许修改关系类型和描述，因为这些属性应与实体关系保持一致
        if (existingEdge.getGeIsProjection()) {
            // 只允许更新样式配置
            graphEdgeRepository.updateById(edgeId, chain -> {
                chain.set(GraphEdgeEntity::getGeStyleConfig, req.getStyleConfig());
                chain.set(GraphEdgeEntity::getGeUpdatedAt, LocalDateTime.now());
            });
        } else {
            // 虚体边可以更新所有属性
            graphEdgeRepository.updateById(edgeId, chain -> {
                chain.set(GraphEdgeEntity::getGeRelationType, req.getRelationType());
                chain.set(GraphEdgeEntity::getGeDescription, req.getDescription());
                chain.set(GraphEdgeEntity::getGeStyleConfig, req.getStyleConfig());
                chain.set(GraphEdgeEntity::getGeUpdatedAt, LocalDateTime.now());
            });
        }

        GraphEdgeEntity entity = graphEdgeRepository.findById(edgeId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return GraphEdgeVO.fromEntity(entity);
    }

    /**
     * 删除虚体边
     * 删除边时需要区分处理：
     * 1. 如果是投影边，根据isDeleteEntity参数决定是否删除关联的实体关系
     * 2. 如果是纯虚体边，直接删除即可
     *
     * @param edgeId         虚体边ID
     * @param isDeleteEntity 是否删除实体关系
     * @throws ApplicationException 应用异常
     */
    @Override
    @Transactional
    public void deleteGraphEdge(Long edgeId, boolean isDeleteEntity) {
        Long userId = StpUtil.getLoginIdAsLong();

        if (!graphEdgeRepository.existsByIdAndUserId(edgeId, userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND);
        }

        // 1. 先查询出这条边的投影状态和关联的实体关系ID
        GraphEdgeEntity edgeEntity = graphEdgeRepository.findById(edgeId, List.of(
                GraphEdgeEntity::getGeIsProjection,
                GraphEdgeEntity::getGeRelationEntityRelationId
        )).orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        // 2. 如果是投影边，根据isDeleteEntity参数决定是否需要删除关联的实体关系
        if (edgeEntity.getGeIsProjection() && edgeEntity.getGeRelationEntityRelationId() != null && isDeleteEntity) {
            // 删除关联的知识点关系实体（这会自动解除其他投影边的关联）
            knowledgePointRelationRepository.deleteById(edgeEntity.getGeRelationEntityRelationId());
        }

        // 3. 删除边本身
        graphEdgeRepository.deleteById(edgeId);
    }

    /**
     * 实体化虚体边
     * 该方法是"手动"将一条满足条件的虚体边实体化的过程。
     * 逻辑流程：
     * 1. 校验边存在且属于用户。
     * 2. 校验边当前不是投影边，否则是重复操作。
     * 3. 查找边的源节点和目标节点。
     * 4. 核心条件：校验源节点和目标节点必须都已经是投影节点，因为只有这样，实体关系才有可以依附的两个实体端点。
     * 5. 如果条件满足，创建新的KnowledgePointRelationEntity，其source/target ID来自于虚体节点的gnEntityId。
     * 6. 保存新的实体关系。
     * 7. 更新GraphEdgeEntity，将其标记为投影并关联上新创建的实体关系ID。
     *
     * @param edgeId 虚体边ID
     * @return 实体化的虚体边
     * @throws ApplicationException 应用异常
     */
    @Override
    @Transactional
    public GraphEdgeVO materializeEdge(Long edgeId) {
        Long userId = StpUtil.getLoginIdAsLong();

        GraphEdgeEntity edgeEntity = graphEdgeRepository.findById(edgeId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        if (!edgeEntity.getGeCreatedByUserId().equals(userId)) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.FORBIDDEN);
        }

        if (edgeEntity.getGeIsProjection()) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED); // 已是投影边，无法实体化
        }

        // 检查源节点和目标节点是否都是投影节点
        GraphNodeEntity sourceNode = graphNodeRepository.findById(edgeEntity.getGeSourceVirtualNodeId(), Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));
        GraphNodeEntity targetNode = graphNodeRepository.findById(edgeEntity.getGeTargetVirtualNodeId(), Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_NOT_FOUND));

        if (!sourceNode.getGnIsProjection() || !targetNode.getGnIsProjection()) {
            throw new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED); // 只有连接两个投影节点的边才能实体化
        }

        // 创建知识点关系实体
        KnowledgePointRelationEntity relation = KnowledgePointRelationEntity.builder()
                .kprId(HutoolSnowflakeIdGenerator.generateLongId())
                .kprCreatedByUserId(userId)
                .kprSourcePointId(sourceNode.getGnEntityId())
                .kprTargetPointId(targetNode.getGnEntityId())
                .kprRelationType(edgeEntity.getGeRelationType())
                .kprDescription(edgeEntity.getGeDescription())
                .kprCreatedAt(LocalDateTime.now())
                .kprUpdatedAt(LocalDateTime.now())
                .build();

        knowledgePointRelationRepository.save(relation);

        // 更新边为投影边
        graphEdgeRepository.materialize(edgeId, relation.getKprId());

        GraphEdgeEntity updatedEntity = graphEdgeRepository.findById(edgeId, Collections.emptyList())
                .orElseThrow(() -> new ApplicationException(KnowledgeErrorCodeEnum.KNOWLEDGE_UPDATE_FAILED));
        return GraphEdgeVO.fromEntity(updatedEntity);
    }
} 