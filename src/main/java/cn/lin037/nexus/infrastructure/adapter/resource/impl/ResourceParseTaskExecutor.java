package cn.lin037.nexus.infrastructure.adapter.resource.impl;

import cn.lin037.nexus.application.resource.enums.ResourceErrorCodeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.ResourceStatusEnum;
import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import cn.lin037.nexus.application.resource.event.ResourceParsedEvent;
import cn.lin037.nexus.application.resource.port.DataSourceHandlePort;
import cn.lin037.nexus.application.resource.port.ResourceChunkRepository;
import cn.lin037.nexus.application.resource.port.ResourceRepository;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceParseTaskParameters;
import cn.lin037.nexus.infrastructure.common.ai.langchain4j.CustomTokenCountEstimator;
import cn.lin037.nexus.infrastructure.common.file.service.FileStorageService;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源解析任务执行器
 *
 * @author LinSanQi
 */
@Slf4j
@Component
public class ResourceParseTaskExecutor implements TaskExecutor<ResourceParseTaskParameters, Void> {

    private final ResourceRepository resourceRepository;
    private final ResourceChunkRepository resourceChunkRepository;
    private final DataSourceHandlePort dataSourceHandlePort;
    private final FileStorageService fileStorageService;
    private final CustomTokenCountEstimator tokenCountEstimator;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher eventPublisher;

    public ResourceParseTaskExecutor(ResourceRepository resourceRepository,
                                     ResourceChunkRepository resourceChunkRepository,
                                     DataSourceHandlePort dataSourceHandlePort,
                                     FileStorageService fileStorageService,
                                     CustomTokenCountEstimator tokenCountEstimator,
                                     PlatformTransactionManager transactionManager,
                                     ApplicationEventPublisher eventPublisher) {
        this.resourceRepository = resourceRepository;
        this.resourceChunkRepository = resourceChunkRepository;
        this.dataSourceHandlePort = dataSourceHandlePort;
        this.fileStorageService = fileStorageService;
        this.tokenCountEstimator = tokenCountEstimator;
        this.transactionManager = transactionManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String getTaskType() {
        return ResourceTaskConstant.TASK_TYPE_RESOURCE_PARSE;
    }

    @Override
    public Class<ResourceParseTaskParameters> getParametersType() {
        return ResourceParseTaskParameters.class;
    }

    @Override
    public Class<Void> getResultType() {
        return Void.class;
    }

    @Override
    public TaskResult<Void> execute(ResourceParseTaskParameters parameters, TaskContext context) throws Exception {
        Long resourceId = parameters.getResourceId();
        Long learningSpaceId = parameters.getLearningSpaceId();
        Long createdByUserId = parameters.getCreatedByUserId();
        ResourceSourceTypeEnum sourceType = parameters.getSourceType();
        String sourceUri = parameters.getSourceUri();
        SliceStrategyEnum sliceStrategy = parameters.getSliceStrategy();

        log.info("开始解析资源 ID: {}, 类型: {}, 策略: {}", resourceId, sourceType, sliceStrategy);

        // 使用编程式事务，立刻更新并提交状态，以便外部可以立即看到任务正在进行中
        new TransactionTemplate(transactionManager).execute(status -> {
            // 1. 更新任务状态为"解析中"
            updateResourceStatus(resourceId, ResourceStatusEnum.PARSING, null);
            return null;
        });

        try {
            // 2. 验证资源是否仍然存在
            if (!resourceRepository.existsById(resourceId)) {
                log.warn("资源已被删除，任务取消，资源ID: {}", resourceId);
                throw new ApplicationException(ResourceErrorCodeEnum.RESOURCE_NOT_FOUND);
            }

            // 3. 执行解析
            List<TextSegment> segments = performParsing(resourceId, sourceType, sourceUri, sliceStrategy);

            // 4. 检查是否被取消
            if (context.isCancellationRequested()) {
                log.info("任务在解析完成后被取消，资源ID: {}", resourceId);
                throw new ApplicationException(ResourceErrorCodeEnum.TASK_CANCELLED, "资源ID: " + resourceId);
            }

            // 5. 保存分片 (此操作在TaskScheduler的主事务中)
            saveResourceChunks(resourceId, learningSpaceId, createdByUserId, segments);

            // 6. 更新任务状态为"完成" (此操作在TaskScheduler的主事务中)
            updateResourceStatus(resourceId, ResourceStatusEnum.PARSE_COMPLETED, null);

            log.info("资源解析任务完成，资源ID: {}, 生成分片数: {}", resourceId, segments.size());

        } catch (Exception e) {
            log.error("资源解析失败，资源ID: {}", resourceId, e);

            // 7. 在独立的事务中更新失败状态，确保即使主事务回滚，状态也能被成功持久化
            try {
                new TransactionTemplate(transactionManager).execute(status -> {
                    updateResourceStatus(resourceId, ResourceStatusEnum.PARSE_FAILED, e.getMessage());
                    return null;
                });
            } catch (Exception updateException) {
                log.warn("更新资源状态失败，资源ID: {} (可能资源已被删除)", resourceId, updateException);
                // 不抛出新异常，保持原始异常的完整性
            }

            // 重新抛出原始异常，以便AsyncTaskManager能捕获并记录
            throw e;
        }

        return null;
    }

    /**
     * 执行具体的解析逻辑
     */
    private List<TextSegment> performParsing(Long resourceId, ResourceSourceTypeEnum sourceType,
                                             String sourceUri, SliceStrategyEnum sliceStrategy) {

        return switch (sourceType) {
            // 文件类型
            case UPLOAD -> {
                if (sourceUri == null || sourceUri.isBlank()) {
                    throw new ApplicationException(ResourceErrorCodeEnum.UPLOAD_SOURCE_URI_EMPTY, "资源ID: " + resourceId);
                }
                Path filePath = fileStorageService.getAbsolutePath(sourceUri);
                log.debug("解析上传文件: {}", filePath);
                yield dataSourceHandlePort.parseFile(resourceId, filePath, sliceStrategy);
            }
            // 链接类型
            case LINK -> {
                if (sourceUri == null || sourceUri.isBlank()) {
                    throw new ApplicationException(ResourceErrorCodeEnum.LINK_SOURCE_URI_EMPTY, "资源ID: " + resourceId);
                }
                log.debug("解析链接资源: {}", sourceUri);
                yield dataSourceHandlePort.parseLink(resourceId, sourceUri, sliceStrategy);
            }
            default -> {
                log.warn("资源 {} 的来源类型 {} 不支持解析", resourceId, sourceType);
                // 返回空列表，而不是异常
                yield List.of();
            }
        };
    }

    /**
     * 保存资源分片
     */
    private void saveResourceChunks(Long resourceId, Long learningSpaceId, Long createdByUserId, List<TextSegment> segments) {
        if (segments.isEmpty()) {
            log.warn("没有生成任何分片，资源ID: {}", resourceId);
            return;
        }

        List<ResourceChunkEntity> chunks = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            ResourceChunkEntity chunk = new ResourceChunkEntity();
            chunk.setRcResourceId(resourceId);
            chunk.setRcContent(segment.text());

            // 从segment的metadata中获取页面和分片索引信息
            // 默认模拟值：每10个分片为一页
            chunk.setRcPageIndex(i / 10);
            // 使用循环索引作为分片索引
            chunk.setRcChunkIndex(i);
            // 计算Token量
            int tokenCount = tokenCountEstimator.estimateTokenCountInText(segment.text());
            chunk.setRcTokenCount(tokenCount);
            // 学习空间
            chunk.setRcLearningSpaceId(learningSpaceId);
            // 创建者
            chunk.setRcCreatedByUserId(createdByUserId);

            chunks.add(chunk);
        }

        log.debug("准备保存 {} 个分片到数据库", chunks.size());
        resourceChunkRepository.saveBatch(chunks);
        log.info("成功保存 {} 个分片，资源ID: {}", chunks.size(), resourceId);

        // 发布资源解析完成事件，由监听器触发向量化任务
        List<Long> chunkIds = chunks.stream().map(ResourceChunkEntity::getRcId).toList();
        eventPublisher.publishEvent(new ResourceParsedEvent(this, resourceId, chunkIds, createdByUserId));
        log.info("已发布资源解析完成事件，分片数量: {}", chunkIds.size());
    }

    /**
     * 更新资源状态
     */
    private void updateResourceStatus(Long resourceId, ResourceStatusEnum status, String errorMessage) {
        resourceRepository.updateById(resourceId,
                updater -> {
                    updater.setRsStatus(status.getCode());
                    if (errorMessage != null) {
                        updater.setRsParseErrorMessage(errorMessage);
                    }
                });

        log.debug("资源状态已更新，ID: {}, 状态: {}", resourceId, status);
    }


}
