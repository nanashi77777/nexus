package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChunkContent {
    /**
     * 分片的id
     */
    private Long chunkId;
    /*
     * 分片的文本内容
     */
    private String chunkContent;
}
