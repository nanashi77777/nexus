package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 主题DTO
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicDto {

    /**
     * 主题标题
     */
    private String topicTitle;

    /**
     * 生成要求
     */
    private String generationRequirement;

    /**
     * 目标参考资料chunk ID列表
     */
    private List<Long> targetChunkIds;

    /**
     * 该主题预期生成的最大知识点数量
     */
    private Integer maxPoints;
}