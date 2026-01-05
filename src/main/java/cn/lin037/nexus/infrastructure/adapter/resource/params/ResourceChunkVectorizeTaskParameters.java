package cn.lin037.nexus.infrastructure.adapter.resource.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceChunkVectorizeTaskParameters {
    private Long chunkId;
    private Long createdByUserId;
    private Long learningSpaceId;
}