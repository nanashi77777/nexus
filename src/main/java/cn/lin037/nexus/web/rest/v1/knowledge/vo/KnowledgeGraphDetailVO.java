package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import lombok.Data;

import java.util.List;

/**
 * 知识图谱详情视图对象
 *
 * @author LinSanQi
 */
@Data
public class KnowledgeGraphDetailVO {
    private Long id;
    private String title;
    private String description;
    private List<GraphNodeVO> nodes;
    private List<GraphEdgeVO> edges;
} 