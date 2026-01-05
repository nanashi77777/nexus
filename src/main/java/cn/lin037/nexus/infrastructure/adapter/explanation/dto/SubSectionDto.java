package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小节DTO
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubSectionDto {
    /**
     * 小节ID（临时ID，用于内部关联）
     */
    private Long subsectionId;

    /**
     * 所属章节ID
     */
    private Long parentSectionId;

    /**
     * 小节标题
     */
    private String subsectionTitle;

    /**
     * 小节教学要求
     */
    private String subsectionRequirement;

    /**
     * 关联的知识点ID列表
     */
    private List<Long> pointIdsForReference;

    /**
     * 关联的资源分片ID列表
     */
    private List<Long> chunkIdsForReference;
}