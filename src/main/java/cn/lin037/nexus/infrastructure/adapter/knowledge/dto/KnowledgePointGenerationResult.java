package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识点生成结果DTO
 * 用于接收AI知识点生成的响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointGenerationResult {

    /**
     * 新生成的知识点列表
     */
    private List<KnowledgePointDto> knowledgePoints;

    /**
     * 是否完成该主题的知识点生成
     */
    private Boolean isComplete;
}