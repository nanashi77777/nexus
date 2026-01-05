package cn.lin037.nexus.infrastructure.adapter.explanation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 章节DTO
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDto {
    /**
     * 章节ID（临时ID，用于内部关联）
     */
    private Long sectionId;

    /**
     * 章节标题
     */
    private String sectionTitle;

    /**
     * 章节教学要求
     */
    private String sectionRequirement;

    /**
     * 关联的知识点ID列表
     */
    private List<Long> pointIdsForReference;

    /**
     * 关联的资源分片ID列表
     */
    private List<Long> chunkIdsForReference;
}