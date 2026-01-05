package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 资料分片搜索参数类
 * 用于Agent工具执行器中的资料分片搜索功能参数传递
 *
 * @author Lin037
 */
@Data
public class ResourceChunkSearchParams {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 最大返回结果数量，默认为10
     */
    private Integer maxResults = 10;

    /**
     * 是否仅搜索当前学习空间的内容，默认为true
     */
    private Boolean scopeToLearningSpace = true;

    /**
     * 指定资源ID进行搜索（可选）
     */
    private Long resourceId;
}