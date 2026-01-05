package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import cn.lin037.nexus.infrastructure.common.persistent.entity.resource.ResourceChunkEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于讲解文档生成的资源分片内容
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkContentForExplanation {
    /**
     * 分片ID
     */
    private Long chunkId;

    /**
     * 分片的文本内容
     */
    private String chunkContent;

    public static ChunkContentForExplanation fromResourceChunkEntity(ResourceChunkEntity entity) {
        return new ChunkContentForExplanation(
                entity.getRcId(),
                entity.getRcContent()
        );
    }
}