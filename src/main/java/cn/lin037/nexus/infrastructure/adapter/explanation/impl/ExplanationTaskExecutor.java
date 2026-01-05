package cn.lin037.nexus.infrastructure.adapter.explanation.impl;

import cn.lin037.nexus.application.explanation.port.ExplanationDocumentRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationPointRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationSectionRepository;
import cn.lin037.nexus.application.explanation.port.ExplanationSubsectionRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.explanation.ExplanationAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.explanation.constant.ExplanationTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.*;
import cn.lin037.nexus.infrastructure.adapter.explanation.params.AiGenerateExplanationTaskParameters;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StructuredOutputTool;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationDocumentEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationPointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.explanation.ExplanationSubsectionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.enums.ExplanationDocumentStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 优化版讲解文档AI生成任务执行器
 * <p>
 * 主要优化：
 * 1. 移除整体事务，使用细粒度事务管理
 * 2. 使用虚拟线程并行生成知识点
 * 3. 增强容错机制，忽略格式错误
 * 4. 优化去重和结果收集逻辑
 *
 * @author LinSanQi
 */
@Slf4j
@Component
public class ExplanationTaskExecutor implements TaskExecutor<AiGenerateExplanationTaskParameters, TokenUsageAccumulator> {

    private static final String CONTENT_MODEL_NAME = "qwen-max";

    private static final String MODEL_NAME = "qwen-max";
    private static final String USED_FOR = "STRUCTURED_OUTPUT_EXPLANATION";
    private static final String CONTENT_USED_FOR = "STRUCTURED_OUTPUT_EXPLANATION_CONTENT";
    // 并发配置
    private static final int KNOWLEDGE_POINT_BATCH_SIZE = 3; // 每批处理的章节数
    private static final int MAX_CONCURRENT_REQUESTS = 5; // 最大并发请求数
    private static final int REQUEST_TIMEOUT_MINUTE = 5; // 单个请求超时时间
    private static final int DEFAULT_MAX_RETRIES = 2; // 默认最大重试次数
    private static final long BASE_RETRY_DELAY_MS = 10 * 1000L; // 基础重试延迟时间（毫秒）
    private static final long REQUEST_DELAY_MS = 1000L; // 基础请求间隔
    private static final long REQUEST_JITTER_MS = 4000L; // 随机抖动范围
    // 示例对象
    private static final ChapterDto CHAPTER_EXAMPLE = ChapterDto.builder()
            .sectionId(1L)
            .sectionTitle("基础概念")
            .sectionRequirement("理解核心概念和基本原理")
            .pointIdsForReference(List.of(1L, 2L))
            .chunkIdsForReference(List.of(1L, 2L))
            .build();
    private static final SubSectionDto SUBSECTION_EXAMPLE = SubSectionDto.builder()
            .subsectionId(1L)
            .subsectionTitle("定义与特性")
            .subsectionRequirement("掌握基本定义和主要特性")
            .parentSectionId(1L)
            .pointIdsForReference(List.of(1L))
            .chunkIdsForReference(List.of(1L))
            .build();
    private static final ExplanationPointDto EXPLANATION_POINT_EXAMPLE = ExplanationPointDto.builder()
            .pointId(1L)
            .title("核心概念")
            .definition("这是一个重要的核心概念")
            .explanation("详细解释了概念的含义和应用")
            .formulaOrCode("示例代码或公式")
            .example("具体的应用示例")
            .build();
    private final ChatModel contentChatModel;
    private final ChatModel chatModel;
    // 虚拟线程执行器
    private final ExecutorService virtualThreadExecutor;
    private final StructuredOutputTool structuredOutputTool;

    // Repository dependencies
    private final ExplanationDocumentRepository explanationDocumentRepository;
    private final ExplanationSectionRepository explanationSectionRepository;
    private final ExplanationSubsectionRepository explanationSubsectionRepository;
    private final ExplanationPointRepository explanationPointRepository;
    // 并发控制信号量
    private final Semaphore concurrencyLimiter;
    // 引用任务执行器
    private ExplanationTaskExecutor self;

    public ExplanationTaskExecutor(AiCoreService aiCoreService,
                                   StructuredOutputTool structuredOutputTool,
                                   ExplanationDocumentRepository explanationDocumentRepository,
                                   ExplanationSectionRepository explanationSectionRepository,
                                   ExplanationSubsectionRepository explanationSubsectionRepository,
                                   ExplanationPointRepository explanationPointRepository) {
        this.chatModel = aiCoreService.getChatModel(MODEL_NAME, USED_FOR);
        this.contentChatModel = aiCoreService.getChatModel(CONTENT_MODEL_NAME, CONTENT_USED_FOR);
        this.structuredOutputTool = structuredOutputTool;
        this.explanationDocumentRepository = explanationDocumentRepository;
        this.explanationSectionRepository = explanationSectionRepository;
        this.explanationSubsectionRepository = explanationSubsectionRepository;
        this.explanationPointRepository = explanationPointRepository;

        // 创建虚拟线程执行器
        this.virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
        // 创建并发控制信号量
        this.concurrencyLimiter = new Semaphore(MAX_CONCURRENT_REQUESTS);

        log.info("ExplanationTaskExecutorOptimized 初始化完成，最大并发请求数: {}", MAX_CONCURRENT_REQUESTS);
    }

    private static @NotNull List<SubSectionDto> getSubSectionDtos(StructResult<List<SubSectionDto>> result) {
        List<SubSectionDto> subsections = result.getResult();
        if (subsections == null) {
            subsections = new ArrayList<>();
        }
        return subsections;
    }

    @Autowired
    public void setSelf(@Lazy ExplanationTaskExecutor self) {
        this.self = self;
    }

    @Override
    public String getTaskType() {
        return ExplanationTaskConstant.TASK_TYPE_EXPLANATION_AI_GENERATE;
    }

    @Override
    // 移除整体事务注解，改为细粒度事务管理
    public TaskResult<TokenUsageAccumulator> execute(AiGenerateExplanationTaskParameters params, TaskContext context) {
        log.info("开始为文档ID {} 生成讲解内容（优化版）", params.getExplanationDocumentId());

        // 立即更新文档状态为生成中（使用独立事务）
        self.updateDocumentStatusInNewTransaction(params.getExplanationDocumentId(), ExplanationDocumentStatusEnum.AI_GENERATING);

        // 创建一个TokenUsage累加器
        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();

        try {
            // 步骤1: 规划阶段 - 生成章节和小节结构
            log.info("步骤1: 开始规划章节结构");
            List<ChapterDto> chapters = planChapters(params, tokenAccumulator);
            if (chapters.isEmpty()) {
                throw new RuntimeException("章节规划失败，未生成任何章节");
            }
            log.info("生成了 {} 个章节", chapters.size());

            log.info("步骤1: 开始规划小节结构并构建大纲");
            List<ChapterOutlineDto> chapterOutlines = planSubSectionsAndBuildOutline(chapters, params, tokenAccumulator);
            log.info("完成小节规划，总共生成了 {} 个章节，{} 个小节",
                    chapterOutlines.size(),
                    chapterOutlines.stream().mapToInt(ChapterOutlineDto::getSubsectionCount).sum());

            // 为所有章节和小节分配最终的永久ID
            log.info("步骤1: 分配永久ID");
            assignPermanentIdsToOutlines(chapterOutlines);
            log.info("ID分配完成");

            // 步骤2: 知识点生成阶段（异步并行优化）
            log.info("步骤2: 开始并行生成知识点");
            List<ExplanationPointDto> explanationPoints = generateKnowledgePointsConcurrently(params, chapterOutlines, tokenAccumulator, context);
            if (explanationPoints.isEmpty()) {
                throw new RuntimeException("知识点生成失败，未生成任何知识点");
            }
            log.info("生成了 {} 个知识点", explanationPoints.size());

            // 步骤3: TODO: 关系生成阶段 - 调用KnowledgeAiGenerationService，因为此处过度耗时，暂且先不处理

            // 步骤4: 内容生成阶段 - 并行生成内容
            log.info("步骤4: 开始并行生成内容");
            ExplanationGenerationResult generationResult = generateAllContentsSequentially(chapterOutlines, explanationPoints, params, tokenAccumulator, context);
            log.info("完成内容生成，生成了 {} 个章节和 {} 个小节",
                    generationResult.getSectionEntities().size(),
                    generationResult.getSubsectionEntities().size());

            if (context.isCancellationRequested()) {
                return TaskResult.success("任务取消，取消生成内容", tokenAccumulator);
            }

            // 步骤5: 持久化阶段（使用独立事务）
            log.info("步骤5: 开始持久化数据");
            self.persistAllDataInTransaction(params, explanationPoints, generationResult);
            log.info("数据持久化完成");

            // 更新文档状态为完成（使用独立事务）
            self.updateDocumentStatusInNewTransaction(params.getExplanationDocumentId(), ExplanationDocumentStatusEnum.NORMAL);

            return TaskResult.success("讲解文档生成成功", tokenAccumulator);

        } catch (Exception e) {
            log.error("生成讲解文档 {} 失败", params.getExplanationDocumentId(), e);
            // 失败时恢复为草稿状态（使用独立事务）
            self.updateDocumentStatusInNewTransaction(params.getExplanationDocumentId(), ExplanationDocumentStatusEnum.AI_GENERATE_FAILED);

            // 在异常中携带已使用的TokenUsageAccumulator
            if (e instanceof ApplicationException) {
                throw e; // 如果已经是ApplicationException，直接抛出
            } else {
                // 将异常包装为ApplicationException并携带TokenUsageAccumulator
                throw new ApplicationException("讲解文档生成任务执行失败: " + e.getMessage(),
                        tokenAccumulator, TokenUsageAccumulator.class);
            }
        }
    }

    @Override
    public Class<AiGenerateExplanationTaskParameters> getParametersType() {
        return AiGenerateExplanationTaskParameters.class;
    }

    @Override
    public Class<TokenUsageAccumulator> getResultType() {
        return TokenUsageAccumulator.class;
    }

    /**
     /**
     * 并发生成知识点（核心优化方法）
     */
    private List<ExplanationPointDto> generateKnowledgePointsConcurrently(AiGenerateExplanationTaskParameters params,
                                                                          List<ChapterOutlineDto> chapterOutlines,
                                                                          TokenUsageAccumulator tokenAccumulator,
                                                                          TaskContext context) {
        log.info("开始并发生成知识点，总共 {} 个章节，每批处理 {} 个章节，最大并发数: {}",
                chapterOutlines.size(), KNOWLEDGE_POINT_BATCH_SIZE, MAX_CONCURRENT_REQUESTS);

        List<ExplanationPointDto> allGeneratedPoints = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<List<ExplanationPointDto>>> futures = new ArrayList<>();

        // 将章节分批处理
        for (int i = 0; i < chapterOutlines.size(); i += KNOWLEDGE_POINT_BATCH_SIZE) {
            int endIndex = Math.min(i + KNOWLEDGE_POINT_BATCH_SIZE, chapterOutlines.size());
            List<ChapterOutlineDto> currentBatch = chapterOutlines.subList(i, endIndex);
            final int batchIndex = i / KNOWLEDGE_POINT_BATCH_SIZE + 1;

            // 创建异步任务，使用并发控制
            CompletableFuture<List<ExplanationPointDto>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // 检查任务是否被取消
                    if (context.isCancellationRequested()) {
                        log.info("任务被取消，停止批次 {} 的知识点生成", batchIndex);
                        return new ArrayList<>();
                    }

                    // 获取并发许可
                    concurrencyLimiter.acquire();
                    log.info("批次 {} 获得并发许可，开始处理 {} 个章节", batchIndex, currentBatch.size());

                    try {
                        List<ExplanationPointDto> batchPoints = generateKnowledgePointsForBatchWithRetry(
                                params, currentBatch, allGeneratedPoints, tokenAccumulator, batchIndex);

                        log.info("第 {} 批完成，生成 {} 个知识点", batchIndex, batchPoints.size());
                        return batchPoints;
                    } finally {
                        // 释放并发许可
                        concurrencyLimiter.release();
                        log.debug("批次 {} 释放并发许可", batchIndex);
                        // 增加请求抖动，避免触发API速率限制
                        try {
                            Thread.sleep(REQUEST_DELAY_MS + ThreadLocalRandom.current().nextLong(REQUEST_JITTER_MS));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("批次 {} 被中断", batchIndex);
                    return new ArrayList<>();
                } catch (Exception e) {
                    log.error("第 {} 批知识点生成失败", batchIndex, e);
                    // 返回空列表而不是抛出异常，实现容错
                    return new ArrayList<>();
                }
            }, virtualThreadExecutor);

            futures.add(future);
        }

        // 等待所有批次完成，并收集结果
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.get(5, TimeUnit.MINUTES); // 设置总体超时时间

            // 收集所有结果
            for (CompletableFuture<List<ExplanationPointDto>> future : futures) {
                List<ExplanationPointDto> batchResult = future.get();
                allGeneratedPoints.addAll(batchResult);
            }

        } catch (TimeoutException e) {
            log.error("知识点并发生成超时", e);
            throw new RuntimeException("知识点生成超时", e);
        } catch (Exception e) {
            log.error("知识点并发生成失败", e);
            throw new RuntimeException("知识点生成失败", e);
        }
        return allGeneratedPoints;
    }

    /**
     * 带重试机制的批次知识点生成
     */
    private List<ExplanationPointDto> generateKnowledgePointsForBatchWithRetry(AiGenerateExplanationTaskParameters params,
                                                                               List<ChapterOutlineDto> chapterBatch,
                                                                               List<ExplanationPointDto> existingPoints,
                                                                               TokenUsageAccumulator tokenAccumulator,
                                                                               int batchIndex) {
        int retryCount = 0;
        List<ExplanationPointDto> res = new ArrayList<>();

        while (retryCount < DEFAULT_MAX_RETRIES) {
            try {
                return generateKnowledgePointsForBatchOptimized(params, chapterBatch, existingPoints, tokenAccumulator);
            } catch (Exception e) {
                retryCount++;
                log.warn("第 {} 批知识点生成失败，重试次数: {}/{}", batchIndex, retryCount + 1, DEFAULT_MAX_RETRIES, e);

                if (retryCount == DEFAULT_MAX_RETRIES) {
                    log.error("第 {} 批知识点生成达到最大重试次数，跳过该批次", batchIndex);
                    return res;
                }

                // 等待一段时间后重试
                try {
                    // 指数退避策略：基础延迟 * 2^(重试次数-1)
                    long delayMs = BASE_RETRY_DELAY_MS * (1L << (retryCount - 1));
                    // 最大延迟不超过30秒
                    delayMs = Math.min(delayMs, 30000L);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("第 {} 批重试被中断", batchIndex);
                    return res;
                }
            }
        }

        return res;
    }

    /**
     * 优化的批次知识点生成（增强容错）
     */
    private List<ExplanationPointDto> generateKnowledgePointsForBatchOptimized(AiGenerateExplanationTaskParameters params,
                                                                               List<ChapterOutlineDto> chapterBatch,
                                                                               List<ExplanationPointDto> existingPoints,
                                                                               TokenUsageAccumulator tokenAccumulator) {
        // 构建当前批次的大纲字符串
        StringBuilder outlineBuilder = new StringBuilder();
        for (ChapterOutlineDto chapterOutline : chapterBatch) {
            outlineBuilder.append(chapterOutline.toOutlineString());
        }
        String batchOutlineString = outlineBuilder.toString();

        // 使用增强的提示词构建方法
        ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.KnowledgePointGenerator.buildWithStringOutlineAndExistingPoints(
                params.getUserPrompt(),
                batchOutlineString,
                params.getChunks(),
                params.getKnowledgePoints(),
                existingPoints
        );

        try {
            StructResult<List<ExplanationPointDto>> result = structuredOutputTool.generateStructuredOutputList(
                    chatModel,
                    promptPair.systemPrompt(),
                    promptPair.userPrompt(),
                    EXPLANATION_POINT_EXAMPLE,
                    ExplanationPointDto.class
            );

            // 更新token使用量（TokenUsageAccumulator已线程安全）
            tokenAccumulator.add(result.getTokenUsage());

            if (result.getResult() == null || result.getResult().isEmpty()) {
                log.warn("当前批次知识点生成返回空结果");
                return new ArrayList<>();
            }

            // 处理结果，容错处理格式错误
            List<ExplanationPointDto> validPoints = new ArrayList<>();
            for (ExplanationPointDto point : result.getResult()) {
                try {
                    // 验证和修复知识点数据
                    if (isValidKnowledgePoint(point)) {
                        // 修复：始终生成新的ID，以避免AI返回示例ID（如1）导致的主键冲突
                        point.setPointId(HutoolSnowflakeIdGenerator.generateLongId());
                        validPoints.add(point);
                    } else {
                        log.warn("跳过无效知识点: {}", point);
                    }
                } catch (Exception e) {
                    log.warn("处理知识点时出错，跳过该知识点: {}", point, e);
                    // 继续处理下一个知识点，实现容错
                }
            }

            log.debug("批次处理完成：原始 {} 个，有效 {} 个", result.getResult().size(), validPoints.size());
            return validPoints;

        } catch (Exception e) {
            log.error("批次知识点生成请求失败", e);
            // 重新抛出异常，由重试机制处理
            throw e;
        }
    }

    /**
     * 验证知识点是否有效
     */
    private boolean isValidKnowledgePoint(ExplanationPointDto point) {
        return point != null &&
                point.getTitle() != null &&
                !point.getTitle().trim().isEmpty() &&
                point.getDefinition() != null &&
                !point.getDefinition().trim().isEmpty();
    }

    /**
     * 顺序生成所有章节内容，但在每个章节内部并行生成小节内容。
     * 这种方式避免了并发嵌套导致的请求爆炸和日志混乱问题。
     */
    private ExplanationGenerationResult generateAllContentsSequentially(List<ChapterOutlineDto> chapterOutlines,
                                                                        List<ExplanationPointDto> allPoints,
                                                                        AiGenerateExplanationTaskParameters params,
                                                                        TokenUsageAccumulator tokenAccumulator,
                                                                        TaskContext context) {
        LocalDateTime now = LocalDateTime.now();
        List<ExplanationSectionEntity> sectionEntities = Collections.synchronizedList(new ArrayList<>());
        List<ExplanationSubsectionEntity> subsectionEntities = Collections.synchronizedList(new ArrayList<>());
        List<Long> sectionOrderList = new ArrayList<>();

        log.info("开始顺序生成内容，总共 {} 个章节，每个章节内部将并行生成小节", chapterOutlines.size());

        // 按顺序处理每个章节
        for (ChapterOutlineDto chapterOutline : chapterOutlines) {
            if (context.isCancellationRequested()) {
                log.warn("任务被取消，停止内容生成。");
                break;
            }

            log.info("--- 开始为章节 '{}' 生成内容 ---", chapterOutline.getSectionTitle());
            try {
                processChapterContent(chapterOutline, allPoints, params, tokenAccumulator,
                        sectionEntities, subsectionEntities, now);
                sectionOrderList.add(chapterOutline.getSectionId());
                log.info("--- 章节 '{}' 内容生成完成 ---", chapterOutline.getSectionTitle());
            } catch (Exception e) {
                log.error("章节 '{}' 内容生成失败，跳过该章节。", chapterOutline.getSectionTitle(), e);
                // 容错：即使单个章节失败，也继续处理下一个章节
            }
        }

        return ExplanationGenerationResult.builder()
                .sectionEntities(sectionEntities)
                .subsectionEntities(subsectionEntities)
                .sectionOrderList(sectionOrderList)
                .build();
    }

    /**
     * 处理单个章节的内容生成
     */
    private void processChapterContent(ChapterOutlineDto chapterOutline,
                                       List<ExplanationPointDto> allPoints,
                                       AiGenerateExplanationTaskParameters params,
                                       TokenUsageAccumulator tokenAccumulator,
                                       List<ExplanationSectionEntity> sectionEntities,
                                       List<ExplanationSubsectionEntity> subsectionEntities,
                                       LocalDateTime now) {

        // 为小节分配ID
        List<SubSectionDto> subsectionsForContent = new ArrayList<>();
        if (chapterOutline.getSubsections() != null) {
            for (SubSectionOutlineDto subsectionOutline : chapterOutline.getSubsections()) {
                SubSectionDto subsectionDto = SubSectionDto.builder()
                        .subsectionId(HutoolSnowflakeIdGenerator.generateLongId())
                        .subsectionTitle(subsectionOutline.getSubsectionTitle())
                        .subsectionRequirement(subsectionOutline.getSubsectionRequirement())
                        .parentSectionId(chapterOutline.getSectionId())
                        .build();
                subsectionsForContent.add(subsectionDto);
            }
        }

        // 并发生成小节内容（使用信号量控制并发）
        List<CompletableFuture<String>> subsectionFutures = new ArrayList<>();
        for (SubSectionDto subsection : subsectionsForContent) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // 小节内容生成也需要控制并发
                    concurrencyLimiter.acquire();
                    log.debug("小节 {} 获得并发许可，开始生成内容", subsection.getSubsectionTitle());
                    return generateSubsectionContentWithRetry(subsection, allPoints, params, tokenAccumulator);
                } catch (Exception e) {
                    log.error("小节 {} 内容生成异常", subsection.getSubsectionTitle(), e);
                    return "内容生成失败，请稍后重试。";
                } finally {
                    concurrencyLimiter.release();
                    log.debug("小节 {} 释放并发许可", subsection.getSubsectionTitle());
                    // 增加请求抖动
                    try {
                        Thread.sleep(REQUEST_DELAY_MS + ThreadLocalRandom.current().nextLong(REQUEST_JITTER_MS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, virtualThreadExecutor);
            subsectionFutures.add(future);
        }

        // 等待所有小节内容生成完成
        List<String> subsectionContents = new ArrayList<>();
        try {
            for (int i = 0; i < subsectionFutures.size(); i++) {
                String content = subsectionFutures.get(i).get(REQUEST_TIMEOUT_MINUTE, TimeUnit.MINUTES);
                subsectionContents.add(content);

                // 创建小节实体
                ExplanationSubsectionEntity subsectionEntity = ExplanationSubsectionEntity.builder()
                        .essId(subsectionsForContent.get(i).getSubsectionId())
                        .essSectionId(chapterOutline.getSectionId())
                        .essTitle(subsectionsForContent.get(i).getSubsectionTitle())
                        .essContent(content)
                        .essExplanationDocumentId(params.getExplanationDocumentId())
                        .essCreatedByUserId(params.getUserId())
                        .essOrder(i + 1)
                        .essCreatedAt(now)
                        .essUpdatedAt(now)
                        .build();

                subsectionEntities.add(subsectionEntity);
            }
        } catch (Exception e) {
            log.error("小节内容生成失败", e);
            throw new RuntimeException("小节内容生成失败", e);
        }

        // 生成章节概述
        String sectionContent = generateSectionContentBasedOnSubsections(
                chapterOutline, subsectionsForContent, subsectionContents, allPoints, tokenAccumulator);

        // 创建章节实体
        ExplanationSectionEntity sectionEntity = ExplanationSectionEntity.builder()
                .esId(chapterOutline.getSectionId())
                .esTitle(chapterOutline.getSectionTitle())
                .esContent(sectionContent)
                .esExplanationDocumentId(params.getExplanationDocumentId())
                .esCreatedByUserId(params.getUserId())
                .esSubsectionOrder(subsectionsForContent.stream().map(SubSectionDto::getSubsectionId).toList())
                .esCreatedAt(now)
                .esUpdatedAt(now)
                .build();

        sectionEntities.add(sectionEntity);
    }

    /**
     * 带重试的小节内容生成
     */
    private String generateSubsectionContentWithRetry(SubSectionDto subsection,
                                                      List<ExplanationPointDto> allPoints,
                                                      AiGenerateExplanationTaskParameters params,
                                                      TokenUsageAccumulator tokenAccumulator) {
        int retryCount = 0;

        while (retryCount < DEFAULT_MAX_RETRIES) {
            try {
                ExplanationAiPrompt.PromptPair subsectionPromptPair = ExplanationAiPrompt.SubSectionContentGenerator.build(
                        subsection, allPoints, params.getChunks());

                StructResult<String> subsectionContentResult = structuredOutputTool.generateStringOutput(
                        contentChatModel,
                        subsectionPromptPair.systemPrompt(),
                        subsectionPromptPair.userPrompt()
                );

                // 更新token统计（TokenUsageAccumulator已线程安全）
                tokenAccumulator.add(subsectionContentResult.getTokenUsage());

                return subsectionContentResult.getResult();

            } catch (Exception e) {
                retryCount++;
                log.warn("小节 {} 内容生成失败，尝试次数: {}/{}", subsection.getSubsectionTitle(), retryCount, DEFAULT_MAX_RETRIES, e);

                if (retryCount == DEFAULT_MAX_RETRIES) {
                    log.error("小节 {} 内容生成达到最大重试次数，返回默认内容", subsection.getSubsectionTitle());
                    // 返回默认内容，实现容错
                    return "内容生成失败，请稍后重试。";
                }

                try {
                    // 指数退避策略：基础延迟 * 2^(重试次数-1)
                    long delayMs = BASE_RETRY_DELAY_MS * (1L << (retryCount - 1));
                    // 最大延迟不超过30秒
                    delayMs = Math.min(delayMs, 30000L);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("小节 {} 重试被中断", subsection.getSubsectionTitle());
                    return "内容生成被中断。";
                }
            }
        }

        return "内容生成失败。";
    }

    /**
     * 为大纲中的所有章节和小节分配永久ID
     */
    private void assignPermanentIdsToOutlines(List<ChapterOutlineDto> chapterOutlines) {
        for (ChapterOutlineDto chapterOutline : chapterOutlines) {
            // 为章节分配永久ID
            chapterOutline.setSectionId(HutoolSnowflakeIdGenerator.generateLongId());

            // 为该章节下的小节分配永久ID
            if (chapterOutline.getSubsections() != null) {
                log.debug("章节 '{}' 包含 {} 个小节",
                        chapterOutline.getSectionTitle(),
                        chapterOutline.getSubsectionCount());
            }
        }
    }

    /**
     * 规划小节并构建完整的章节大纲
     */
    private List<ChapterOutlineDto> planSubSectionsAndBuildOutline(List<ChapterDto> chapters,
                                                                   AiGenerateExplanationTaskParameters params,
                                                                   TokenUsageAccumulator tokenAccumulator) {
        List<ChapterOutlineDto> chapterOutlines = new ArrayList<>();

        for (ChapterDto chapter : chapters) {
            int retryCount = 0;

            while (retryCount < DEFAULT_MAX_RETRIES) {
                try {
                    ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.SubSectionPlanner.build(
                            chapter,
                            params.getChunks(),
                            params.getKnowledgePoints()
                    );

                    StructResult<List<SubSectionDto>> result = structuredOutputTool.generateStructuredOutputList(
                            chatModel,
                            promptPair.systemPrompt(),
                            promptPair.userPrompt(),
                            SUBSECTION_EXAMPLE,
                            SubSectionDto.class
                    );

                    // 更新token统计（TokenUsageAccumulator已线程安全）
                    tokenAccumulator.add(result.getTokenUsage());

                    List<SubSectionDto> subsections = getSubSectionDtos(result);

                    // 转换为SubSectionOutlineDto
                    List<SubSectionOutlineDto> subsectionOutlines = subsections.stream()
                            .map(SubSectionOutlineDto::fromSubSectionDto)
                            .collect(Collectors.toList());

                    // 构建ChapterOutlineDto
                    ChapterOutlineDto chapterOutline = ChapterOutlineDto.builder()
                            .sectionTitle(chapter.getSectionTitle())
                            .sectionRequirement(chapter.getSectionRequirement())
                            .pointIdsForReference(chapter.getPointIdsForReference())
                            .chunkIdsForReference(chapter.getChunkIdsForReference())
                            .subsections(subsectionOutlines)
                            .build();

                    chapterOutlines.add(chapterOutline);
                    break; // 成功后跳出重试循环

                } catch (Exception e) {
                    retryCount++;
                    log.warn("小节规划失败，章节: {}, 尝试次数: {}/{}", chapter.getSectionTitle(), retryCount, DEFAULT_MAX_RETRIES, e);

                    if (retryCount == DEFAULT_MAX_RETRIES) {
                        log.error("小节规划达到最大重试次数，章节: {}, 跳过该章节", chapter.getSectionTitle());
                        // 创建一个空的章节大纲
                        ChapterOutlineDto emptyChapterOutline = ChapterOutlineDto.builder()
                                .sectionTitle(chapter.getSectionTitle())
                                .sectionRequirement(chapter.getSectionRequirement())
                                .pointIdsForReference(chapter.getPointIdsForReference())
                                .chunkIdsForReference(chapter.getChunkIdsForReference())
                                .subsections(new ArrayList<>())
                                .build();
                        chapterOutlines.add(emptyChapterOutline);
                        break;
                    }

                    try {
                        // 指数退避策略：基础延迟 * 2^(重试次数-1)
                        long delayMs = BASE_RETRY_DELAY_MS * (1L << (retryCount - 1));
                        // 最大延迟不超过30秒
                        delayMs = Math.min(delayMs, 30000L);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("小节规划重试被中断，章节: {}", chapter.getSectionTitle());
                        // 创建一个空的章节大纲
                        ChapterOutlineDto emptyChapterOutline = ChapterOutlineDto.builder()
                                .sectionTitle(chapter.getSectionTitle())
                                .sectionRequirement(chapter.getSectionRequirement())
                                .pointIdsForReference(chapter.getPointIdsForReference())
                                .chunkIdsForReference(chapter.getChunkIdsForReference())
                                .subsections(new ArrayList<>())
                                .build();
                        chapterOutlines.add(emptyChapterOutline);
                        break;
                    }
                }
            }
        }

        return chapterOutlines;
    }

    /**
     * 基于小节实际内容生成章节概述
     */
    private String generateSectionContentBasedOnSubsections(ChapterOutlineDto chapterOutline,
                                                            List<SubSectionDto> subsections,
                                                            List<String> subsectionContents,
                                                            List<ExplanationPointDto> allPoints,
                                                            TokenUsageAccumulator tokenAccumulator) {
        int retryCount = 0;

        while (retryCount < DEFAULT_MAX_RETRIES) {
            try {
                // 构建基于小节实际内容的提示词
                StringBuilder subsectionSummary = new StringBuilder();
                subsectionSummary.append("该章节包含的小节及其实际内容：\n\n");

                for (int i = 0; i < subsections.size(); i++) {
                    subsectionSummary.append("小节标题：").append(subsections.get(i).getSubsectionTitle()).append("\n");
                    subsectionSummary.append("小节内容：\n").append(subsectionContents.get(i)).append("\n\n");
                }

                // 使用现有的章节内容生成器
                ExplanationAiPrompt.PromptPair sectionPromptPair = ExplanationAiPrompt.SectionContentBasedOnSubsections.build(
                        chapterOutline,
                        subsectionSummary.toString(),
                        allPoints
                );

                StructResult<String> sectionContentResult = structuredOutputTool.generateStringOutput(
                        contentChatModel,
                        sectionPromptPair.systemPrompt(),
                        sectionPromptPair.userPrompt()
                );

                // 更新token统计（TokenUsageAccumulator已线程安全）
                tokenAccumulator.add(sectionContentResult.getTokenUsage());
                return sectionContentResult.getResult();

            } catch (Exception e) {
                retryCount++;
                log.warn("章节 {} 内容生成失败，尝试次数: {}/{}", chapterOutline.getSectionTitle(), retryCount, DEFAULT_MAX_RETRIES, e);

                if (retryCount == DEFAULT_MAX_RETRIES) {
                    log.error("章节 {} 内容生成达到最大重试次数，返回默认内容", chapterOutline.getSectionTitle());
                    return "章节内容生成失败，请稍后重试。"; // 返回默认内容，实现容错
                }

                try {
                    // 指数退避策略：基础延迟 * 2^(重试次数-1)
                    long delayMs = BASE_RETRY_DELAY_MS * (1L << (retryCount - 1));
                    // 最大延迟不超过30秒
                    delayMs = Math.min(delayMs, 30000L);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("章节 {} 重试被中断", chapterOutline.getSectionTitle());
                    return "章节内容生成被中断。";
                }
            }
        }

        return "章节内容生成失败。";
    }

    /**
     * 步骤1: 规划章节
     */
    private List<ChapterDto> planChapters(AiGenerateExplanationTaskParameters params, TokenUsageAccumulator tokenAccumulator) {
        int retryCount = 0;

        while (retryCount < DEFAULT_MAX_RETRIES) {
            try {
                ExplanationAiPrompt.PromptPair promptPair = ExplanationAiPrompt.ChapterPlanner.build(
                        params.getUserPrompt(),
                        params.getChunks(),
                        params.getKnowledgePoints()
                );

                StructResult<List<ChapterDto>> result = structuredOutputTool.generateStructuredOutputList(
                        chatModel,
                        promptPair.systemPrompt(),
                        promptPair.userPrompt(),
                        CHAPTER_EXAMPLE,
                        ChapterDto.class
                );

                // 更新token统计（TokenUsageAccumulator已线程安全）
                tokenAccumulator.add(result.getTokenUsage());

                if (result.getResult() == null || result.getResult().isEmpty()) {
                    log.warn("章节规划返回空结果");
                    return new ArrayList<>();
                }

                return result.getResult();

            } catch (Exception e) {
                retryCount++;
                log.warn("章节规划失败，尝试次数: {}/{}", retryCount, DEFAULT_MAX_RETRIES, e);

                if (retryCount == DEFAULT_MAX_RETRIES) {
                    log.error("章节规划达到最大重试次数，返回空列表");
                    return new ArrayList<>();
                }

                try {
                    // 指数退避策略：基础延迟 * 2^(重试次数-1)
                    long delayMs = BASE_RETRY_DELAY_MS * (1L << (retryCount - 1));
                    // 最大延迟不超过30秒
                    delayMs = Math.min(delayMs, 30000L);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("章节规划重试被中断");
                    return new ArrayList<>();
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * 步骤5: 在独立事务中持久化所有数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void persistAllDataInTransaction(AiGenerateExplanationTaskParameters params,
                                            List<ExplanationPointDto> explanationPoints,
                                            ExplanationGenerationResult generationResult) {

        LocalDateTime now = LocalDateTime.now();

        // 1. 处理知识点 - 生成永久ID并建立映射
        List<ExplanationPointEntity> pointsToSave = new ArrayList<>();

        for (ExplanationPointDto pointDto : explanationPoints) {

            ExplanationPointEntity entity = ExplanationPointEntity.builder()
                    .epId(pointDto.getPointId())
                    .epExplanationDocumentId(params.getExplanationDocumentId())
                    .epCreatedByUserId(params.getUserId())
                    .epTitle(pointDto.getTitle())
                    .epDefinition(pointDto.getDefinition())
                    .epExplanation(pointDto.getExplanation())
                    .epFormulaOrCode(pointDto.getFormulaOrCode())
                    .epExample(pointDto.getExample())
                    .epStyleConfig(null)
                    .epCreatedAt(now)
                    .epUpdatedAt(now)
                    .build();

            pointsToSave.add(entity);
        }

        // 2. 直接使用生成结果中的实体列表
        List<ExplanationSectionEntity> sectionsToSave = generationResult.getSectionEntities();
        List<ExplanationSubsectionEntity> subsectionsToSave = generationResult.getSubsectionEntities();
        List<Long> sectionOrderList = generationResult.getSectionOrderList();

        // 4. 批量保存
        log.info("开始批量保存数据：{} 个知识点，{} 个章节，{} 个小节",
                pointsToSave.size(), sectionsToSave.size(), subsectionsToSave.size());

        explanationPointRepository.saveBatch(pointsToSave);
        explanationSectionRepository.saveBatch(sectionsToSave);
        explanationSubsectionRepository.saveBatch(subsectionsToSave);

        // 5. 更新文档的章节顺序
        Optional<ExplanationDocumentEntity> documentOpt = explanationDocumentRepository.findById(params.getExplanationDocumentId(),
                List.of(ExplanationDocumentEntity::getEdId)
        );
        if (documentOpt.isPresent()) {
            ExplanationDocumentEntity document = documentOpt.get();
            document.setEdSectionOrder(sectionOrderList);
            document.setEdUpdatedAt(now);
            explanationDocumentRepository.updateById(document);
            log.info("更新文档章节顺序完成，共 {} 个章节", sectionOrderList.size());
        }
    }

    /**
     * 在新事务中更新文档状态
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void updateDocumentStatusInNewTransaction(Long documentId, ExplanationDocumentStatusEnum status) {
        try {
            Optional<ExplanationDocumentEntity> documentOpt = explanationDocumentRepository.findById(documentId);
            if (documentOpt.isPresent()) {
                ExplanationDocumentEntity document = documentOpt.get();
                document.setEdStatus(status.getCode());
                document.setEdUpdatedAt(LocalDateTime.now());
                explanationDocumentRepository.updateById(document);
                log.info("文档 {} 状态更新为: {}", documentId, status.getDescription());
            } else {
                log.warn("文档 {} 不存在，无法更新状态", documentId);
            }
        } catch (Exception e) {
            log.error("更新文档 {} 状态失败", documentId, e);
            throw e;
        }
    }

    /**
     * 内容生成结果类，包含已填充内容的实体列表
     */
    @Data
    @Builder
    public static class ExplanationGenerationResult {
        private List<ExplanationSectionEntity> sectionEntities;
        private List<ExplanationSubsectionEntity> subsectionEntities;
        private List<Long> sectionOrderList;
    }
}