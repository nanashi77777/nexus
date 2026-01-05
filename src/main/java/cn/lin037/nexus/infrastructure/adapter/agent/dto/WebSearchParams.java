package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.Data;

/**
 * 联网搜索参数类
 * 用于Agent工具执行器中的联网搜索功能参数传递
 *
 * @author Lin037
 */
@Data
public class WebSearchParams {

    /**
     * 搜索查询关键词
     */
    private String query;

    /**
     * 最大返回结果数量，默认为5
     */
    private Integer maxResults = 5;

    /**
     * 搜索语言，默认为中文
     */
    private String language = "zh";
}