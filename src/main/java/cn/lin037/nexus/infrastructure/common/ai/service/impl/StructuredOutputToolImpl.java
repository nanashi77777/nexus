package cn.lin037.nexus.infrastructure.common.ai.service.impl;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.ai.exception.AIInfraExceptionEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.ai.service.StructuredOutputTool;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * 结构化输出工具实现
 *
 * @author LinSanQi
 */
@Slf4j
@Component
public class StructuredOutputToolImpl implements StructuredOutputTool {

    private static final int MAX_RETRY_ATTEMPTS = 2; // 最多重试1次，总计执行2次
    private static final long RETRY_DELAY_MS = 1000; // 重试延迟1秒
    private static final int USER_PROMPT_SUMMARY_LENGTH = 100; // 可根据需要调整

    /**
     * 执行带重试机制的操作
     */
    private <T> StructResult<T> executeWithRetry(String operationType, Supplier<StructResult<T>> operation) {
        TokenUsageAccumulator tokenAccumulator = new TokenUsageAccumulator();
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                log.debug("{} 尝试第 {} 次执行", operationType, attempt);
                StructResult<T> result = operation.get();
                if (result.getTokenUsage() != null) {
                    tokenAccumulator.add(result.getTokenUsage());
                }

                // 如果不是第一次尝试，更新结果中的累计TokenUsage
                if (attempt > 1) {
                    return StructResult.of(result.getContent(), result.getResult(), tokenAccumulator.getTotal());
                }
                return result;

            } catch (Exception e) {
                lastException = e;
                log.warn("{} 第 {} 次尝试失败: {}", operationType, attempt, e.getMessage());

                // 如果不是最后一次尝试，等待后重试
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new InfrastructureException(AIInfraExceptionEnum.AI_RESPONSE_PARSE_ERROR, "重试被中断");
                    }
                }
            }
        }

        // 所有重试都失败，抛出异常并携带累计的TokenUsage
        log.error("{} 所有重试都失败", operationType, lastException);
        // 这里可以考虑在异常中携带TokenUsage信息，但需要修改异常类
        throw new InfrastructureException(
                AIInfraExceptionEnum.AI_RESPONSE_PARSE_ERROR,
                lastException.getMessage()
        );
    }

    @Override
    public StructResult<String> generateStringOutput(ChatModel model, String systemPrompt, String userPrompt) {
        try {
            // 使用独立的SystemMessage和UserMessage，这更符合模型的设计
            List<ChatMessage> chatMessages = List.of(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userPrompt)
            );
            ChatResponse response = model.chat(chatMessages);
            log.debug("[CHAT_SINGLE] AI生成的内容: {}", response.aiMessage().text());
            return StructResult.of(response.aiMessage().text(), response.aiMessage().text(), response.tokenUsage());
        } catch (Exception e) {
            log.error("[CHAT_SINGLE] 生成结构化输出失败", e);
            throw new InfrastructureException(AIInfraExceptionEnum.AI_RESPONSE_PARSE_ERROR, e.getMessage());
        }
    }

    @Override
    public <T> StructResult<T> generateStructuredOutput(LanguageModel model, String systemPrompt, String userPrompt, T exampleOutput, Class<T> targetType) {
        return executeWithRetry("[LLM_SINGLE]", () -> {
            Response<String> response = executeLanguageModel(model, systemPrompt, userPrompt, exampleOutput, false);
            String rawContent = response.content();
            log.debug("[LLM_SINGLE] AI生成的原始内容: {}", sanitizeForSingleLineLogging(rawContent));

            // 首先尝试直接解析
            T bean;
            String finalContent;
            try {
                bean = JSONUtil.toBean(rawContent, targetType);
                finalContent = rawContent;
                log.debug("[LLM_SINGLE] 直接解析成功");
            } catch (Exception e) {
                log.debug("[LLM_SINGLE] 直接解析失败，尝试清理后解析: {}", e.getMessage());
                // 直接解析失败，尝试清理后解析
                String cleanedContent = cleanJsonContent(rawContent);
                log.debug("[LLM_SINGLE] 清理后的JSON内容: {}", cleanedContent);
                bean = JSONUtil.toBean(cleanedContent, targetType);
                finalContent = cleanedContent;
                log.debug("[LLM_SINGLE] 清理后解析成功");
            }

            return StructResult.of(finalContent, bean, response.tokenUsage());
        });
    }

    @Override
    public <R> StructResult<List<R>> generateStructuredOutputList(LanguageModel model, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType) {
        return executeWithRetry("[LLM_LIST]", () -> {
            Response<String> response = executeLanguageModel(model, systemPrompt, userPrompt, exampleOutput, true);
            String rawContent = response.content();
            log.debug("[LLM_LIST] AI生成的原始内容: {}", sanitizeForSingleLineLogging(rawContent));

            // 首先尝试直接解析
            List<R> resultList;
            String finalContent;
            try {
                resultList = JSONUtil.toList(rawContent, targetType);
                finalContent = rawContent;
                log.debug("[LLM_LIST] 直接解析成功");
            } catch (Exception e) {
                log.debug("[LLM_LIST] 直接解析失败，尝试清理后解析: {}", e.getMessage());
                // 直接解析失败，尝试清理后解析
                String cleanedContent = cleanJsonContent(rawContent);
                log.debug("[LLM_LIST] 清理后的JSON内容: {}", cleanedContent);
                resultList = JSONUtil.toList(cleanedContent, targetType);
                finalContent = cleanedContent;
                log.debug("[LLM_LIST] 清理后解析成功");
            }

            return StructResult.of(finalContent, resultList, response.tokenUsage());
        });
    }

    @Override
    public <R> StructResult<R> generateStructuredOutput(ChatModel chatModel, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType) {
        return executeWithRetry("[CHAT_SINGLE]", () -> {
            ChatResponse response = executeChatModel(chatModel, systemPrompt, userPrompt, exampleOutput, false);
            String rawContent = response.aiMessage().text();
            log.debug("[CHAT_SINGLE] AI生成的原始内容: {}", sanitizeForSingleLineLogging(rawContent));

            // 首先尝试直接解析
            R bean;
            String finalContent;
            try {
                bean = JSONUtil.toBean(rawContent, targetType);
                finalContent = rawContent;
                log.debug("[CHAT_SINGLE] 直接解析成功");
            } catch (Exception e) {
                log.debug("[CHAT_SINGLE] 直接解析失败，尝试清理后解析: {}", e.getMessage());
                // 直接解析失败，尝试清理后解析
                String cleanedContent = cleanJsonContent(rawContent);
                log.debug("[CHAT_SINGLE] 清理后的JSON内容: {}", cleanedContent);
                bean = JSONUtil.toBean(cleanedContent, targetType);
                finalContent = cleanedContent;
                log.debug("[CHAT_SINGLE] 清理后解析成功");
            }

            return StructResult.of(finalContent, bean, response.tokenUsage());
        });
    }

    @Override
    public <R> StructResult<List<R>> generateStructuredOutputList(ChatModel chatModel, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType) {
        return executeWithRetry("[CHAT_LIST]", () -> {
            ChatResponse response = executeChatModel(chatModel, systemPrompt, userPrompt, exampleOutput, true);
            String rawContent = response.aiMessage().text();
            log.debug("[CHAT_LIST] AI生成的原始内容: {}", sanitizeForSingleLineLogging(rawContent));

            // 首先尝试直接解析
            List<R> resultList;
            String finalContent;
            try {
                resultList = JSONUtil.toList(rawContent, targetType);
                finalContent = rawContent;
                log.debug("[CHAT_LIST] 直接解析成功");
            } catch (Exception e) {
                log.debug("[CHAT_LIST] 直接解析失败，尝试清理后解析: {}", e.getMessage());
                // 直接解析失败，尝试清理后解析
                String cleanedContent = cleanJsonContent(rawContent);
                log.debug("[CHAT_LIST] 清理后的JSON内容: {}", cleanedContent);
                resultList = JSONUtil.toList(cleanedContent, targetType);
                finalContent = cleanedContent;
                log.debug("[CHAT_LIST] 清理后解析成功");
            }

            return StructResult.of(finalContent, resultList, response.tokenUsage());
        });
    }

    /**
     * 执行LanguageModel生成响应
     */
    private <T> Response<String> executeLanguageModel(LanguageModel model, String systemPrompt, String userPrompt, T exampleOutput, boolean isList) {
        String exampleJson = JSONUtil.toJsonPrettyStr(exampleOutput);
        String fullSystemPrompt = buildFormatInstructionPrompt(systemPrompt + userPrompt + exampleJson, isList);
        logPromptDetails(
                isList ? "LLM_LIST" : "LLM_SINGLE",
                "", // 在此模型中，所有内容都在一个提示中
                fullSystemPrompt
        );

        Prompt from = Prompt.from(fullSystemPrompt);
        return model.generate(from);
    }

    /**
     * 执行ChatModel生成响应
     */
    private <T> ChatResponse executeChatModel(ChatModel chatModel, String systemPrompt, String userPrompt, T exampleOutput, boolean isList) {
        String exampleJson = JSONUtil.toJsonPrettyStr(exampleOutput);

        // 构建一个更专注于格式要求的系统提示
        String formatInstructionPrompt = buildFormatInstructionPrompt(exampleJson, isList);

        // 完整的系统提示现在由两部分组成：任务指令 + 格式指令
        String fullSystemPrompt = systemPrompt + "\n\n" + formatInstructionPrompt;

        logPromptDetails(
                isList ? "CHAT_LIST" : "CHAT_SINGLE",
                fullSystemPrompt,
                userPrompt
        );

        // 使用独立的SystemMessage和UserMessage，这更符合模型的设计
        List<ChatMessage> chatMessages = List.of(
                SystemMessage.from(fullSystemPrompt),
                UserMessage.from(userPrompt)
        );
        return chatModel.chat(chatMessages);
    }


    /**
     * 构建专注于JSON格式和输出规则的提示部分
     *
     * @param exampleJson 示例JSON
     * @param isList      是否为列表
     * @return 格式化指令字符串
     */
    private String buildFormatInstructionPrompt(String exampleJson, boolean isList) {
        String corePrinciples = """
                【核心原则】
                1. 准确性：确保生成的数据符合业务逻辑和常识
                2. 完整性：所有必填字段都必须有合理的值
                3. 一致性：数据之间保持逻辑一致性
                4. 规范性：严格遵循JSON格式标准，确保所有特殊字符都已正确转义
                
                """;

        String outputRequirements = """
                【输出要求】
                1. 只输出符合以下JSON结构的字符串，不要添加任何解释文字、代码块标记或其他内容。
                2. 确保JSON格式完全正确，可以被程序直接解析，不允许使用代码块包裹（例如```json```）。
                3. **绝对不允许使用 '...' 或任何形式的省略号来截断内容。**
                4. 所有字段都必须填写，不能为空、null或undefined。
                5. **JSON格式严格要求**：
                   - 所有对象和数组元素之间不要有多余的空行或换行
                   - 字符串必须用双引号包围，不能使用单引号
                   - JSON数组格式必须是：[{...},{...},{...}] 而不是分行写法
                   - 所有字符串内的双引号必须转义为 \\"
                   - 所有反斜杠必须转义为 \\\\
                6. **特殊字符处理**：
                   - 换行使用 \\n 表示
                   - 制表符使用 \\t 表示
                   - 代码示例中的反斜杠必须双重转义：\\\\ 变成 \\\\\\\\
                   - 代码示例中的引号必须转义：\\" 变成 \\\\"
                7. **内容要求**：
                   - 如果内容包含Markdown格式，确保所有特殊字符不会破坏JSON结构
                   - 代码块使用 \\n```\\n...\\n```\\n 的形式嵌入在字符串中
                   - 保持内容的完整性，不要截断或省略
                
                """;

        String jsonFormatExample = """
                【正确JSON格式示例】
                如果是数组，必须严格按照以下格式，不要有多余换行：
                [{"field1":"value1","field2":"value2"},{"field1":"value3","field2":"value4"}]
                
                如果内容包含代码或特殊字符，必须正确转义：
                {"content":"git add . : 快速添加所有文件\\ngit commit -m \\"your message\\" : 提交更改"}
                
                """;

        String expectedFormat = "【期望的JSON格式】\n";

        if (isList) {
            String formattedExample = exampleJson.replace("\n", "\n  ");
            return corePrinciples + outputRequirements + jsonFormatExample + expectedFormat + "[\n  " + formattedExample + "\n]";
        } else {
            return corePrinciples + outputRequirements + jsonFormatExample + expectedFormat + exampleJson;
        }
    }

    /**
     * 清理和修复AI生成的JSON内容
     *
     * @param rawContent AI生成的原始内容
     * @return 清理后的JSON内容
     */
    private String cleanJsonContent(String rawContent) {
        if (rawContent == null) {
            return "";
        }

        String cleaned = rawContent.trim();

        // 移除可能的代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        cleaned = cleaned.trim();

        // 预处理：移除JSON数组中的多余空行和不一致的格式
        cleaned = preprocessJsonArray(cleaned);

        // 修复常见的JSON格式问题
        cleaned = fixCommonJsonIssues(cleaned);

        // 如果还是解析失败，尝试更激进的修复
        try {
            JSONUtil.parse(cleaned);
        } catch (Exception e) {
            log.debug("常规修复后仍然解析失败，尝试激进修复: {}", e.getMessage());
            cleaned = aggressiveJsonFix(cleaned);
        }

        return cleaned;
    }

    /**
     * 预处理JSON数组，移除多余空行和格式不一致问题
     */
    private String preprocessJsonArray(String jsonContent) {
        if (!jsonContent.trim().startsWith("[")) {
            return jsonContent;
        }

        // 移除JSON数组中的多余空行
        String cleaned = jsonContent.replaceAll("\\n\\s*\\n", "\n");

        // 标准化JSON数组格式：移除对象之间的多余空格和换行
        cleaned = cleaned.replaceAll(",\\s*\\n\\s*\\n\\s*\\{", ",{");
        cleaned = cleaned.replaceAll(",\\s*\\n\\s*\\{", ",{");

        // 处理混合的引号格式问题
        cleaned = normalizeQuoteFormat(cleaned);

        return cleaned;
    }

    /**
     * 标准化引号格式，将混合的引号格式统一
     */
    private String normalizeQuoteFormat(String jsonContent) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);
            char prev = i > 0 ? jsonContent.charAt(i - 1) : 0;
            char next = i + 1 < jsonContent.length() ? jsonContent.charAt(i + 1) : 0;

            if (c == '\\' && next == '"') {
                // 处理转义引号
                // 跳过下一个引号
                if (!inString) {
                    // 在字符串外部遇到转义引号，转换为正常引号开始字符串
                    sb.append('"');
                    inString = true;
                } else {
                    // 在字符串内部，检查是否是结束引号
                    // 跳过下一个引号
                    if (isStringEndContext(jsonContent, i + 2)) {
                        // 这是字符串结束
                        sb.append('"');
                        inString = false;
                    } else {
                        // 这是字符串内部的转义引号，保持转义
                        sb.append("\\\"");
                    }
                }
                i++; // 跳过下一个引号
            } else if (c == '"' && prev != '\\') {
                if (!inString) {
                    // 开始字符串
                    inString = true;
                    sb.append(c);
                } else {
                    // 结束字符串
                    inString = false;
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 检查指定位置是否是字符串结束的上下文
     */
    private boolean isStringEndContext(String content, int afterQuoteIndex) {
        for (int i = afterQuoteIndex; i < content.length(); i++) {
            char c = content.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            return c == ':' || c == ',' || c == '}' || c == ']';
        }
        return true; // 到达文件末尾
    }

    /**
     * 修复常见的JSON格式问题
     *
     * @param jsonContent 原始JSON内容
     * @return 修复后的JSON内容
     */
    private String fixCommonJsonIssues(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            return jsonContent;
        }

        String fixed = jsonContent.trim();

        try {
            // 首先尝试解析，如果成功就不需要修复
            JSONUtil.parse(fixed);
            return fixed;
        } catch (Exception e) {
            log.debug("JSON解析失败，尝试修复: {}", e.getMessage());
        }

        // 修复常见问题

        // 1. 处理字符串中未转义的引号和换行符问题
        fixed = fixStringEscaping(fixed);

        // 2. 确保JSON对象/数组的完整性
        fixed = ensureJsonIntegrity(fixed);

        // 3. 移除可能的尾随逗号
        fixed = fixed.replaceAll(",\\s*([}\\]])", "$1");

        return fixed;
    }

    /**
     * 修复JSON字符串中的转义问题
     */
    private String fixStringEscaping(String jsonContent) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean inContentField = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);
            char prev = i > 0 ? jsonContent.charAt(i - 1) : 0;

            if (c == '"' && prev != '\\') {
                if (!inString) {
                    // 开始字符串
                    inString = true;
                    // 检查是否是 "content" 字段
                    if (i >= 10) {
                        String before = jsonContent.substring(Math.max(0, i - 10), i);
                        inContentField = before.contains("\"content\"");
                    }
                } else {
                    // 结束字符串
                    inString = false;
                    inContentField = false;
                }
                sb.append(c);
            } else if (inString && inContentField) {
                // 在 content 字段的字符串内，需要转义特殊字符
                if (c == '"' && prev != '\\') {
                    // 未转义的引号，需要转义
                    sb.append("\\\"");
                } else if (c == '\n') {
                    // 换行符转换为 \n
                    sb.append("\\n");
                } else if (c == '\r') {
                    // 回车符忽略
                    continue;
                } else if (c == '\t') {
                    // 制表符转换为 \t
                    sb.append("\\t");
                } else if (c == '\\' && i + 1 < jsonContent.length()) {
                    // 检查是否已经是转义序列
                    char next = jsonContent.charAt(i + 1);
                    if (next == '"' || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                        // 已经是有效的转义序列，保持不变
                        sb.append(c);
                    } else {
                        // 单独的反斜杠，需要转义
                        sb.append("\\\\");
                    }
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 确保JSON结构的完整性
     */
    private String ensureJsonIntegrity(String jsonContent) {
        StringBuilder trimmed = new StringBuilder(jsonContent.trim());

        // 确保以 [ 开始的内容以 ] 结束
        if (trimmed.toString().startsWith("[") && !trimmed.toString().endsWith("]")) {
            // 查找最后一个有效的对象结束位置
            int lastBrace = trimmed.lastIndexOf("}");
            if (lastBrace != -1) {
                trimmed = new StringBuilder(trimmed.substring(0, lastBrace + 1) + "\n]");
            }
        }

        // 确保以 { 开始的内容以 } 结束
        if (trimmed.toString().startsWith("{") && !trimmed.toString().endsWith("}")) {
            trimmed.append("}");
        }

        // 处理可能被截断的JSON数组
        if (trimmed.toString().startsWith("[")) {
            // 检查是否有未完成的对象
            int openBraces = 0;
            int closeBraces = 0;
            boolean inString = false;

            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                char prev = i > 0 ? trimmed.charAt(i - 1) : 0;

                if (c == '"' && prev != '\\') {
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') {
                        openBraces++;
                    } else if (c == '}') {
                        closeBraces++;
                    }
                }
            }

            // 如果有未闭合的大括号，尝试补全
            while (openBraces > closeBraces) {
                if (!trimmed.toString().endsWith("}") && !trimmed.toString().endsWith("]")) {
                    trimmed.append("}");
                } else {
                    // 在最后的 ] 前面加 }
                    if (trimmed.toString().endsWith("]")) {
                        trimmed = new StringBuilder(trimmed.substring(0, trimmed.length() - 1) + "}]");
                    } else {
                        trimmed.append("}");
                    }
                }
                closeBraces++;
            }

            // 确保以 ] 结束
            if (!trimmed.toString().endsWith("]")) {
                trimmed.append("]");
            }
        }

        return trimmed.toString();
    }

    /**
     * 激进的JSON修复方法，用于处理严重损坏的JSON
     */
    private String aggressiveJsonFix(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            return jsonContent;
        }

        String fixed = jsonContent.trim();

        // 1. 直接使用字符级别的修复，这样更可靠
        fixed = characterLevelFix(fixed);

        return fixed;
    }

    /**
     * 字符级别的JSON修复
     */
    private String characterLevelFix(String jsonContent) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean inFieldValue = false;
        int braceCount = 0;
        int bracketCount = 0;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);
            char prev = i > 0 ? jsonContent.charAt(i - 1) : 0;

            if (c == '"' && prev != '\\') {
                if (!inString) {
                    // 开始字符串
                    inString = true;
                    // 检查接下来是否是字段值（前面有冒号）
                    inFieldValue = isFieldValue(jsonContent, i);
                    sb.append(c);
                } else {
                    // 在字符串中遇到引号，需要判断是否是字符串结束
                    if (inFieldValue) {
                        // 在字段值中，检查是否真的是字符串结束
                        if (isStringEnd(jsonContent, i)) {
                            // 这是字段结束的引号
                            inString = false;
                            inFieldValue = false;
                            sb.append(c);
                        } else {
                            // 这是内容中的引号，需要转义
                            sb.append("\\\"");
                        }
                    } else {
                        // 不在字段值中，直接结束字符串
                        inString = false;
                        sb.append(c);
                    }
                }
            } else if (inString && inFieldValue) {
                // 在字段值字符串内，特殊处理
                if (c == '\n') {
                    sb.append("\\n");
                } else if (c == '\r') {
                    // 忽略回车符
                } else if (c == '\t') {
                    sb.append("\\t");
                } else if (c == '\\') {
                    // 检查下一个字符
                    if (i + 1 < jsonContent.length()) {
                        char next = jsonContent.charAt(i + 1);
                        if (next == '"' || next == '\\' || next == 'n' || next == 't' || next == 'r') {
                            // 已经是转义序列
                            sb.append(c);
                        } else {
                            // 需要转义反斜杠
                            sb.append("\\\\");
                        }
                    } else {
                        sb.append("\\\\");
                    }
                } else {
                    sb.append(c);
                }
            } else {
                // 不在字符串中，或在字段名中，处理结构字符
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                } else if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                }
                sb.append(c);
            }
        }

        // 确保结构完整
        while (braceCount > 0) {
            sb.append('}');
            braceCount--;
        }
        while (bracketCount > 0) {
            sb.append(']');
            bracketCount--;
        }

        return sb.toString();
    }

    /**
     * 检查当前引号位置是否是字段值的开始
     */
    private boolean isFieldValue(String jsonContent, int quoteIndex) {
        // 向前查找最近的冒号
        for (int i = quoteIndex - 1; i >= 0; i--) {
            char c = jsonContent.charAt(i);
            if (c == ':') {
                return true;
            } else if (c == ',' || c == '{' || c == '[') {
                return false;
            }
            // 忽略空白字符，继续查找
        }
        return false;
    }

    /**
     * 检查当前引号位置是否是字符串的结束
     */
    private boolean isStringEnd(String jsonContent, int quoteIndex) {
        // 向后查找，看下一个非空白字符是否是逗号、花括号或方括号
        for (int i = quoteIndex + 1; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            return c == ',' || c == '}' || c == ']';
        }
        // 如果到达文件末尾，认为是字符串结束
        return true;
    }

    /**
     * Logs prompt details at different levels for clarity and debugging.
     * - INFO level: Logs a summarized user prompt.
     * - DEBUG level: Logs the full, single-line system and user prompts.
     *
     * @param type         The type fromCode operation (e.g., "CHAT_LIST").
     * @param systemPrompt The system prompt.
     * @param userPrompt   The user prompt.
     */
    private void logPromptDetails(String type, String systemPrompt, String userPrompt) {
        // INFO log with a summary fromCode the user prompt
        log.info("[{}] AI Request: {}...", type, summarize(userPrompt));

        // DEBUG logs with full, single-line prompts
        if (log.isDebugEnabled()) {
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                log.debug("[{}] Full System Prompt: {}", type, sanitizeForSingleLineLogging(systemPrompt));
            }
            log.debug("[{}] Full User Prompt: {}", type, sanitizeForSingleLineLogging(userPrompt));
        }
    }

    /**
     * Summarizes a string to a fixed length for concise logging.
     *
     * @param text The text to summarize.
     * @return A summarized string.
     */
    private String summarize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String cleanedText = text.replaceAll("\\s+", " ").trim();
        if (cleanedText.length() <= USER_PROMPT_SUMMARY_LENGTH) {
            return cleanedText;
        }
        return cleanedText.substring(0, USER_PROMPT_SUMMARY_LENGTH);
    }

    /**
     * Sanitizes a string for single-line logging by replacing newlines with a visual representation.
     *
     * @param text The text to sanitize.
     * @return A single-line string.
     */
    private String sanitizeForSingleLineLogging(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\n", "\\n").replace("\r", "");
    }
}