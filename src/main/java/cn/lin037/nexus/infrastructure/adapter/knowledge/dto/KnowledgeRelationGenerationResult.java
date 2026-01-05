package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识关系生成结果DTO
 * 用于接收AI关系生成的响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelationGenerationResult {

    /**
     * 新生成的关系列表
     */
    private List<AiKnowledgeRelation> relations;

    /**
     * 是否完成关系生成
     */
    private Boolean isComplete;
}