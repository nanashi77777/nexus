package cn.lin037.nexus.infrastructure.common.ai.service;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.AiProviderInfoDTO;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;

import java.util.List;

/**
 * AI 核心服务接口
 *
 * @author lin037
 */
public interface AiCoreService {

    /**
     * 根据模型名称和用途，获取一个经过权重选择的语言模型实例。
     *
     * @param modelName 模型名称，例如 "gpt-4-turbo"
     * @param usedFor   用途标识，例如 "chat", "summary"
     * @return 最合适的 LanguageModel 实例
     * @deprecated 将逐步弃用，请使用 getChatModel 替代
     */
    @Deprecated
    LanguageModel getLanguageModel(String modelName, String usedFor);

    /**
     * 根据模型名称和用途，获取一个经过权重选择的流式语言模型实例。
     *
     * @param modelName 模型名称
     * @param usedFor   用途标识
     * @return 最合适的 StreamingLanguageModel 实例
     * @deprecated 将逐步弃用，请使用 getStreamingChatModel 替代
     */
    @Deprecated
    StreamingLanguageModel getStreamingLanguageModel(String modelName, String usedFor);

    /**
     * 根据模型名称和用途，获取一个经过权重选择的对话模型实例。
     *
     * @param modelName 模型名称
     * @param usedFor   用途标识
     * @return 最合适的 ChatModel 实例
     */
    ChatModel getChatModel(String modelName, String usedFor);

    /**
     * 根据模型名称和用途，获取一个经过权重选择的流式对话模型实例。
     *
     * @param modelName 模型名称
     * @param usedFor   用途标识
     * @return 最合适的 StreamingChatModel 实例
     */
    StreamingChatModel getStreamingChatModel(String modelName, String usedFor);

    /**
     * 根据模型名称，获取一个经过权重选择的嵌入模型实例。
     *
     * @param modelName 模型名称
     * @return 最合适的 EmbeddingModel 模型实例
     */
    EmbeddingModel getEmbeddingModel(String modelName);

    // ------------------------------------以下代码暂时不需要管------------------------------------

    /**
     * 获取所有 AI 模型配置的DTO列表。
     *
     * @return DTO 列表
     */
    List<AiModelConfig> listModels();

    /**
     * 获取所有 AI 服务商配置的DTO列表（不含敏感信息）。
     *
     * @return DTO 列表
     */
    List<AiProviderInfoDTO> listProviders();

    /**
     * 保存一个新的模型配置。
     *
     * @param config AiModelConfig 对象
     */
    void saveModelConfig(AiModelConfig config);

    /**
     * 更新一个已有的模型配置。
     * 此操作会清除该模型在缓存中的实例。
     *
     * @param config AiModelConfig 对象
     */
    void updateModelConfig(AiModelConfig config);

    /**
     * 根据 ID 删除一个模型配置。
     * 此操作会清除该模型在缓存中的实例。
     *
     * @param modelId 模型ID
     */
    void deleteModelConfig(Long modelId);

    /**
     * 保存一个新的服务商配置。
     *
     * @param config AiProviderConfig 对象
     */
    void saveProviderConfig(AiProviderConfig config);

    /**
     * 更新一个已有的服务商配置。
     * 此操作会清除该服务商下所有模型在缓存中的实例。
     *
     * @param config AiProviderConfig 对象
     */
    void updateProviderConfig(AiProviderConfig config);

    /**
     * 根据 ID 删除一个服务商配置。
     * 此操作会清除该服务商下所有模型在缓存中的实例。
     *
     * @param providerId 服务商ID
     */
    void deleteProviderConfig(Long providerId);
}
 