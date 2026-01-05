package cn.lin037.nexus.application.resource.port;

import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceEntity;

import java.util.List;

/**
 * 资源任务端口
 *
 * @author LinSanQi
 */
public interface ResourceTaskPort {

    /**
     * 提交资源解析任务
     *
     * @param resourceEntity  资源实体对象
     * @param sliceStrategy   分片策略
     * @param ownerIdentifier 任务标识符
     * @return 任务ID
     */
    Long submitParseTask(ResourceEntity resourceEntity, SliceStrategyEnum sliceStrategy, String ownerIdentifier);

    /**
     * 提交AI生成任务
     *
     * @param resourceEntity  资源实体对象
     * @param ownerIdentifier 任务标识符
     * @return 任务ID
     */
    Long submitAiGenerateTask(ResourceEntity resourceEntity, String ownerIdentifier);

    /**
     * 提交分片向量化任务
     *
     * @param chunkEntity     分片实体对象
     * @param ownerIdentifier 任务标识符
     * @return 任务ID
     */
    Long submitVectorizeTask(ResourceChunkEntity chunkEntity, String ownerIdentifier);

    /**
     * 提交分片批量向量化任务
     *
     * @param chunkIds        分片ID列表
     * @param ownerIdentifier 任务标识符
     * @return 任务ID
     */
    Long submitBatchVectorizeTask(List<Long> chunkIds, String ownerIdentifier);
} 