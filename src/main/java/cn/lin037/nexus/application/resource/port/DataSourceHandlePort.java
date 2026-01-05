package cn.lin037.nexus.application.resource.port;

import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import dev.langchain4j.data.segment.TextSegment;

import java.nio.file.Path;
import java.util.List;

/**
 * 数据源解析器端口
 *
 * @author LinSanQi
 */
public interface DataSourceHandlePort {

    /**
     * 根据解析策略进行文件解析
     *
     * @param resourceId       资源ID
     * @param fileAbsolutePath 文件绝对路径
     * @param sliceStrategy    分片策略
     * @return 分片列表
     */
    List<TextSegment> parseFile(Long resourceId, Path fileAbsolutePath, SliceStrategyEnum sliceStrategy);

    /**
     * 根据解析策略进行链接解析
     *
     * @param resourceId    资源ID
     * @param sourceUrl     链接地址
     * @param sliceStrategy 分片策略
     * @return 分片列表
     */
    List<TextSegment> parseLink(Long resourceId, String sourceUrl, SliceStrategyEnum sliceStrategy);

    /**
     * 根据解析策略进行文本解析（这个其实是文本生成，而非解析）
     *
     * @param resourceId    资源ID
     * @param sourceText    文本内容
     * @param sliceStrategy 分片策略
     * @return 分片列表
     */
    List<TextSegment> generateUsingAI(Long resourceId, String sourceText, SliceStrategyEnum sliceStrategy);
} 