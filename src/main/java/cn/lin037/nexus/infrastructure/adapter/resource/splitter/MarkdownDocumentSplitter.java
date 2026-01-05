package cn.lin037.nexus.infrastructure.adapter.resource.splitter;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 自定义 Markdown 文档分割器
 * <p>
 * 实现按照 Markdown 语法结构进行智能分割：
 * - 按标题层级分割
 * - 列表保持完整性并支持前置标题
 * - 引用独立分割
 * - 表格独立分割并支持前置标题
 * - 代码块独立分割
 *
 * @author LinSanQi
 */
@Slf4j
@Component
public class MarkdownDocumentSplitter implements DocumentSplitter {

    /**
     * 最大分割长度（字符数）
     */
    private static final int MAX_TITLE_LENGTH = 200;

    private static final ThreadLocal<Parser> PARSER_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        // 配置 FlexMark 解析器，启用表格扩展
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(TablesExtension.create()));
        return Parser.builder(options).build();
    });

    /**
     * 最大分段长度，默认为 5000 个字符
     */
    private final int maxSegmentSizeInChars;

    public MarkdownDocumentSplitter(@Value("${langchain4j.document-splitter.max-segment-size-in-chars:500}") int maxSegmentSizeInChars) {
        this.maxSegmentSizeInChars = maxSegmentSizeInChars;
    }

    @Override
    public List<TextSegment> split(Document document) {
        return splitAll(Collections.singletonList(document));
    }

    @Override
    public List<TextSegment> splitAll(List<Document> documents) {
        List<TextSegment> allSegments = new ArrayList<>();

        for (Document document : documents) {
            List<TextSegment> segments = splitSingleDocument(document);
            allSegments.addAll(segments);
        }

        return allSegments;
    }

    private List<TextSegment> splitSingleDocument(Document document) {
        String markdownText = document.text();
        Node markdownRoot = PARSER_THREAD_LOCAL.get().parse(markdownText);

        List<MarkdownBlock> blocks = extractBlocks(markdownRoot, markdownText);

        return combineBlocksIntoSegments(blocks, document.metadata());
    }

    /**
     * 从 AST 中提取结构化的 Markdown 块
     */
    private List<MarkdownBlock> extractBlocks(Node root, String originalText) {
        List<MarkdownBlock> blocks = new ArrayList<>();

        for (Node child : root.getChildren()) {
            MarkdownBlock block = processNode(child, originalText);
            if (block != null) {
                blocks.add(block);
            }
        }

        return blocks;
    }

    /**
     * 处理单个 AST 节点
     */
    private MarkdownBlock processNode(Node node, String originalText) {
        String content = extractNodeContent(node, originalText);

        if (node instanceof Heading heading) {
            return new MarkdownBlock(MarkdownBlockType.HEADING, content, heading.getLevel());
        } else if (node instanceof BulletList || node instanceof OrderedList) {
            return new MarkdownBlock(MarkdownBlockType.LIST, content, 0);
        } else if (node instanceof BlockQuote) {
            return new MarkdownBlock(MarkdownBlockType.QUOTE, content, 0);
        } else if (node instanceof FencedCodeBlock || node instanceof IndentedCodeBlock) {
            return new MarkdownBlock(MarkdownBlockType.CODE, content, 0);
        } else if (isTableNode(node)) {
            return new MarkdownBlock(MarkdownBlockType.TABLE, content, 0);
        } else if (node instanceof Paragraph || node instanceof Text) {
            return new MarkdownBlock(MarkdownBlockType.PARAGRAPH, content, 0);
        }

        return null;
    }

    /**
     * 判断是否为表格节点
     */
    private boolean isTableNode(Node node) {
        return node.getClass().getSimpleName().contains("Table");
    }

    /**
     * 提取节点的文本内容
     */
    private String extractNodeContent(Node node, String originalText) {
        if (node.getStartOffset() >= 0 && node.getEndOffset() <= originalText.length()) {
            return originalText.substring(node.getStartOffset(), node.getEndOffset()).trim();
        }
        return node.getChars().toString().trim();
    }

    /**
     * 将 Markdown 块组合成文本段
     */
    private List<TextSegment> combineBlocksIntoSegments(List<MarkdownBlock> blocks, Metadata metadata) {
        List<TextSegment> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        String previousParagraph = "";

        for (int i = 0; i < blocks.size(); i++) {
            MarkdownBlock block = blocks.get(i);
            MarkdownBlock nextBlock = (i + 1 < blocks.size()) ? blocks.get(i + 1) : null;

            switch (block.type) {
                case HEADING -> handleHeading(block, currentSegment, segments, metadata);
                case LIST -> {
                    String title = extractTitle(previousParagraph);
                    currentSegment = flushCurrentSegment(currentSegment, segments, metadata);
                    segments.addAll(splitListIntoSegments(block.content, title, metadata));
                    previousParagraph = "";
                }
                case QUOTE, CODE -> {
                    currentSegment = flushCurrentSegment(currentSegment, segments, metadata);
                    segments.add(createTextSegment(block.content, metadata, segments.size()));
                }
                case TABLE -> {
                    String title = extractTitle(previousParagraph);
                    currentSegment = flushCurrentSegment(currentSegment, segments, metadata);
                    segments.addAll(splitTableIntoSegments(block.content, title, metadata));
                    previousParagraph = "";
                }
                case PARAGRAPH -> {
                    boolean nextIsListOrTable = nextBlock != null &&
                            (nextBlock.type == MarkdownBlockType.LIST || nextBlock.type == MarkdownBlockType.TABLE);

                    if (nextIsListOrTable) {
                        previousParagraph = block.content;
                    } else {
                        previousParagraph = "";
                    }

                    currentSegment = addContentToSegment(currentSegment, block.content, segments, metadata);
                }
            }
        }

        if (!currentSegment.isEmpty()) {
            segments.add(createTextSegment(currentSegment.toString(), metadata, segments.size()));
            log.debug("添加最后的段落，长度: {}", currentSegment.length());
        }

        return segments;
    }

    /**
     * 处理标题块
     */
    private void handleHeading(MarkdownBlock block, StringBuilder currentSegment,
                               List<TextSegment> segments, Metadata metadata) {
        if (!currentSegment.isEmpty() &&
                currentSegment.length() + block.content.length() > maxSegmentSizeInChars) {
            segments.add(createTextSegment(currentSegment.toString(), metadata, segments.size()));
            currentSegment.setLength(0);
        }

        if (!currentSegment.isEmpty()) {
            currentSegment.append("\n\n");
        }
        currentSegment.append(block.content);

        if (block.level <= 2 && currentSegment.length() > maxSegmentSizeInChars * 0.8) {
            segments.add(createTextSegment(currentSegment.toString(), metadata, segments.size()));
            currentSegment.setLength(0);
        }
    }

    /**
     * 将内容添加到当前段落，如果超出限制则创建新段落
     */
    private StringBuilder addContentToSegment(StringBuilder currentSegment, String content,
                                              List<TextSegment> segments, Metadata metadata) {
        if (!currentSegment.isEmpty() &&
                currentSegment.length() + content.length() + 2 > maxSegmentSizeInChars) {
            segments.add(createTextSegment(currentSegment.toString(), metadata, segments.size()));
            currentSegment = new StringBuilder();
        }

        if (!currentSegment.isEmpty()) {
            currentSegment.append("\n\n");
        }
        currentSegment.append(content);
        return currentSegment;
    }

    /**
     * 刷新当前段落到段落列表
     */
    private StringBuilder flushCurrentSegment(StringBuilder currentSegment,
                                              List<TextSegment> segments, Metadata metadata) {
        if (!currentSegment.isEmpty()) {
            segments.add(createTextSegment(currentSegment.toString(), metadata, segments.size()));
            return new StringBuilder();
        }
        return currentSegment;
    }

    /**
     * 提取并限制标题长度
     */
    private String extractTitle(String paragraph) {
        if (paragraph.isEmpty()) {
            return "";
        }

        if (paragraph.length() <= MAX_TITLE_LENGTH) {
            return paragraph;
        }

        // 按句子分割，找到最后一个完整句子
        String[] sentences = paragraph.split("[。！？.!?]");
        StringBuilder title = new StringBuilder();

        for (String sentence : sentences) {
            if (title.length() + sentence.length() + 1 <= MAX_TITLE_LENGTH) {
                if (!title.isEmpty()) {
                    title.append("。");
                }
                title.append(sentence.trim());
            } else {
                break;
            }
        }

        return title.toString();
    }

    /**
     * 将列表分割成多个文本段
     */
    private List<TextSegment> splitListIntoSegments(String listContent, String title, Metadata metadata) {
        String[] lines = listContent.split("\n");
        List<String> listItems = new ArrayList<>();

        // 提取列表项
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^[-*+]\\s+.*") || trimmed.matches("^\\d+\\.\\s+.*")) {
                listItems.add(line);
            } else if (!listItems.isEmpty()) {
                // 列表项的延续行
                listItems.set(listItems.size() - 1, listItems.getLast() + "\n" + line);
            }
        }

        log.debug("从列表内容中提取到 {} 个列表项", listItems.size());
        List<TextSegment> segments = createSegmentsFromItems(listItems, title, metadata, listItems.size());
        log.debug("列表项分割结果: {} 个段落", segments.size());

        return segments;
    }

    /**
     * 将表格分割成多个文本段
     */
    private List<TextSegment> splitTableIntoSegments(String tableContent, String title, Metadata metadata) {
        String[] lines = tableContent.split("\n");
        List<String> tableItems = new ArrayList<>();
        String header = "";

        // 提取表格标题和行
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.contains("|")) {
                if (i == 0) {
                    // 表格标题行
                    header = line;
                    // 跳过分隔行
                } else if (!line.matches("^[|\\s:-]+$")) {
                    tableItems.add(line);
                }
            }
        }

        log.debug("从表格内容中提取到 {} 行数据，表头: {}", tableItems.size(), header);

        // 如果有标题行，将其添加到每个段落
        String tableHeader = header.isEmpty() ? "" : header + "\n" + generateTableSeparator(header);

        List<TextSegment> segments = createSegmentsFromItems(tableItems, title, tableHeader, metadata, tableItems.size());
        log.debug("表格行分割结果: {} 个段落", segments.size());

        return segments;
    }

    /**
     * 生成表格分隔行
     */
    private String generateTableSeparator(String header) {
        int columnCount = header.split("\\|").length - 1;
        return "|" + "---|".repeat(Math.max(0, columnCount));
    }

    /**
     * 从项目列表创建文本段（用于列表）
     */
    private List<TextSegment> createSegmentsFromItems(List<String> items, String title,
                                                      Metadata metadata, int startIndex) {
        return createSegmentsFromItems(items, title, "", metadata, startIndex);
    }

    /**
     * 从项目列表创建文本段（用于表格和列表）
     */
    private List<TextSegment> createSegmentsFromItems(List<String> items, String title, String header,
                                                      Metadata metadata, int startIndex) {
        List<TextSegment> segments = new ArrayList<>();
        if (items.isEmpty()) {
            log.warn("项目列表为空，无法创建段落");
            return segments;
        }

        StringBuilder currentSegment = new StringBuilder();
        String titleHeader = buildTitleHeader(title, header);

        log.debug("开始分割 {} 个项目，标题头部长度: {}", items.size(), titleHeader.length());

        // 添加标题和表头
        currentSegment.append(titleHeader);

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            String itemWithNewline = item + "\n";

            // 检查添加当前项是否会超出限制
            if (currentSegment.length() + itemWithNewline.length() > maxSegmentSizeInChars) {
                // 如果当前段落只有标题，说明单个项目太大，强制添加
                if (currentSegment.toString().equals(titleHeader)) {
                    currentSegment.append(itemWithNewline);
                    segments.add(createTextSegment(currentSegment.toString().trim(), metadata, startIndex + segments.size()));
                    log.debug("单个项目过大，强制添加到段落 {}，长度: {}", segments.size(), currentSegment.length());
                    currentSegment = new StringBuilder(titleHeader);
                } else {
                    // 保存当前段落，开始新段落
                    segments.add(createTextSegment(currentSegment.toString().trim(), metadata, startIndex + segments.size()));
                    log.debug("完成段落 {}，长度: {}，包含 {} 个项目", segments.size(), currentSegment.length(), i);
                    currentSegment = new StringBuilder(titleHeader);
                    currentSegment.append(itemWithNewline);
                }
            } else {
                currentSegment.append(itemWithNewline);
            }
        }

        // 添加最后的段落
        if (!currentSegment.toString().equals(titleHeader)) {
            segments.add(createTextSegment(currentSegment.toString().trim(), metadata, startIndex + segments.size()));
            log.debug("添加最后段落 {}，长度: {}", segments.size(), currentSegment.length());
        }

        log.info("项目分割完成，总共生成 {} 个段落", segments.size());
        return segments;
    }

    /**
     * 构建标题头部
     */
    private String buildTitleHeader(String title, String header) {
        StringBuilder titleHeader = new StringBuilder();
        if (!title.isEmpty()) {
            titleHeader.append(title).append("\n\n");
        }
        if (!header.isEmpty()) {
            titleHeader.append(header).append("\n");
        }
        return titleHeader.toString();
    }

    /**
     * 创建文本段
     */
    private TextSegment createTextSegment(String content, Metadata metadata, int index) {
        Map<String, Object> segmentMetadata = new HashMap<>(metadata.toMap());
        segmentMetadata.put("index", index);
        TextSegment segment = TextSegment.from(content, Metadata.from(segmentMetadata));

        log.trace("创建文本段 {}: 长度={}, 元数据={}", index, content.length(), segmentMetadata);

        return segment;
    }

    /**
     * Markdown 块类型
     */
    private enum MarkdownBlockType {
        HEADING, PARAGRAPH, LIST, QUOTE, TABLE, CODE
    }

    /**
     * Markdown 块数据结构
     *
     * @param level 用于标题级别
     */
    private record MarkdownBlock(MarkdownBlockType type, String content, int level) {
    }
}

