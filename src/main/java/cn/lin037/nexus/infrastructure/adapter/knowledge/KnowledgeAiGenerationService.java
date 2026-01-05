package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.lin037.nexus.infrastructure.adapter.knowledge.constant.KnowledgeAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.*;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StructuredOutputTool;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识生成服务 - 核心编排逻辑
 * <p>
 * 该服务负责编排AI调用链，通过串联多个AI调用来构建完整的知识图谱。
 * 只处理DTO，不接触实体（Entity）。
 *
 * @author LinSanQi
 */
@Slf4j
@Service
public class KnowledgeAiGenerationService {

    private static final String MODEL_NAME = "qwen-max";
    private static final String USED_FOR = "STRUCTURED_OUTPUT_EXPLANATION";

    // 迭代控制常量
    private static final int MAX_TOPIC_ITERATIONS = 10;
    private static final int MAX_POINT_ITERATIONS_PER_TOPIC = 5;
    private static final int MAX_EXPANSION_ITERATIONS = 5;

    // 每次调用生成数量限制
    private static final int MAX_POINTS_PER_CALL = 3;
    /**
     * 创建TopicExpansionResult示例对象静态方法
     * 用于指导AI返回正确的数据结构
     */
    private static final TopicExpansionResult TOPIC_EXPANSION_EXAMPLE = new TopicExpansionResult(
            List.of(
                    new TopicDto("Java基础语法", "掌握Java的基本语法结构和编程规范", List.of(1001L, 1002L), 8),
                    new TopicDto("面向对象编程", "理解Java中的类、对象、继承、多态等核心概念", List.of(1003L, 1004L), 10)
            ),
            List.of(1001L, 1002L, 1003L, 1004L),
            false
    );
    /**
     * 创建KnowledgePointGenerationResult示例对象静态方法
     * 用于指导AI返回正确的数据结构
     */
    private static final KnowledgePointGenerationResult KNOWLEDGE_POINT_EXAMPLE = new KnowledgePointGenerationResult(
            List.of(
                    new KnowledgePointDto("知识点标题1", "对知识点的精确定义。", "对知识点的详细讲解。", "相关的公式或代码示例。", "一个具体的使用示例。")
            ),
            false
    );
    /**
     * 创建KnowledgeRelationGenerationResult示例对象静态方法
     * 用于指导AI返回正确的数据结构
     */
    private static final KnowledgeRelationGenerationResult RELATION_EXAMPLE = new KnowledgeRelationGenerationResult(
            List.of(
                    new AiKnowledgeRelation(1L, 2L, "PRE_REQUISITE", "理解“仓库”是进行“提交”操作的基础。"),
                    new AiKnowledgeRelation(2L, 3L, "PRE_REQUISITE", "理解“提交”操作是进行“推送”操作的基础。")
            ),
            false
    );
    /**
     * 创建KnowledgePointExpansionResult示例对象静态方法
     * 用于指导AI返回正确的数据结构
     */
    private static final KnowledgePointExpansionResult EXPANSION_EXAMPLE = new KnowledgePointExpansionResult(
            List.of(
                    new KnowledgePointDto("新知识点标题1", "对新知识点的精确定义。", "对新知识点的详细讲解。", "相关的公式或代码示例。", "一个具体的使用示例。")
            ),
            false
    );
    private final ChatModel chatModel;
    private final StructuredOutputTool structuredOutputTool;

    public KnowledgeAiGenerationService(AiCoreService aiCoreService, StructuredOutputTool structuredOutputTool) {
        this.chatModel = aiCoreService.getChatModel(MODEL_NAME, USED_FOR);
        this.structuredOutputTool = structuredOutputTool;
    }

    /**
     * 生成完整的知识图谱
     *
     * @param userRequirement 用户核心需求
     * @param chunks          参考资料列表
     * @return 包含知识图谱和Token使用情况的结果
     */
    public Optional<AiGenerationResult<KnowledgeGraphDto>> generateKnowledgeGraph(String userRequirement, List<AiChunkContent> chunks) {
        log.info("开始生成知识图谱，用户需求: {}", userRequirement);

        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();

        // 1. 主题扩展阶段 - 循环调用直到完成
        List<TopicDto> allTopics = expandTopicsUntilComplete(userRequirement, chunks, tokenAccumulator);
        log.info("主题扩展完成，生成了 {} 个主题", allTopics.size());
        if (allTopics.isEmpty()) {
            return Optional.of(AiGenerationResult.empty(tokenAccumulator.getTotal()));
        }

        // 2. 知识点生成阶段 - 为每个主题生成知识点
        List<AiKnowledgePoint> allPoints = generateKnowledgePointsForTopics(userRequirement, allTopics, chunks, tokenAccumulator);
        log.info("知识点生成完成，总共生成了 {} 个知识点", allPoints.size());
        if (allPoints.isEmpty()) {
            return Optional.of(AiGenerationResult.empty(tokenAccumulator.getTotal()));
        }

        // 3. 关系生成阶段 - 循环调用直到完成
        List<AiKnowledgeRelation> relations = generateRelationsUntilComplete(allPoints, tokenAccumulator);
        log.info("知识图谱的知识关系生成完成，生成了 {} 条关系", relations.size());

        // 4. 返回完整的DTO和Token使用情况
        KnowledgeGraphDto knowledgeGraph = new KnowledgeGraphDto(allPoints, relations);
        return Optional.of(AiGenerationResult.success(knowledgeGraph, tokenAccumulator.getTotal()));
    }

    /**
     * 为给定的知识点列表生成关系
     * 专门为ExplanationTaskExecutor提供的公共接口
     *
     * @param knowledgePoints  知识点列表
     * @param tokenAccumulator Token使用量累加器
     * @return 知识点关系列表
     */
    public List<AiKnowledgeRelation> generateRelationsForPoints(List<AiKnowledgePoint> knowledgePoints, TokenUsageAccumulator tokenAccumulator) {
        return generateRelationsUntilComplete(knowledgePoints, tokenAccumulator);
    }

    /**
     * 主题扩展阶段 - 循环调用直到完成
     *
     * @param userRequirement  用户核心需求
     * @param chunks           参考资料列表
     * @param tokenAccumulator Token使用量累加器
     * @return 所有生成的主题列表
     */
    private List<TopicDto> expandTopicsUntilComplete(String userRequirement, List<AiChunkContent> chunks, TokenUsageAccumulator tokenAccumulator) {
        List<TopicDto> allTopics = new ArrayList<>();
        List<Long> coveredChunkIds = new ArrayList<>();
        boolean isComplete = false;
        int iteration = 0;

        while (!isComplete && iteration < MAX_TOPIC_ITERATIONS) {
            iteration++;
            log.info("主题扩展第 {} 轮迭代开始", iteration);

            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.TopicExpander.build(
                    userRequirement, chunks, coveredChunkIds, allTopics);

            // 构建系统提示和用户提示
            StructResult<TopicExpansionResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    TOPIC_EXPANSION_EXAMPLE,
                    TopicExpansionResult.class
            );

            // 统计Token使用量
            tokenAccumulator.add(result.getTokenUsage());

            // 获取并处理主题扩展结果
            if (result.getResult() != null) {
                TopicExpansionResult expansionResult = result.getResult();
                // 添加新生成的主题到总主题列表
                if (expansionResult.getNewTopics() != null && !expansionResult.getNewTopics().isEmpty()) {
                    allTopics.addAll(expansionResult.getNewTopics());
                    log.info("第 {} 轮生成了 {} 个新主题", iteration, expansionResult.getNewTopics().size());
                }
                // 更新已覆盖的chunk ID列表
                if (expansionResult.getNewlyCoveredChunkIds() != null) {
                    coveredChunkIds.addAll(expansionResult.getNewlyCoveredChunkIds());
                }
                // 更新完成态
                isComplete = expansionResult.getIsComplete();
            } else {
                log.warn("第 {} 轮主题扩展返回空结果", iteration);
                break;
            }
        }

        if (allTopics.isEmpty()) {
            log.warn("主题扩展未生成任何主题，使用用户需求作为默认主题");
        }

        return allTopics;
    }

    /**
     * 为所有主题生成知识点（并生成ID）
     *
     * @param userRequirement  用户核心需求
     * @param allTopics        所有主题列表
     * @param chunks           参考资料列表
     * @param tokenAccumulator Token使用量累加器
     * @return 所有生成的知识点列表
     */
    private List<AiKnowledgePoint> generateKnowledgePointsForTopics(String userRequirement,
                                                                    List<TopicDto> allTopics,
                                                                    List<AiChunkContent> chunks,
                                                                    TokenUsageAccumulator tokenAccumulator) {
        List<KnowledgePointDto> allPoints = new ArrayList<>();

        for (TopicDto topic : allTopics) {
            List<KnowledgePointDto> topicPoints = generatePointsForSingleTopic(userRequirement, topic, allPoints, chunks, tokenAccumulator);
            allPoints.addAll(topicPoints);
            log.info("主题 '{}' 生成了 {} 个知识点", topic.getTopicTitle(), topicPoints.size());
        }

        return allPoints.stream().map(
                dto -> new AiKnowledgePoint(HutoolSnowflakeIdGenerator.generateLongId(), dto.getTitle(), dto.getDefinition(), dto.getExplanation(), dto.getFormulaOrCode(), dto.getExample())
        ).toList();
    }

    /**
     * 为单个主题生成知识点 - 循环调用直到完成
     *
     * @param userRequirement  用户核心需求
     * @param topic            主题
     * @param existingPoints   已存在的知识点列表
     * @param allChunks        参考资料列表
     * @param tokenAccumulator Token使用量累加器
     * @return 新生成的知识点列表
     */
    private List<KnowledgePointDto> generatePointsForSingleTopic(String userRequirement,
                                                                 TopicDto topic,
                                                                 List<KnowledgePointDto> existingPoints,
                                                                 List<AiChunkContent> allChunks,
                                                                 TokenUsageAccumulator tokenAccumulator) {
        List<KnowledgePointDto> topicPoints = new ArrayList<>();
        boolean isComplete = false;
        int iteration = 0;

        // 获取相关的参考资料
        List<AiChunkContent> relevantChunks = getRelevantChunks(allChunks, topic.getTargetChunkIds());

        while (!isComplete && iteration < MAX_POINT_ITERATIONS_PER_TOPIC) {
            iteration++;
            log.info("主题 '{}' 知识点生成第 {} 轮迭代开始", topic.getTopicTitle(), iteration);

            // 获取所有已存在的知识点标题
            List<String> allExistingTitles = new ArrayList<>();
            allExistingTitles.addAll(existingPoints.stream().map(KnowledgePointDto::getTitle).toList());
            allExistingTitles.addAll(topicPoints.stream().map(KnowledgePointDto::getTitle).toList());

            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.PointGenerator.build(
                    userRequirement, MAX_POINTS_PER_CALL, topic, allExistingTitles, relevantChunks);

            // 使用静态示例对象
            StructResult<KnowledgePointGenerationResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    KNOWLEDGE_POINT_EXAMPLE,
                    KnowledgePointGenerationResult.class
            );

            // 统计Token使用量
            tokenAccumulator.add(result.getTokenUsage());

            if (result.getResult() != null) {
                KnowledgePointGenerationResult generationResult = result.getResult();
                if (generationResult.getKnowledgePoints() != null && !generationResult.getKnowledgePoints().isEmpty()) {
                    topicPoints.addAll(generationResult.getKnowledgePoints());
                    log.info("主题 '{}' 第 {} 轮生成了 {} 个知识点", topic.getTopicTitle(), iteration, generationResult.getKnowledgePoints().size());
                }
                isComplete = generationResult.getIsComplete();
            } else {
                log.warn("主题 '{}' 第 {} 轮知识点生成返回空结果", topic.getTopicTitle(), iteration);
                break;
            }
        }

        return topicPoints;
    }

    /**
     * 根据chunk ID列表获取相关的参考资料
     */
    private List<AiChunkContent> getRelevantChunks(List<AiChunkContent> allChunks, List<Long> targetChunkIds) {
        if (allChunks == null || targetChunkIds == null || targetChunkIds.isEmpty()) {
            return new ArrayList<>();
        }

        return allChunks.stream()
                .filter(chunk -> targetChunkIds.contains(chunk.getChunkId()))
                .collect(Collectors.toList());
    }

    /**
     * 关系生成阶段 - 循环调用直到完成
     *
     * @param allPoints        所有知识点
     * @param tokenAccumulator Token使用量累加器
     * @return 知识点关系列表
     */
    private List<AiKnowledgeRelation> generateRelationsUntilComplete(List<AiKnowledgePoint> allPoints, TokenUsageAccumulator tokenAccumulator) {
        if (allPoints.size() < 2) {
            log.info("知识点数量少于2个，跳过关系生成");
            return new ArrayList<>();
        }

        List<AiKnowledgeRelation> allRelations = new ArrayList<>();
        boolean isComplete = false;
        int iteration = 0;
        int MAX_RELATION_ITERATIONS = Math.min(allPoints.size() * 2, allPoints.size() * allPoints.size());

        while (!isComplete && iteration < MAX_RELATION_ITERATIONS) {
            iteration++;
            log.info("关系生成第 {} 轮迭代开始", iteration);

            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.RelationGenerator.build(allPoints, allRelations);

            // 使用静态示例对象
            StructResult<KnowledgeRelationGenerationResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    RELATION_EXAMPLE,
                    KnowledgeRelationGenerationResult.class
            );

            // 统计Token使用量
            tokenAccumulator.add(result.getTokenUsage());

            if (result.getResult() != null) {
                KnowledgeRelationGenerationResult relationResult = result.getResult();
                if (relationResult.getRelations() != null && !relationResult.getRelations().isEmpty()) {
                    // 验证生成的关系
                    List<AiKnowledgeRelation> validRelations = new ArrayList<>();
                    for (AiKnowledgeRelation relation : relationResult.getRelations()) {
                        if (isValidRelation(relation)) {
                            validRelations.add(relation);
                        } else {
                            log.warn("跳过无效关系: sourceId={}, targetId={}, type={}, description={}",
                                    relation.getSourceKnowledgeId(),
                                    relation.getTargetKnowledgeId(),
                                    relation.getRelationType(),
                                    relation.getRelationDescription());
                        }
                    }
                    allRelations.addAll(validRelations);
                    log.info("第 {} 轮生成了 {} 条关系，有效关系 {} 条",
                            iteration, relationResult.getRelations().size(), validRelations.size());
                }
                isComplete = relationResult.getIsComplete();
            } else {
                log.warn("第 {} 轮关系生成返回空结果", iteration);
                break;
            }
        }

        return allRelations;
    }

    /**
     * 验证关系对象是否包含完整的必要字段
     */
    private boolean isValidRelation(AiKnowledgeRelation relation) {
        return relation != null &&
                relation.getSourceKnowledgeId() != null &&
                relation.getTargetKnowledgeId() != null &&
                relation.getRelationType() != null &&
                !relation.getRelationType().trim().isEmpty() &&
                relation.getRelationDescription() != null &&
                !relation.getRelationDescription().trim().isEmpty();
    }

    /**
     * 知识点扩展 - 基于原始知识点进行广度优先拓展
     *
     * @param userRequirement         用户核心需求
     * @param originalKnowledgePoints 原始知识点列表
     * @param chunks                  参考资料列表
     * @return 包含新生成的知识图谱和Token使用情况的结果
     */
    public Optional<AiGenerationResult<KnowledgeGraphDto>> expandKnowledgePoints(String userRequirement,
                                                                                 List<AiKnowledgePoint> originalKnowledgePoints,
                                                                                 List<AiChunkContent> chunks) {
        log.info("开始知识点扩展，原始知识点数量: {}", originalKnowledgePoints.size());

        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();
        List<AiKnowledgePoint> expandedPoints = new ArrayList<>();
        boolean isComplete = false;
        int iteration = 0;

        // 将原始知识点转换为KnowledgePointDto用于AI调用
        List<KnowledgePointDto> originalPointDtos = originalKnowledgePoints.stream()
                .map(point -> new KnowledgePointDto(point.getTitle(), point.getDefinition(),
                        point.getExplanation(), point.getFormulaOrCode(), point.getExample()))
                .collect(Collectors.toList());

        while (!isComplete && iteration < MAX_EXPANSION_ITERATIONS) {
            iteration++;
            log.info("知识点扩展第 {} 轮迭代开始", iteration);

            // 将已扩展的知识点转换为KnowledgePointDto
            List<KnowledgePointDto> expandedPointDtos = expandedPoints.stream()
                    .map(point -> new KnowledgePointDto(point.getTitle(), point.getDefinition(),
                            point.getExplanation(), point.getFormulaOrCode(), point.getExample()))
                    .collect(Collectors.toList());

            KnowledgeAiPrompt.PromptPair promptPair = KnowledgeAiPrompt.KnowledgePointExpander.build(
                    userRequirement, MAX_POINTS_PER_CALL, originalPointDtos, expandedPointDtos, chunks);

            // 使用静态示例对象
            StructResult<KnowledgePointExpansionResult> result = structuredOutputTool.generateStructuredOutput(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    EXPANSION_EXAMPLE,
                    KnowledgePointExpansionResult.class
            );

            // 统计Token使用量
            tokenAccumulator.add(result.getTokenUsage());

            if (result.getResult() != null) {
                KnowledgePointExpansionResult expansionResult = result.getResult();
                if (expansionResult.getNewKnowledgePoints() != null && !expansionResult.getNewKnowledgePoints().isEmpty()) {
                    // 将KnowledgePointDto转换为AiKnowledgePoint（添加ID）
                    List<AiKnowledgePoint> newAiPoints = expansionResult.getNewKnowledgePoints().stream()
                            .map(dto -> new AiKnowledgePoint(HutoolSnowflakeIdGenerator.generateLongId(), dto.getTitle(), dto.getDefinition(), dto.getExplanation(), dto.getFormulaOrCode(), dto.getExample()))
                            .toList();
                    expandedPoints.addAll(newAiPoints);
                    log.info("第 {} 轮扩展了 {} 个知识点", iteration, expansionResult.getNewKnowledgePoints().size());
                }
                isComplete = expansionResult.getIsComplete();
            } else {
                log.warn("第 {} 轮知识点扩展返回空结果", iteration);
                break;
            }
        }

        // 如果没有扩展的知识点，返回空结果
        if (expandedPoints.isEmpty()) {
            log.info("知识点扩展完成，没有生成新的知识点");
            return Optional.of(AiGenerationResult.success(new KnowledgeGraphDto(new ArrayList<>(), new ArrayList<>()), tokenAccumulator.getTotal()));
        }

        // 生成新扩展知识点之间的关系
        List<AiKnowledgeRelation> relations = generateRelationsUntilComplete(expandedPoints, tokenAccumulator);
        log.info("知识点扩展的知识关系生成完成，生成了 {} 条关系", relations.size());

        // 返回包含新生成知识点和关系的完整知识图谱
        KnowledgeGraphDto knowledgeGraph = new KnowledgeGraphDto(expandedPoints, relations);
        log.info("知识点扩展完成，扩展了 {} 个新知识点和 {} 条关系", expandedPoints.size(), relations.size());
        return Optional.of(AiGenerationResult.success(knowledgeGraph, tokenAccumulator.getTotal()));
    }
}

