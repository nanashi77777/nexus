package cn.lin037.nexus.infrastructure.adapter.resource.impl;

import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.application.resource.port.VectorPort;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceChunkBatchVectorizeTaskParameters;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.TokenUsage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceVectorizeMetadataKey.getStringStringMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceChunkBatchVectorizeTaskExecutor implements TaskExecutor<ResourceChunkBatchVectorizeTaskParameters, TokenUsageAccumulator> {

    private static final String EMBEDDING_MODEL_ALIAS = "text-embedding-v4";
    private final ResourceChunkRepository resourceChunkRepository;
    private final VectorPort vectorPort;
    private final AiCoreService aiCoreService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public String getTaskType() {
        return ResourceTaskConstant.TASK_TYPE_RESOURCE_BATCH_VECTORIZE;
    }

    @Override
    public Class<ResourceChunkBatchVectorizeTaskParameters> getParametersType() {
        return ResourceChunkBatchVectorizeTaskParameters.class;
    }

    @Override
    public Class<TokenUsageAccumulator> getResultType() {
        return TokenUsageAccumulator.class;
    }

    @Override
    public TaskResult<TokenUsageAccumulator> execute(ResourceChunkBatchVectorizeTaskParameters parameters, TaskContext context) throws Exception {
        if (parameters == null || CollectionUtils.isEmpty(parameters.getChunkIds())) {
            log.warn("批量向量化任务参数为空，任务终止。");
            return TaskResult.success("批量向量化任务参数为空", new TokenUsageAccumulator());
        }
        log.info("开始执行批量分片向量化任务，分片ID: {}", parameters.getChunkIds());

        List<ResourceChunkEntity> chunks = resourceChunkRepository.findByIds(parameters.getChunkIds());
        List<ResourceChunkEntity> chunksToProcess = chunks.stream()
                .filter(chunk -> !Boolean.TRUE.equals(chunk.getRcIsVectorized()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(chunksToProcess)) {
            log.info("所有分片均已向量化，任务完成。");
            return TaskResult.success("所有分片均已向量化", new TokenUsageAccumulator());
        }

        try {
            EmbeddingModel embeddingModel = aiCoreService.getEmbeddingModel(EMBEDDING_MODEL_ALIAS);
            int dimension = embeddingModel.dimension();

            List<String> contents = chunksToProcess.stream().map(ResourceChunkEntity::getRcContent).collect(Collectors.toList());
            // 注意：批量添加时，所有分片的元数据需要相同，这可能需要在未来进行更精细的分组处理
            Map<String, String> commonMetadata = getStringStringMap(chunksToProcess.getFirst());

            StructResult<List<String>> batchResult = vectorPort.batchAddWithTokenUsage(contents, commonMetadata, embeddingModel);
            List<String> vectorIds = batchResult.getResult();
            TokenUsage tokenUsage = batchResult.getTokenUsage();

            for (int i = 0; i < chunksToProcess.size(); i++) {
                ResourceChunkEntity chunk = chunksToProcess.get(i);
                String vectorId = vectorIds.get(i);
                resourceChunkRepository.updateById(chunk.getRcId(), updater -> {
                    updater.setRcIsVectorized(true);
                    updater.setRcVectorId(vectorId);
                    updater.setRcVectorDimension(dimension);
                });
            }

            log.info("批量向量化任务成功完成，共处理 {} 个分片，Token使用量: {}。", chunksToProcess.size(), tokenUsage);

            TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();
            tokenAccumulator.add(tokenUsage);
            return TaskResult.success("批量向量化任务成功完成", tokenAccumulator);
        } catch (Exception e) {
            log.error("批量向量化任务失败。", e);
            // 在独立的事务中将分片状态重置为“未向量化”，以确保更新成功
            try {
                new TransactionTemplate(transactionManager).execute(status -> {
                    chunksToProcess.forEach(chunk -> {
                        resourceChunkRepository.updateById(chunk.getRcId(), updater -> updater.setRcIsVectorized(false));
                    });
                    return null;
                });
            } catch (Exception updateException) {
                log.warn("批量更新分片状态为“未向量化”时失败", updateException);
            }
            throw e;
        }
    }
}
