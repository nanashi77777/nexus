package cn.lin037.nexus.infrastructure.adapter.resource.impl;

import cn.lin037.nexus.application.resource.enums.ResourceErrorCodeEnum;
import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceAiPrompt;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.ContentResultDto;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.ModuleDto;
import cn.lin037.nexus.infrastructure.adapter.resource.dto.PlanEntryDto;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceAiGenerateTaskParameters;
import cn.lin037.nexus.infrastructure.common.ai.langchain4j.CustomTokenCountEstimator;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StructuredOutputTool;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ResourceChunkAiGenerateTaskExecutor implements TaskExecutor<ResourceAiGenerateTaskParameters, TokenUsageAccumulator> {

    private static final String MODEL_NAME = "qwen-max";
    private static final String USED_FOR = "STRUCTURED_OUTPUT_SEARCH";
    private static final int CONTENT_GENERATION_BATCH_SIZE = 12;

    private final ChatModel chatModel;
    private final ResourceChunkRepository resourceChunkRepository;
    private final StructuredOutputTool structuredOutputTool;
    private final CustomTokenCountEstimator tokenCountEstimator;

    public ResourceChunkAiGenerateTaskExecutor(ResourceChunkRepository resourceChunkRepository,
                                               AiCoreService aiCoreService,
                                               StructuredOutputTool structuredOutputTool,
                                               CustomTokenCountEstimator tokenCountEstimator) {
        this.resourceChunkRepository = resourceChunkRepository;
        this.chatModel = aiCoreService.getChatModel(MODEL_NAME, USED_FOR);
        this.structuredOutputTool = structuredOutputTool;
        this.tokenCountEstimator = tokenCountEstimator;
    }

    @Override
    public String getTaskType() {
        return ResourceTaskConstant.TASK_TYPE_RESOURCE_AI_GENERATE;
    }

    @Override
    public Class<TokenUsageAccumulator> getResultType() {
        return TokenUsageAccumulator.class;
    }

    @Override
    public Class<ResourceAiGenerateTaskParameters> getParametersType() {
        return ResourceAiGenerateTaskParameters.class;
    }

    @Override
    public TaskResult<TokenUsageAccumulator> execute(ResourceAiGenerateTaskParameters parameters, TaskContext context) throws Exception {
        log.info("开始执行AI资源生成任务，资源ID: {}", parameters.getResourceId());
        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();

        try {
            List<ModuleDto> modules = generateBlueprint(parameters.getRsPrompt(), tokenAccumulator);
            Map<ModuleDto, List<PlanEntryDto>> executionPlan = detailModules(parameters.getRsPrompt(), modules, tokenAccumulator);
            executePlan(parameters, executionPlan, tokenAccumulator);

            log.info("AI资源生成任务完成，资源ID: {}, 总Token使用量: {}", parameters.getResourceId(), tokenAccumulator.getTotal());
            return TaskResult.success("AI content generation completed successfully.", tokenAccumulator);
        } catch (Exception e) {
            log.error("AI资源生成任务执行失败，资源ID: {}", parameters.getResourceId(), e);
            // 在异常中携带已使用的TokenUsageAccumulator
            if (e instanceof ApplicationException) {
                throw e; // 如果已经是ApplicationException，直接抛出
            } else {
                // 将异常包装为ApplicationException并携带TokenUsageAccumulator
                throw new ApplicationException(ResourceErrorCodeEnum.AI_GENERATE_FAILED,
                        "AI资源生成任务执行失败: " + e.getMessage(),
                        tokenAccumulator, TokenUsageAccumulator.class);
            }
        }
    }

    private List<ModuleDto> generateBlueprint(String userRequest, TokenUsageAccumulator tokenAccumulator) {
        log.info("开始阶段一：设计蓝图");
        ResourceAiPrompt.PromptPair promptPair = ResourceAiPrompt.Blueprint.build(userRequest);
        try {
            StructResult<List<ModuleDto>> listStructResult = structuredOutputTool.generateStructuredOutputList(
                    chatModel, promptPair.systemPrompt(), promptPair.userPrompt(),
                    ResourceAiPrompt.Blueprint.EXAMPLE_MODULE, ModuleDto.class
            );
            tokenAccumulator.add(listStructResult.getTokenUsage());
            List<ModuleDto> modules = listStructResult.getResult();
            if (modules == null || modules.isEmpty()) {
                throw new ApplicationException(ResourceErrorCodeEnum.AI_GENERATE_FAILED,
                        "用户输入的内容无法生成有效的学习模块。",
                        tokenAccumulator, TokenUsageAccumulator.class);
            }
            log.info("阶段一完成，生成了 {} 个模块", modules.size());
            return modules;
        } catch (Exception e) {
            log.error("阶段一执行失败", e);
            if (e instanceof ApplicationException) {
                throw e;
            }
            throw new ApplicationException(ResourceErrorCodeEnum.AI_GENERATE_FAILED,
                    "AI生成蓝图失败，请检查主题或联系管理员。",
                    tokenAccumulator, TokenUsageAccumulator.class);
        }
    }

    private Map<ModuleDto, List<PlanEntryDto>> detailModules(String userRequest, List<ModuleDto> modules, TokenUsageAccumulator tokenAccumulator) {
        log.info("开始阶段二：细化模块，共 {} 个模块需要细化", modules.size());
        Map<ModuleDto, List<PlanEntryDto>> fullExecutionPlan = new LinkedHashMap<>();
        String allModuleList = modules.stream().map(ModuleDto::getModuleTitle).collect(Collectors.joining("\n- ", "- ", ""));

        for (ModuleDto module : modules) {
            log.debug("正在细化模块: {}", module.getModuleTitle());
            ResourceAiPrompt.PromptPair promptPair = ResourceAiPrompt.Detailing.build(userRequest, allModuleList, module);
            try {
                StructResult<List<PlanEntryDto>> result = structuredOutputTool.generateStructuredOutputList(
                        chatModel, promptPair.systemPrompt(), promptPair.userPrompt(),
                        ResourceAiPrompt.Detailing.EXAMPLE_PLAN_ENTRY, PlanEntryDto.class
                );
                tokenAccumulator.add(result.getTokenUsage());
                List<PlanEntryDto> planEntries = result.getResult();
                if (planEntries != null && !planEntries.isEmpty()) {
                    fullExecutionPlan.put(module, planEntries);
                    log.debug("模块 {} 细化完成，生成了 {} 个计划条目", module.getModuleTitle(), planEntries.size());
                } else {
                    log.warn("模块 {} 细化结果为空", module.getModuleTitle());
                }
            } catch (Exception e) {
                log.error("模块 {} 细化失败", module.getModuleTitle(), e);
                if (e instanceof ApplicationException) {
                    throw e;
                }
                throw new ApplicationException(ResourceErrorCodeEnum.AI_GENERATE_FAILED,
                        "AI细化模块失败，请检查主题或联系管理员。",
                        tokenAccumulator, TokenUsageAccumulator.class);
            }
        }
        log.info("阶段二完成，总共生成了 {} 个执行计划条目", fullExecutionPlan.values().stream().mapToLong(List::size).sum());
        return fullExecutionPlan;
    }

    /**
     * 阶段三：执行计划 (Plan Execution)
     * 使用均衡批处理策略生成内容。
     */
    private void executePlan(ResourceAiGenerateTaskParameters parameters, Map<ModuleDto, List<PlanEntryDto>> executionPlan, TokenUsageAccumulator tokenAccumulator) {
        log.info("开始阶段三：执行计划，共 {} 个模块需要执行", executionPlan.size());
        String allPlanList = executionPlan.values().stream().flatMap(List::stream)
                .map(PlanEntryDto::getDescription).collect(Collectors.joining("\n- ", "- ", ""));

        int pageIndex = 0;
        for (Map.Entry<ModuleDto, List<PlanEntryDto>> moduleEntry : executionPlan.entrySet()) {
            List<PlanEntryDto> planEntries = moduleEntry.getValue();

            // 使用均衡批处理算法
            List<List<PlanEntryDto>> batches = createBalancedBatches(planEntries);
            int chunkIndexOffset = 0;

            for (List<PlanEntryDto> planBatch : batches) {
                log.debug("正在处理模块 {} (Page {}) 的批次，包含 {} 个计划条目", pageIndex, pageIndex, planBatch.size());

                ResourceAiPrompt.PromptPair promptPair = ResourceAiPrompt.Synthesis.build(parameters.getRsPrompt(), allPlanList, planBatch);
                try {
                    StructResult<List<ContentResultDto>> result = structuredOutputTool.generateStructuredOutputList(
                            chatModel, promptPair.systemPrompt(), promptPair.userPrompt(),
                            ResourceAiPrompt.Synthesis.EXAMPLE_CONTENT_RESULT, ContentResultDto.class
                    );
                    tokenAccumulator.add(result.getTokenUsage());
                    List<ContentResultDto> contentResults = result.getResult();
                    if (contentResults != null && !contentResults.isEmpty()) {
                        saveContentResults(parameters, pageIndex, chunkIndexOffset, planBatch, contentResults);
                        chunkIndexOffset += contentResults.size();
                        log.debug("批次内容生成并保存完成");
                    } else {
                        log.warn("批次内容生成结果为空");
                    }
                } catch (Exception e) {
                    log.error("批次内容生成失败", e);
                    if (e instanceof ApplicationException) {
                        throw e;
                    }
                    throw new ApplicationException(ResourceErrorCodeEnum.AI_GENERATE_FAILED,
                            "AI生成内容失败，请检查主题或联系管理员。",
                            tokenAccumulator, TokenUsageAccumulator.class);
                }
            }
            pageIndex++;
        }
        log.info("阶段三完成，所有内容已生成并保存");
    }

    /**
     * 创建均衡的批处理列表，使每个批次的大小尽可能接近，但不超过CONTENT_GENERATION_BATCH_SIZE。
     * 并确保每批至少包含2个条目，以避免单个条目的批次。
     *
     * @param planEntries 原始计划条目列表
     * @return 均衡分配后的批处理列表
     */
    private List<List<PlanEntryDto>> createBalancedBatches(List<PlanEntryDto> planEntries) {
        List<List<PlanEntryDto>> batches = new ArrayList<>();
        int totalSize = planEntries.size();

        if (totalSize == 0) {
            return batches;
        }

        // 设定最小批次大小为2，避免单个条目批次
        int minBatchSize = 2;
        // 初始批次大小不超过CONTENT_GENERATION_BATCH_SIZE
        int initialBatchSize = Math.min(CONTENT_GENERATION_BATCH_SIZE, totalSize);
        int numBatches = (int) Math.ceil((double) totalSize / initialBatchSize);

        // 重新计算每批次大小，使分布更均匀
        int batchSize = (int) Math.ceil((double) totalSize / numBatches);
        // 确保每批至少包含minBatchSize个条目
        batchSize = Math.max(minBatchSize, batchSize);

        // 特殊情况处理：当总条目数少于最小批次大小时，直接作为一个批次
        if (totalSize < minBatchSize) {
            batches.add(new ArrayList<>(planEntries));
            return batches;
        }

        for (int i = 0; i < totalSize; i += batchSize) {
            int end = Math.min(i + batchSize, totalSize);
            batches.add(new ArrayList<>(planEntries.subList(i, end)));
        }

        // 模拟各种情况下的行为(CONTENT_GENERATION_BATCH_SIZE=4)
        // 1. 空列表 -> 返回空列表
        // 2. 1个条目 -> 单个批次(尽管小于minBatchSize，但这是特例)
        // 3. 2~4个条目 -> 1个批次，大小为条目数
        // 4. 5个条目 -> 2个批次(3+2)，避免出现单个条目的批次
        // 5. 6个条目 -> 2个批次(3+3)
        // 6. 7个条目 -> 2个批次(4+3)
        // 7. 8个条目 -> 2个批次(4+4)
        // 8. 9个条目 -> 3个批次(3+3+3)
        // 9. 10个条目 -> 3个批次(4+3+3)

        return batches;
    }

    private void saveContentResults(ResourceAiGenerateTaskParameters parameters, int pageIndex, int chunkIndexOffset, List<PlanEntryDto> planBatch, List<ContentResultDto> contentResults) {
        List<ResourceChunkEntity> chunksToSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int size = Math.min(planBatch.size(), contentResults.size());

        for (int i = 0; i < size; i++) {
            PlanEntryDto planEntry = planBatch.get(i);
            ContentResultDto contentResult = contentResults.get(i);

            ResourceChunkEntity chunk = new ResourceChunkEntity();
            chunk.setRcId(HutoolSnowflakeIdGenerator.generateLongId());
            chunk.setRcResourceId(parameters.getResourceId());
            chunk.setRcLearningSpaceId(parameters.getLearningSpaceId());
            chunk.setRcCreatedByUserId(parameters.getCreatedByUserId());
            chunk.setRcContent(contentResult.getContent());
            chunk.setRcKeywords(planEntry.getSearchKeyWords());
            chunk.setRcTokenCount(tokenCountEstimator.estimateTokenCountInText(contentResult.getContent()));
            chunk.setRcPageIndex(pageIndex);
            chunk.setRcChunkIndex(chunkIndexOffset + i);
            chunk.setRcIsVectorized(false);
            chunk.setRcCreatedAt(now);
            chunk.setRcUpdatedAt(now);
            chunksToSave.add(chunk);
        }

        if (!chunksToSave.isEmpty()) {
            resourceChunkRepository.saveBatch(chunksToSave);
            log.debug("保存了 {} 个资源分片", chunksToSave.size());
        }
    }
}