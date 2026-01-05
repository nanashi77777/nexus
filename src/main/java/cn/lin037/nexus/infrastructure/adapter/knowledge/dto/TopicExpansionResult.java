package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主题扩展结果DTO
 * 用于接收AI主题扩展的响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicExpansionResult {

    /**
     * 新生成的主题列表
     */
    private List<TopicDto> newTopics;

    /**
     * 新覆盖的chunk ID列表
     */
    private List<Long> newlyCoveredChunkIds;

    /**
     * 是否完成主题扩展
     */
    private Boolean isComplete;
}