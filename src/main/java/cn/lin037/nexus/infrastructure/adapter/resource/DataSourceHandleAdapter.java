package cn.lin037.nexus.infrastructure.adapter.resource;

import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import cn.lin037.nexus.application.resource.port.DataSourceHandlePort;
import cn.lin037.nexus.common.constant.SystemConstant;
import cn.lin037.nexus.infrastructure.adapter.resource.splitter.MarkdownDocumentSplitter;
import cn.lin037.nexus.infrastructure.common.ai.langchain4j.CustomTokenCountEstimator;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * 数据源解析适配器
 * 支持三种分割策略：
 * - RECURSIVE_BY_TOKEN: 基于 Token 的递归分割
 * - BY_SENTENCE: 基于句子的分割
 * - BY_MARKDOWN: 基于 Markdown 语法结构的智能分割
 *
 * @author LinSanQi
 */
@Slf4j
@Component
public class DataSourceHandleAdapter implements DataSourceHandlePort {

    private final ApacheTikaDocumentParser apacheTikaDocumentParser = new ApacheTikaDocumentParser();
    private final MarkdownDocumentSplitter markdownDocumentSplitter;
    private final CustomTokenCountEstimator customTokenCountEstimator;
    private DocumentSplitter recursiveSplitter;
    private DocumentSplitter sentenceSplitter;
    @Value("${langchain4j.document-splitter.max-segment-size-in-chars:500}")
    private Integer maxSegmentSizeInChars;
    @Value("${langchain4j.document-splitter.max-overlap-size-in-chars:64}")
    private Integer maxOverlapSizeInChars;
    // 针对句子分割的特殊配置
    @Value("${langchain4j.document-splitter.sentence.max-segment-size-in-chars:500}")
    private Integer sentenceMaxSegmentSizeInChars;
    @Value("${langchain4j.document-splitter.sentence.max-overlap-size-in-chars:64}")
    private Integer sentenceMaxOverlapSizeInChars;


    public DataSourceHandleAdapter(CustomTokenCountEstimator customTokenCountEstimator, MarkdownDocumentSplitter markdownDocumentSplitter) {
        this.markdownDocumentSplitter = markdownDocumentSplitter;
        this.customTokenCountEstimator = customTokenCountEstimator;
    }

    @PostConstruct
    private void init() {
        this.recursiveSplitter = DocumentSplitters.recursive(
                maxSegmentSizeInChars,
                maxOverlapSizeInChars,
                customTokenCountEstimator
        );
        this.sentenceSplitter = new DocumentBySentenceSplitter(
                sentenceMaxSegmentSizeInChars,
                sentenceMaxOverlapSizeInChars
        );
    }

    @Override
    public List<TextSegment> parseFile(Long resourceId, Path fileAbsolutePath, SliceStrategyEnum sliceStrategy) {
        Document document = FileSystemDocumentLoader.loadDocument(fileAbsolutePath);
        return splitDocument(resourceId, document, sliceStrategy);
    }

    @Override
    public List<TextSegment> parseLink(Long resourceId, String sourceUrl, SliceStrategyEnum sliceStrategy) {
        Document document = UrlDocumentLoader.load(sourceUrl, apacheTikaDocumentParser);
        return splitDocument(resourceId, document, sliceStrategy);
    }

    @Override
    public List<TextSegment> generateUsingAI(Long resourceId, String sourceText, SliceStrategyEnum sliceStrategy) {
        return List.of();
    }

    private List<TextSegment> splitDocument(Long resourceId, Document document, SliceStrategyEnum sliceStrategy) {
        document.metadata().put(SystemConstant.DATA_SOURCE_ID_KEY, resourceId);

        DocumentSplitter splitter = switch (sliceStrategy) {
            case RECURSIVE_BY_TOKEN -> {
                log.debug("使用基于 Token 的递归分割策略");
                yield recursiveSplitter;
            }
            case BY_SENTENCE -> {
                log.debug("使用基于句子的分割策略");
                yield sentenceSplitter;
            }
            case BY_MARKDOWN -> {
                log.debug("使用基于 Markdown 语法的智能分割策略");
                yield markdownDocumentSplitter;
            }
        };

        List<TextSegment> segments = splitter.split(document);
        log.info("文档分割完成，策略: {}, 生成 {} 个片段", sliceStrategy, segments.size());

        return segments;
    }
}
