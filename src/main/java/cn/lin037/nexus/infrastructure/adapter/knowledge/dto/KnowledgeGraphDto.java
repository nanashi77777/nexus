package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识图谱DTO - 用于封装服务层最终结果的容器
 *
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphDto {

    /**
     * 知识点列表
     */
    private List<AiKnowledgePoint> points;

    /**
     * 知识关系列表
     */
    private List<AiKnowledgeRelation> relations;
}