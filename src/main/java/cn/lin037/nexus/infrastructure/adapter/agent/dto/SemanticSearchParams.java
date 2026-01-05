package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 语义搜索参数类
 * 用于Agent工具执行器中的语义搜索功能参数传递
 *
 * @author Lin037
 */
@Data
public class SemanticSearchParams {

    /**
     * 搜索查询文本
     */
    private String query;

    /**
     * 最大返回结果数量，默认为10
     */
    private Integer maxResults = 10;

    /**
     * 最小相似度阈值，默认为0.7
     */
    private Double minScore = 0.7;

    /**
     * 是否仅搜索当前学习空间的内容，默认为false
     */
    private Boolean scopeToLearningSpace = false;
}