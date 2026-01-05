package cn.lin037.nexus.infrastructure.adapter.resource;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import cn.lin037.nexus.application.resource.port.ResourceTaskPort;
import cn.lin037.nexus.infrastructure.adapter.resource.constant.ResourceTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceAiGenerateTaskParameters;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceChunkBatchVectorizeTaskParameters;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceChunkVectorizeTaskParameters;
import cn.lin037.nexus.infrastructure.adapter.resource.params.ResourceParseTaskParameters;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;
import cn.lin037.nexus.infrastructure.common.task.api.AsyncTaskManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 资源任务适配器
 *
 * @author LinSanQi
 */
@Component
public class ResourceTaskAdapter implements ResourceTaskPort {

    private final AsyncTaskManager asyncTaskManager;

    public ResourceTaskAdapter(AsyncTaskManager asyncTaskManager) {
        this.asyncTaskManager = asyncTaskManager;
    }

    @Override
    public Long submitParseTask(ResourceEntity resourceEntity, SliceStrategyEnum sliceStrategy, String ownerIdentifier) {
        ResourceParseTaskParameters parameters = ResourceParseTaskParameters.builder()
                .resourceId(resourceEntity.getRsId())
                .sourceType(ResourceSourceTypeEnum.fromCode(resourceEntity.getRsSourceType()))
                .sourceUri(resourceEntity.getRsSourceUri())
                .sliceStrategy(sliceStrategy)
                .learningSpaceId(resourceEntity.getRsLearningSpaceId())
                .createdByUserId(resourceEntity.getRsCreatedByUserId())
                .build();

        // 将参数对象转换为Map，因为AsyncTaskManager.submit需要Map<String, Object>
        Map<String, Object> parametersMap = BeanUtil.beanToMap(parameters);

        return asyncTaskManager.submit(
                ResourceTaskConstant.TASK_TYPE_RESOURCE_PARSE,
                parametersMap,
                ownerIdentifier
        );
    }

    @Override
    public Long submitAiGenerateTask(ResourceEntity resourceEntity, String ownerIdentifier) {

        ResourceAiGenerateTaskParameters parameters = ResourceAiGenerateTaskParameters.builder()
                .resourceId(resourceEntity.getRsId())
                .sourceType(ResourceSourceTypeEnum.fromCode(resourceEntity.getRsSourceType()))
                .rsPrompt(resourceEntity.getRsPrompt())
                .learningSpaceId(resourceEntity.getRsLearningSpaceId())
                .createdByUserId(resourceEntity.getRsCreatedByUserId())
                .build();

        // 将参数对象转换为Map，因为AsyncTaskManager.submit需要Map<String, Object>
        Map<String, Object> parametersMap = BeanUtil.beanToMap(parameters);

        return asyncTaskManager.submit(
                ResourceTaskConstant.TASK_TYPE_RESOURCE_AI_GENERATE,
                parametersMap,
                ownerIdentifier
        );
    }

    @Override
    public Long submitVectorizeTask(ResourceChunkEntity chunk, String ownerIdentifier) {
        ResourceChunkVectorizeTaskParameters parameters = ResourceChunkVectorizeTaskParameters.builder()
                .chunkId(chunk.getRcId())
                .createdByUserId(chunk.getRcCreatedByUserId())
                .learningSpaceId(chunk.getRcLearningSpaceId())
                .build();

        return asyncTaskManager.submit(
                ResourceTaskConstant.TASK_TYPE_RESOURCE_VECTORIZE,
                BeanUtil.beanToMap(parameters),
                ownerIdentifier
        );
    }

    @Override
    public Long submitBatchVectorizeTask(List<Long> chunkIds, String ownerIdentifier) {
        ResourceChunkBatchVectorizeTaskParameters parameters = ResourceChunkBatchVectorizeTaskParameters.builder()
                .chunkIds(chunkIds)
                .build();
        return asyncTaskManager.submit(
                ResourceTaskConstant.TASK_TYPE_RESOURCE_BATCH_VECTORIZE,
                BeanUtil.beanToMap(parameters),
                ownerIdentifier
        );
    }
} 