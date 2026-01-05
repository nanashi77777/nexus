package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 知识点搜索参数类
 * 用于Agent工具执行器中的知识点搜索功能参数传递
 *
 * @author Lin037
 */
@Data
public class KnowledgeSearchParams {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 最大返回结果数量，默认为10
     */
    private Integer maxResults = 10;

    /**
     * 是否仅搜索当前学习空间的内容，默认为false
     */
    private Boolean scopeToLearningSpace = false;
}