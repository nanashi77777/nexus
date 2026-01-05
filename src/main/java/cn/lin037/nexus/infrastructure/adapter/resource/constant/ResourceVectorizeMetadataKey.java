package cn.lin037.nexus.infrastructure.adapter.resource.constant;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量化任务元数据键常量
 */
public final class ResourceVectorizeMetadataKey {

    /**
     * 分片ID的元数据键
     */
    public static final String CHUNK_ID = "chunkId";
    /**
     * 资源ID的元数据键
     */
    public static final String RESOURCE_ID = "resourceId";
    /**
     * 用户ID的元数据键
     */
    public static final String USER_ID = "userId";
    /**
     * 学习空间ID的元数据键
     */
    public static final String LEARNING_SPACE_ID = "learningSpaceId";

    private ResourceVectorizeMetadataKey() {
        // 私有构造函数，防止实例化
    }

    public static @NotNull Map<String, String> getStringStringMap(ResourceChunkEntity chunk) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(ResourceVectorizeMetadataKey.CHUNK_ID, String.valueOf(chunk.getRcId()));
        metadata.put(ResourceVectorizeMetadataKey.RESOURCE_ID, String.valueOf(chunk.getRcResourceId()));
        metadata.put(ResourceVectorizeMetadataKey.USER_ID, String.valueOf(chunk.getRcCreatedByUserId()));
        metadata.put(ResourceVectorizeMetadataKey.LEARNING_SPACE_ID, String.valueOf(chunk.getRcLearningSpaceId()));
        return metadata;
    }
}