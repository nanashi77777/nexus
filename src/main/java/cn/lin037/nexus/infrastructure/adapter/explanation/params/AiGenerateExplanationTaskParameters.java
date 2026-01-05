package cn.lin037.nexus.infrastructure.adapter.explanation.params;

import cn.lin037.nexus.infrastructure.adapter.explanation.dto.ChunkContentForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.KnowledgePointForExplanation;
import cn.lin037.nexus.infrastructure.adapter.explanation.dto.KnowledgeRelationForExplanation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成讲解文档任务参数
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateExplanationTaskParameters {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习空间ID
     */
    private Long learningSpaceId;

    /**
     * 讲解文档ID
     */
    private Long explanationDocumentId;

    /**
     * 用户额外要求的prompt
     */
    private String userPrompt;

    /**
     * 资源分片列表
     */
    private List<ChunkContentForExplanation> chunks;

    /**
     * 知识点列表
     */
    private List<KnowledgePointForExplanation> knowledgePoints;

    /**
     * 知识点关系列表
     */
    private List<KnowledgeRelationForExplanation> knowledgeRelations;

}