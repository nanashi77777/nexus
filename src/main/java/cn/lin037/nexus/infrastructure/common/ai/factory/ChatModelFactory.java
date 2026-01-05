
package cn.lin037.nexus.infrastructure.common.ai.factory;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.AiModuleTypeEnum;
import cn.lin037.nexus.infrastructure.common.ai.exception.AIInfraExceptionEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.OpenAiParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.QwenParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolSpecificationConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对话模型 (ChatModel) 工厂类
 *
 * @author Lin Ant
 * @since 2024/7/31
 */
@Slf4j
public class ChatModelFactory {

    /**
     * 根据提供的模型和供应商配置创建对话模型实例。
     *
     * @param aiModelConfig    模型配置
     * @param aiProviderConfig 供应商配置
     * @return ChatModel的实例
     */
    public static ChatModel createChatModel(AiModelConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        String channel = aiProviderConfig.getApcChannel();
        AiModuleTypeEnum moduleType = AiModuleTypeEnum.fromValue(channel);

        if (moduleType == null) {
            throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        }

        return switch (moduleType) {
            case OPEN_AI ->
                    createOpenAiChatModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), OpenAiParamConfig.class), aiProviderConfig);
            case DASH_SCOPE ->
                    createDashScopeChatModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), QwenParamConfig.class), aiProviderConfig);
//            default -> throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        };
    }

    /**
     * 根据提供的模型和供应商配置创建流式对话模型实例。
     *
     * @param aiModelConfig    模型配置
     * @param aiProviderConfig 供应商配置
     * @return StreamingChatModel的实例
     */
    public static StreamingChatModel createStreamingChatModel(AiModelConfig aiModelConfig, AiProviderConfig aiProviderConfig) {
        String channel = aiProviderConfig.getApcChannel();
        AiModuleTypeEnum moduleType = AiModuleTypeEnum.fromValue(channel);

        if (moduleType == null) {
            throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        }

        return switch (moduleType) {
            case OPEN_AI ->
                    createOpenAiStreamingChatModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), OpenAiParamConfig.class), aiProviderConfig);
            case DASH_SCOPE ->
                    createDashScopeStreamingChatModel(JSONUtil.toBean(aiModelConfig.getAmcConfig(), QwenParamConfig.class), aiProviderConfig);
//            default -> throw new InfrastructureException(AIInfraExceptionEnum.UNSUPPORTED_AI_MODULE_TYPE);
        };
    }

    private static ChatModel createOpenAiChatModel(OpenAiParamConfig paramConfig, AiProviderConfig providerConfig) {
        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.getApcBaseUrl())
                .apiKey(providerConfig.getApcApiKey())
                .organizationId(paramConfig.getOrganizationId())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature())
                .topP(paramConfig.getTopP())
                .maxTokens(paramConfig.getMaxTokens())
                .presencePenalty(paramConfig.getPresencePenalty())
                .frequencyPenalty(paramConfig.getFrequencyPenalty())
                .stop(paramConfig.getStop())
                .responseFormat(paramConfig.getResponseFormat())
                .seed(paramConfig.getSeed())
                .maxRetries(paramConfig.getMaxRetries())
                .logRequests(paramConfig.getLogRequests())
                .logResponses(paramConfig.getLogResponses())
                .strictTools(true)
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .toolSpecifications(toToolSpecifications(paramConfig.getToolSpecifications()))
                        .build())
                .build();
    }

    private static ChatModel createDashScopeChatModel(QwenParamConfig paramConfig, AiProviderConfig providerConfig) {
        return QwenChatModel.builder()
                .baseUrl(providerConfig.getApcBaseUrl())
                .apiKey(providerConfig.getApcApiKey())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature().floatValue())
                .topP(paramConfig.getTopP())
                .topK(paramConfig.getTopK())
                .enableSearch(paramConfig.getEnableSearch())
                .seed(paramConfig.getSeed())
                .repetitionPenalty(paramConfig.getRepetitionPenalty())
                .maxTokens(paramConfig.getMaxTokens())
                .stops(paramConfig.getStops())
                .isMultimodalModel(paramConfig.getIsMultimodalModel())
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .toolSpecifications(toToolSpecifications(paramConfig.getToolSpecifications()))
                        .responseFormat(paramConfig.getIsJsonResponseFormat() ? ResponseFormat.JSON : null)
                        .build())
                .build();
    }

    private static StreamingChatModel createOpenAiStreamingChatModel(OpenAiParamConfig paramConfig, AiProviderConfig providerConfig) {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(providerConfig.getApcBaseUrl())
                .apiKey(providerConfig.getApcApiKey())
                .organizationId(paramConfig.getOrganizationId())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature())
                .topP(paramConfig.getTopP())
                .maxTokens(paramConfig.getMaxTokens())
                .presencePenalty(paramConfig.getPresencePenalty())
                .frequencyPenalty(paramConfig.getFrequencyPenalty())
                .stop(paramConfig.getStop())
                .responseFormat(paramConfig.getResponseFormat())
                .seed(paramConfig.getSeed())
                .logRequests(paramConfig.getLogRequests())
                .logResponses(paramConfig.getLogResponses())
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .toolSpecifications(toToolSpecifications(paramConfig.getToolSpecifications()))
                        .build())
                .build();
    }

    private static StreamingChatModel createDashScopeStreamingChatModel(QwenParamConfig paramConfig, AiProviderConfig providerConfig) {
        return QwenStreamingChatModel.builder()
                .baseUrl(providerConfig.getApcBaseUrl())
                .apiKey(providerConfig.getApcApiKey())
                .modelName(paramConfig.getModelName())
                .temperature(paramConfig.getTemperature() != null ? paramConfig.getTemperature().floatValue() : null)
                .topP(paramConfig.getTopP())
                .topK(paramConfig.getTopK())
                .enableSearch(paramConfig.getEnableSearch())
                .seed(paramConfig.getSeed())
                .repetitionPenalty(paramConfig.getRepetitionPenalty())
                .maxTokens(paramConfig.getMaxTokens())
                .stops(paramConfig.getStops())
                .isMultimodalModel(paramConfig.getIsMultimodalModel())
                .defaultRequestParameters(ChatRequestParameters.builder()
                        .toolSpecifications(toToolSpecifications(paramConfig.getToolSpecifications()))
                        .responseFormat(paramConfig.getIsJsonResponseFormat() ? ResponseFormat.JSON : null)
                        .build())
                .build();
    }

    private static List<ToolSpecification> toToolSpecifications(List<ToolSpecificationConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return Collections.emptyList();
        }
        return configs.stream()
                .map(config -> ToolSpecification.builder()
                        .name(config.getName())
                        .description(config.getDescription())
                        .parameters(config.getParameters())
                        .build())
                .collect(Collectors.toList());
    }

}

