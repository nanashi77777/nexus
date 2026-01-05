package cn.lin037.nexus.infrastructure.adapter.resource.impl;

import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.application.resource.port.VectorPort;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceChunkVectorizeTaskParameters;
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceVectorizeMetadataKey.getStringStringMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceChunkVectorizeTaskExecutor implements TaskExecutor<ResourceChunkVectorizeTaskParameters, TokenUsageAccumulator> {

    private static final String EMBEDDING_MODEL_ALIAS = "text-embedding-v4";
    private final ResourceChunkRepository resourceChunkRepository;
    private final VectorPort vectorPort;
    private final AiCoreService aiCoreService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public String getTaskType() {
        return ResourceTaskConstant.TASK_TYPE_RESOURCE_VECTORIZE;
    }

    @Override
    public Class<ResourceChunkVectorizeTaskParameters> getParametersType() {
        return ResourceChunkVectorizeTaskParameters.class;
    }

    @Override
    public Class<TokenUsageAccumulator> getResultType() {
        return TokenUsageAccumulator.class;
    }

    @Override
    public TaskResult<TokenUsageAccumulator> execute(ResourceChunkVectorizeTaskParameters parameters, TaskContext context) throws Exception {
        Long chunkId = parameters.getChunkId();
        log.info("开始执行分片向量化任务，chunkId: {}", chunkId);

        Optional<ResourceChunkEntity> chunkOptional = resourceChunkRepository.findById(chunkId, Collections.emptyList());
        if (chunkOptional.isEmpty()) {
            log.warn("分片向量化任务失败：未找到 chunkId 为 {} 的分片", chunkId);
            return TaskResult.success("分片不存在，跳过向量化", new TokenUsageAccumulator());
        }
        ResourceChunkEntity chunk = chunkOptional.get();

        if (Boolean.TRUE.equals(chunk.getRcIsVectorized()) && chunk.getRcVectorId() != null) {
            log.info("分片 {} 已被向量化，跳过任务。", chunkId);
            return TaskResult.success("分片已向量化，跳过任务", new TokenUsageAccumulator());
        }
        try {
            // 1. 获取默认的嵌入模型
            EmbeddingModel embeddingModel = aiCoreService.getEmbeddingModel(EMBEDDING_MODEL_ALIAS);
            int dimension = embeddingModel.dimension();

            // 2. 准备元数据
            Map<String, String> metadata = getStringStringMap(chunk);

            String vectorId;
            TokenUsage tokenUsage;

            // 3. 调用 VectorPort 进行向量化和存储
            if (Boolean.FALSE.equals(chunk.getRcIsVectorized()) && chunk.getRcVectorId() != null) {
                log.info("分片 {} 已被向量化过，开始重新向量化。", chunkId);

                vectorPort.delete(chunk.getRcVectorId(), chunk.getRcVectorDimension());
                StructResult<Void> upsertResult = vectorPort.upsertWithTokenUsage(chunk.getRcVectorId(), chunk.getRcContent(), metadata, embeddingModel);
                vectorId = chunk.getRcVectorId();
                tokenUsage = upsertResult.getTokenUsage();
            } else {
                StructResult<String> addResult = vectorPort.addWithTokenUsage(chunk.getRcContent(), metadata, embeddingModel);
                vectorId = addResult.getResult();
                tokenUsage = addResult.getTokenUsage();
            }

            // 4. 更新分片实体状态
            resourceChunkRepository.updateById(chunkId, updater -> {
                updater.setRcIsVectorized(true);
                updater.setRcVectorId(vectorId);
                updater.setRcVectorDimension(dimension);
            });

            log.info("分片 {} 向量化成功，vectorId: {}, Token使用量: {}", chunkId, vectorId, tokenUsage);

            TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();
            tokenAccumulator.add(tokenUsage);
            return TaskResult.success("分片向量化成功", tokenAccumulator);

        } catch (Exception e) {
            log.error("分片 {} 向量化任务执行失败", chunkId, e);
            // 在独立的事务中将分片状态重置为“未向量化”，以确保更新成功
            try {
                new TransactionTemplate(transactionManager).execute(status -> {
                    resourceChunkRepository.updateById(chunkId, updater -> updater.setRcIsVectorized(false));
                    return null;
                });
            } catch (Exception updateException) {
                log.warn("更新分片 {} 状态为“未向量化”时失败", chunkId, updateException);
            }
            // 重新抛出异常，以便任务管理器记录失败
            throw e;
        }
    }
} 