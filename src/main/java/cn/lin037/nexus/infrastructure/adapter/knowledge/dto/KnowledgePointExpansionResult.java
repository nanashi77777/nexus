package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识点扩展结果DTO
 * 用于接收AI知识点扩展的响应结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePointExpansionResult {

    /**
     * 新扩展的知识点列表
     */
    private List<KnowledgePointDto> newKnowledgePoints;

    /**
     * 是否完成知识点扩展
     */
    private Boolean isComplete;
}