package cn.lin037.nexus.infrastructure.common.ai.factory;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.AiModuleTypeEnum;
import cn.lin037.nexus.infrastructure.common.ai.exception.AIInfraExceptionEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.OpenAiParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.QwenParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenLanguageModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingLanguageModel;
import lombok.extern.slf4j.Slf4j;

/**
 * 大语言模型（LLM）工厂类
 *
 * @author Lin Ant
 * @since 2024/7/28
 */
@Slf4j
public class LLMFactory {
    /**
     * 根据提供的模型和供应商配置创建语言模型实例。
     *
     * @param aiModelConfig    模型配置
     * @param aiProviderConfig 供应商配置
     * @return LanguageModel的实例
     */
    public static LanguageModel create(AiModelConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        String channel = aiProviderConfig.getApcChannel();
        AiModuleTypeEnum moduleType = AiModuleTypeEnum.fromValue(channel);

        if (moduleType == null) {
            throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        }

        return switch (moduleType) {
            case OPEN_AI ->
                    createOpenAiLanguageModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), OpenAiParamConfig.class), aiProviderConfig);
            case DASH_SCOPE ->
                    createDashScopeLanguageModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), QwenParamConfig.class), aiProviderConfig);
            default -> throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        };
    }

    public static StreamingLanguageModel createStreaming(AiModelConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        String channel = aiProviderConfig.getApcChannel();
        AiModuleTypeEnum moduleType = AiModuleTypeEnum.fromValue(channel);

        if (moduleType == null) {
            throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        }

        return switch (moduleType) {
            case OPEN_AI ->
                    createOpenAiStreamingLanguageModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), OpenAiParamConfig.class), aiProviderConfig);
            case DASH_SCOPE ->
                    createDashScopeStreamingLanguageModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), QwenParamConfig.class), aiProviderConfig);
            default -> throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        };
    }

    public static EmbeddingModel createEmbeddingModel(AiModelConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        String channel = aiProviderConfig.getApcChannel();
        AiModuleTypeEnum moduleType = AiModuleTypeEnum.fromValue(channel);

        if (moduleType == null) {
            throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        }

        return switch (moduleType) {
            case OPEN_AI ->
                    createOpenAiEmbeddingModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), OpenAiParamConfig.class), aiProviderConfig);
            case DASH_SCOPE ->
                    createDashScopeEmbeddingModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), QwenParamConfig.class), aiProviderConfig);
            default -> throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        };
    }

    private static LanguageModel createOpenAiLanguageModel(OpenAiParamConfig paramConfig, AiProviderConfig aiProviderConfig) {
        // 创建 OpenAI 语言模型构建器并配置基础参数
        return OpenAiLanguageModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .organizationId(paramConfig.getOrganizationId())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature())
                .maxRetries(paramConfig.getMaxRetries())
                .logRequests(true)
                .logResponses(true).build();
    }

    private static LanguageModel createDashScopeLanguageModel(QwenParamConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        // 创建 DashScope 语言模型构建器并配置基础参数
        return QwenLanguageModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .modelName(aiModelConfig.getModelName())
                .temperature(aiModelConfig.getTemperature().floatValue())
                .topP(aiModelConfig.getTopP())
                .maxTokens(aiModelConfig.getMaxTokens())
                .repetitionPenalty(aiModelConfig.getRepetitionPenalty())
                .topK(aiModelConfig.getTopK())
                .seed(aiModelConfig.getSeed())
                .enableSearch(aiModelConfig.getEnableSearch())
                .stops(aiModelConfig.getStops())
                .build();
    }

    private static StreamingLanguageModel createOpenAiStreamingLanguageModel(OpenAiParamConfig paramConfig, AiProviderConfig aiProviderConfig) {
        // 创建 OpenAI 语言模型构建器并配置基础参数
        return OpenAiStreamingLanguageModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .organizationId(paramConfig.getOrganizationId())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature())
                .logRequests(true)
                .logResponses(true).build();
    }

    private static StreamingLanguageModel createDashScopeStreamingLanguageModel(QwenParamConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        // 创建 DashScope 语言模型构建器并配置基础参数
        return QwenStreamingLanguageModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .modelName(aiModelConfig.getModelName())
                .temperature(aiModelConfig.getTemperature().floatValue())
                .topP(aiModelConfig.getTopP())
                .maxTokens(aiModelConfig.getMaxTokens())
                .repetitionPenalty(aiModelConfig.getRepetitionPenalty())
                .topK(aiModelConfig.getTopK())
                .seed(aiModelConfig.getSeed())
                .enableSearch(aiModelConfig.getEnableSearch())
                .stops(aiModelConfig.getStops()).build();
    }

    private static EmbeddingModel createOpenAiEmbeddingModel(OpenAiParamConfig paramConfig, AiProviderConfig aiProviderConfig) {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .organizationId(paramConfig.getOrganizationId())
                .modelName(paramConfig.getModelName())
                .dimensions(paramConfig.getDimensions())
                .user(paramConfig.getUser())
                .maxRetries(paramConfig.getMaxRetries())
                .logRequests(paramConfig.getLogRequests())
                .logResponses(paramConfig.getLogResponses())
                .build();
    }

    private static EmbeddingModel createDashScopeEmbeddingModel(QwenParamConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        return QwenEmbeddingModel.builder()
                .baseUrl(aiProviderConfig.getApcBaseUrl())
                .apiKey(aiProviderConfig.getApcApiKey())
                .modelName(aiModelConfig.getModelName())
                .dimension(aiModelConfig.getDimension())
                .build();
    }
}