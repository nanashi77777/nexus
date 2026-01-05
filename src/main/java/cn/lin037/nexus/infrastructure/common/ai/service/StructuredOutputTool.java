package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.StructResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.language.LanguageModel;

import java.util.List;

/**
 * 结构化输出工具接口
 *
 * @author LinSanQi
 */
public interface StructuredOutputTool {

    /**
     * 生成字符串输出
     *
     * @param model        语言模型
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 字符串输出
     */
    StructResult<String> generateStringOutput(ChatModel model, String systemPrompt, String userPrompt);

    /**
     * 生成结构化输出
     *
     * @param model         语言模型
     * @param systemPrompt  系统提示词
     * @param userPrompt    用户提示词
     * @param exampleOutput 示例输出
     * @param targetType    输出类型
     * @param <R>           输出类型泛型
     * @return 结构化输出对象
     * @deprecated 请使用 {@link #generateStructuredOutput(ChatModel, String, String, R, Class)} 替代
     */
    @Deprecated
    <R> StructResult<R> generateStructuredOutput(LanguageModel model, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType);

    /**
     * 批量生成结构化输出
     *
     * @param model         语言模型
     * @param systemPrompt  系统提示词
     * @param userPrompt    用户提示词
     * @param exampleOutput 示例输出
     * @param targetType    输出类型
     * @return 批量结构化输出对象列表
     * @deprecated 请使用 {@link #generateStructuredOutputList(ChatModel, String, String, R, Class)} 替代
     */
    @Deprecated
    <R> StructResult<List<R>> generateStructuredOutputList(LanguageModel model, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType);

    /**
     * 使用ChatModel生成结构化输出
     *
     * @param chatModel     对话模型
     * @param systemPrompt  系统提示词
     * @param userPrompt    用户提示词
     * @param exampleOutput 示例输出
     * @param targetType    目标类型
     * @param <R>           输出类型泛型
     * @return 结构化输出对象
     */
    <R> StructResult<R> generateStructuredOutput(ChatModel chatModel, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType);

    /**
     * 使用ChatModel批量生成结构化输出
     *
     * @param chatModel     对话模型
     * @param systemPrompt  系统提示词
     * @param userPrompt    用户提示词
     * @param exampleOutput 示例输出
     * @param targetType    目标类型
     * @param <R>           输出类型泛型
     * @return 批量结构化输出对象列表
     */
    <R> StructResult<List<R>> generateStructuredOutputList(ChatModel chatModel, String systemPrompt, String userPrompt, R exampleOutput, Class<R> targetType);
}
