package cn.lin037.nexus.infrastructure.adapter.resource.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceChunkBatchVectorizeTaskParameters {
    private List<Long> chunkIds;
    private Long createdByUserId;
    private Long learningSpaceId;
} 