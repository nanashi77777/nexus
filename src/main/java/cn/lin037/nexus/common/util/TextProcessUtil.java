package cn.lin037.nexus.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本处理工具类
 * 提供文本分割、合并等功能
 *
 * @author LinSanQi
 */
@Slf4j
public class TextProcessUtil {

    // 段落分隔符模式（多个连续换行符表示段落分隔）
    private static final Pattern PARAGRAPH_DELIMITER = Pattern.compile("\\n\\s*\\n");

    // 句子分隔符正则表达式
    // 中文句号
    private static final Pattern SENTENCE_DELIMITER_CN = Pattern.compile("[。！？]");
    // 英文句号
    private static final Pattern SENTENCE_DELIMITER_EN = Pattern.compile("[.!?]");
    // 换行符
    private static final Pattern SENTENCE_DELIMITER_NEWLINE = Pattern.compile("\\n");
    // 分号
    private static final Pattern SENTENCE_DELIMITER_SEMICOLON = Pattern.compile("[;；]");
    // 逗号
    private static final Pattern SENTENCE_DELIMITER_COMMA = Pattern.compile("[,，]");

    /**
     * 按段落智能分割文本
     * 将文本按段落拆分，并组合成接近但不超过指定字符数的文本块
     *
     * @param text             要拆分的文本内容
     * @param maxCharsPerChunk 每个文本块的最大字符数
     * @return 拆分后的文本块列表
     */
    public static List<String> splitTextIntoChunks(String text, int maxCharsPerChunk) {
        if (text == null || text.isEmpty()) {
            log.warn("输入文本为空，无法进行分段");
            return new ArrayList<>();
        }

        if (maxCharsPerChunk <= 0) {
            log.warn("无效的最大字符数参数: {}, 将使用默认值4000", maxCharsPerChunk);
            maxCharsPerChunk = 4000;
        }

        log.debug("开始对文本进行智能分段，文本长度: {}, 每块最大字符数: {}", text.length(), maxCharsPerChunk);

        try {
            // 按段落拆分文本（使用连续的换行符作为段落分隔）
            String[] paragraphs = PARAGRAPH_DELIMITER.split(text);
            log.debug("文本已拆分为{}个段落", paragraphs.length);

            List<String> chunks = new ArrayList<>();
            StringBuilder currentChunk = new StringBuilder();
            int currentLength = 0;

            for (String paragraph : paragraphs) {
                // 去除首尾空白字符
                String trimmedParagraph = paragraph.trim();
                if (trimmedParagraph.isEmpty()) {
                    continue; // 跳过空段落
                }

                int paragraphLength = trimmedParagraph.length();

                // 特殊情况：单个段落超过最大字符限制
                if (paragraphLength > maxCharsPerChunk) {
                    // 如果当前块不为空，先添加到结果中
                    if (currentLength > 0) {
                        chunks.add(currentChunk.toString());
                        currentChunk = new StringBuilder();
                        currentLength = 0;
                    }

                    // 对大段落进行句子分割
                    log.debug("段落长度({})超过限制({}), 进行句子分割", paragraphLength, maxCharsPerChunk);
                    List<String> smallerParts = splitLargeParagraphSmartly(trimmedParagraph, maxCharsPerChunk);
                    chunks.addAll(smallerParts);
                    continue;
                }

                // 正常情况：检查是否添加当前段落会超出限制
                // (currentLength > 0 ? 2 : 0) 表示如果当前文本块已有内容，需要额外加上两个换行符（\n\n）的长度
                if (currentLength + paragraphLength + (currentLength > 0 ? 2 : 0) > maxCharsPerChunk) {
                    // 添加当前块到结果并重置
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder(trimmedParagraph);
                    currentLength = paragraphLength;
                } else {
                    // 在当前块后添加段落
                    if (currentLength > 0) {
                        currentChunk.append("\n\n");
                        currentLength += 2;
                    }
                    currentChunk.append(trimmedParagraph);
                    currentLength += paragraphLength;
                }
            }

            // 添加最后一个块（如果有）
            if (currentLength > 0) {
                chunks.add(currentChunk.toString());
            }

            log.debug("文本分段完成，共生成{}个文本块", chunks.size());
            return chunks;

        } catch (Exception e) {
            log.error("文本分段处理失败", e);
            throw new RuntimeException("文本分段处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能分割大段落
     * 按照句号、换行、分号、逗号的优先级分割
     *
     * @param paragraph        要分割的段落
     * @param maxCharsPerChunk 每个文本块的最大字符数
     * @return 分割后的段落列表
     */
    private static List<String> splitLargeParagraphSmartly(String paragraph, int maxCharsPerChunk) {
        List<String> results = new ArrayList<>();

        // 如果段落长度小于最大字符数，直接返回
        if (paragraph.length() <= maxCharsPerChunk) {
            results.add(paragraph);
            return results;
        }

        // 按优先级尝试不同的分隔符进行分割

        // 1. 先尝试按中文句号分割
        List<String> chunks = tryToSplitWithDelimiter(paragraph, SENTENCE_DELIMITER_CN, maxCharsPerChunk);
        if (chunks != null) {
            return chunks;
        }

        // 2. 再尝试按英文句号分割
        chunks = tryToSplitWithDelimiter(paragraph, SENTENCE_DELIMITER_EN, maxCharsPerChunk);
        if (chunks != null) {
            return chunks;
        }

        // 3. 再尝试按换行符分割
        chunks = tryToSplitWithDelimiter(paragraph, SENTENCE_DELIMITER_NEWLINE, maxCharsPerChunk);
        if (chunks != null) {
            return chunks;
        }

        // 4. 再尝试按分号分割
        chunks = tryToSplitWithDelimiter(paragraph, SENTENCE_DELIMITER_SEMICOLON, maxCharsPerChunk);
        if (chunks != null) {
            return chunks;
        }

        // 5. 最后尝试按逗号分割
        chunks = tryToSplitWithDelimiter(paragraph, SENTENCE_DELIMITER_COMMA, maxCharsPerChunk);
        if (chunks != null) {
            return chunks;
        }

        // 6. 如果以上方法都不行，进行硬分割（尽量平均）
        log.debug("无法按句子分割段落，进行二分硬分割");

        // 找到最佳分割点，尽量接近中点
        int idealSplitPoint = paragraph.length() / 2;
        int actualSplitPoint = idealSplitPoint;

        // 尝试在理想分割点附近找到一个空格或标点作为分割点
        // 在中点前后20个字符内寻找
        for (int i = 0; i < 20; i++) {
            // 向右搜索
            int rightPos = idealSplitPoint + i;
            if (rightPos < paragraph.length() && isGoodSplitPoint(paragraph.charAt(rightPos))) {
                // 在标点后分割
                actualSplitPoint = rightPos + 1;
                break;
            }

            // 向左搜索
            int leftPos = idealSplitPoint - i;
            // 在标点后分割
            if (leftPos > 0 && isGoodSplitPoint(paragraph.charAt(leftPos))) {
                actualSplitPoint = leftPos + 1;
                break;
            }
        }

        // 分割段落
        String firstHalf = paragraph.substring(0, actualSplitPoint);
        String secondHalf = paragraph.substring(actualSplitPoint);

        // 递归处理两部分
        results.addAll(splitLargeParagraphSmartly(firstHalf, maxCharsPerChunk));
        results.addAll(splitLargeParagraphSmartly(secondHalf, maxCharsPerChunk));

        return results;
    }

    /**
     * 尝试使用指定的分隔符模式分割文本
     *
     * @param text             要分割的文本
     * @param delimiterPattern 分隔符正则表达式模式
     * @param maxCharsPerChunk 每个块的最大字符数
     * @return 分割成功返回块列表，失败返回null
     */
    private static List<String> tryToSplitWithDelimiter(String text, Pattern delimiterPattern, int maxCharsPerChunk) {
        List<String> sentences = splitByPattern(text, delimiterPattern);

        // 检查是否可以将分割后的句子组合成符合要求的块
        if (canFormChunks(sentences, maxCharsPerChunk)) {
            // 可以组合成块，进行组合
            return combineIntoChunks(sentences, maxCharsPerChunk);
        }

        // 无法组合成符合要求的块
        return null;
    }

    /**
     * 判断一个字符是否是良好的分割点
     *
     * @param c 要判断的字符
     * @return 是否是良好的分割点
     */
    private static boolean isGoodSplitPoint(char c) {
        return c == ' ' || c == ',' || c == '，' || c == '.' || c == '。' ||
                c == ';' || c == '；' || c == '!' || c == '！' || c == '?' ||
                c == '？' || c == '\n' || c == '\r';
    }

    /**
     * 使用正则表达式将文本分割成句子
     *
     * @param text    要分割的文本
     * @param pattern 分割的正则表达式模式
     * @return 分割后的句子列表
     */
    private static List<String> splitByPattern(String text, Pattern pattern) {
        List<String> sentences = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            int end = matcher.end();
            if (end > lastEnd) {
                sentences.add(text.substring(lastEnd, end));
                lastEnd = end;
            }
        }

        // 添加剩余部分
        if (lastEnd < text.length()) {
            sentences.add(text.substring(lastEnd));
        }

        return sentences;
    }

    /**
     * 检查是否可以将句子列表组合成不超过最大字符数的块
     *
     * @param sentences        句子列表
     * @param maxCharsPerChunk 每个文本块的最大字符数
     * @return 是否可以组合
     */
    private static boolean canFormChunks(List<String> sentences, int maxCharsPerChunk) {
        // 如果只有一个句子且超过限制，则无法组合
        if (sentences.size() == 1 && sentences.getFirst().length() > maxCharsPerChunk) {
            return false;
        }

        // 检查每个句子是否都小于最大字符数
        for (String sentence : sentences) {
            if (sentence.length() > maxCharsPerChunk) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将句子列表组合成不超过最大字符数的块
     *
     * @param sentences        句子列表
     * @param maxCharsPerChunk 每个文本块的最大字符数
     * @return 组合后的文本块列表
     */
    private static List<String> combineIntoChunks(List<String> sentences, int maxCharsPerChunk) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;

        for (String sentence : sentences) {
            if (currentLength + sentence.length() > maxCharsPerChunk) {
                // 当前块已满，添加到结果并重置
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder(sentence);
                currentLength = sentence.length();
            } else {
                // 添加句子到当前块
                currentChunk.append(sentence);
                currentLength += sentence.length();
            }
        }

        // 添加最后一个块
        if (currentLength > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }
} 