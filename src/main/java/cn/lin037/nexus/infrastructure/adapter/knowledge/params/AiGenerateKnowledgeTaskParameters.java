package cn.lin037.nexus.infrastructure.adapter.knowledge.params;

import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiChunkContent;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgePoint;
import cn.lin037.nexus.infrastructure.adapter.knowledge.dto.AiKnowledgeRelation;
import cn.lin037.nexus.infrastructure.adapter.knowledge.enums.AiGenerationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成知识任务参数
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerateKnowledgeTaskParameters {
    private AiGenerationTypeEnum generationType;
    private Long userId;
    private Long learningSpaceId;
    private String prompt;
    private Boolean isVirtual;
    private Long targetDepositId;

    // --- Context-specific data objects ---

    /**
     * [FROM_RESOURCE] 资源分片列表
     */
    private List<AiChunkContent> chunks;

    /**
     * [EXPAND_KNOWLEDGE, CONNECT_KNOWLEDGE] 知识点版本列表
     */
    private List<AiKnowledgePoint> knowledgePoints;

    /**
     * [CONNECT_KNOWLEDGE] 已存在的知识点关系列表
     */
    private List<AiKnowledgeRelation> existingRelations;
}
