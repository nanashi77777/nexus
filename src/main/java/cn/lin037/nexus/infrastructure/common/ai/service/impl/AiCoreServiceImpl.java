package cn.lin037.nexus.infrastructure.common.ai.service.impl;

import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.exception.AIInfraExceptionEnum;
import cn.lin037.nexus.infrastructure.common.ai.factory.ChatModelFactory;
import cn.lin037.nexus.infrastructure.common.ai.factory.LLMFactory;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.AiProviderInfoDTO;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.lin037.nexus.infrastructure.common.ai.repository.AiModelConfigRepository;
import cn.lin037.nexus.infrastructure.common.ai.repository.AiProviderConfigRepository;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * AI 核心服务实现
 *
 * @author lin037
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiCoreServiceImpl implements AiCoreService {

    /**
     * 负载均衡统计的时间窗口
     */
    private static final long TIME_WINDOW_MS = 60 * 1000;
    private final AiModelConfigRepository modelConfigRepository;
    private final AiProviderConfigRepository providerConfigRepository;
    /**
     * 模型实例缓存。
     * Key: "modelName:usedFor"
     * Value: 包装了模型实例和统计信息的列表
     */
    private final Map<String, List<ModelInstanceWrapper>> modelCache = new ConcurrentHashMap<>();
    private final PlatformTransactionManager transactionManager;

    @Nullable
    private static ModelInstanceWrapper getModelInstanceWrapper(List<ModelInstanceWrapper> candidates) {
        ModelInstanceWrapper bestChoice = null;
        double maxWeight = -Double.MAX_VALUE;

        for (int i = 0; i < candidates.size(); i++) {
            ModelInstanceWrapper current = candidates.get(i);
            // 优先级由列表顺序决定，索引越小，优先级越高。我们用 (size - i) 来表示优先级分数。
            double priorityScore = candidates.size() - i;
            long usage = current.getRecentUsageCount();

            // 简单的权重算法
            double weight = priorityScore - usage;

            if (weight > maxWeight) {
                maxWeight = weight;
                bestChoice = current;
            }
        }
        return bestChoice;
    }

    @Override
    public LanguageModel getLanguageModel(String modelName, String usedFor) {
        // 预加载并缓存模型定义，如果它们尚未加载
        getOrLoadModelInstances(modelName, usedFor);
        // 返回一个代理，该代理将在每次调用时执行负载均衡
        return new LoadBalancedLanguageModel(this, modelName, usedFor);
    }

    @Override
    public StreamingLanguageModel getStreamingLanguageModel(String modelName, String usedFor) {
        // 预加载并缓存模型定义
        getOrLoadModelInstances(modelName, usedFor);
        // 返回流式模型的代理
        return new LoadBalancedStreamingLanguageModel(this, modelName, usedFor);
    }

    @Override
    public ChatModel getChatModel(String modelName, String usedFor) {
        getOrLoadModelInstances(modelName, usedFor);
        return new LoadBalancedChatModel(this, modelName, usedFor);
    }

    @Override
    public StreamingChatModel getStreamingChatModel(String modelName, String usedFor) {
        getOrLoadModelInstances(modelName, usedFor);
        return new LoadBalancedStreamingChatModel(this, modelName, usedFor);
    }

    @Override
    public EmbeddingModel getEmbeddingModel(String modelName) {
        // 默认为 text-embedding-ada-002
        if (modelName == null) {
            modelName = "text-embedding-v4";
        }
        // 预加载并缓存模型定义
        getOrLoadModelInstances(modelName, "embedding");
        return new LoadBalancedEmbeddingModel(this, modelName, "embedding");
    }

    /**
     * 为常规语言模型选择一个具体的、可用的提供商实例。
     * 该方法实现了负载均衡的核心逻辑。
     *
     * @param modelName 模型名称
     * @param usedFor   使用场景
     * @return 一个具体的 LanguageModel 实例
     */
    public LanguageModel selectLanguageModel(String modelName, String usedFor) {
        List<ModelInstanceWrapper> candidates = getOrLoadModelInstances(modelName, usedFor);
        ModelInstanceWrapper bestChoice = selectBestModel(candidates);
        bestChoice.recordUsage();
        log.debug("已选择模型 '{}' 用于 '{}'。当前时间窗口内使用次数: {}", bestChoice.config.getAmcName(), usedFor, bestChoice.getRecentUsageCount());
        return bestChoice.languageModel;
    }

    /**
     * 为流式语言模型选择一个具体的、可用的提供商实例。
     *
     * @param modelName 模型名称
     * @param usedFor   使用场景
     * @return 一个具体的 StreamingLanguageModel 实例
     */
    public StreamingLanguageModel selectStreamingLanguageModel(String modelName, String usedFor) {
        List<ModelInstanceWrapper> candidates = getOrLoadModelInstances(modelName, usedFor);
        ModelInstanceWrapper bestChoice = selectBestModel(candidates);
        bestChoice.recordUsage();
        log.debug("已选择流式模型 '{}' 用于 '{}'。当前时间窗口内使用次数: {}", bestChoice.config.getAmcName(), usedFor, bestChoice.getRecentUsageCount());
        return bestChoice.streamingLanguageModel;
    }

    public ChatModel selectChatModel(String modelName, String usedFor) {
        List<ModelInstanceWrapper> candidates = getOrLoadModelInstances(modelName, usedFor);
        ModelInstanceWrapper bestChoice = selectBestModel(candidates);
        bestChoice.recordUsage();
        log.debug("已选择对话模型 '{}' 用于 '{}'。当前时间窗口内使用次数: {}", bestChoice.config.getAmcName(), usedFor, bestChoice.getRecentUsageCount());
        return bestChoice.chatModel;
    }

    public StreamingChatModel selectStreamingChatModel(String modelName, String usedFor) {
        List<ModelInstanceWrapper> candidates = getOrLoadModelInstances(modelName, usedFor);
        ModelInstanceWrapper bestChoice = selectBestModel(candidates);
        bestChoice.recordUsage();
        log.debug("已选择流式对话模型 '{}' 用于 '{}'。当前时间窗口内使用次数: {}", bestChoice.config.getAmcName(), usedFor, bestChoice.getRecentUsageCount());
        return bestChoice.streamingChatModel;
    }

    /**
     * 为嵌入模型选择一个具体的、可用的提供商实例。
     *
     * @param modelName 模型名称
     * @param usedFor   使用场景
     * @return 一个具体的 EmbeddingModel 实例
     */
    public EmbeddingModel selectEmbeddingModel(String modelName, String usedFor) {
        List<ModelInstanceWrapper> candidates = getOrLoadModelInstances(modelName, usedFor);
        ModelInstanceWrapper bestChoice = selectBestModel(candidates);
        bestChoice.recordUsage();
        log.debug("已选择嵌入模型 '{}' 用于 '{}'", bestChoice.config.getAmcName(), usedFor);
        return bestChoice.embeddingModel;
    }

    private List<ModelInstanceWrapper> getOrLoadModelInstances(String modelName, String usedFor) {
        String cacheKey = modelName + ":" + (usedFor == null ? "default" : usedFor);
        // 使用 computeIfAbsent 保证原子性操作，避免并发下重复加载
        return modelCache.computeIfAbsent(cacheKey, k -> {
            log.info("缓存未命中，开始从数据库加载模型。key: '{}'", k);
            List<AiModelConfig> configs = modelConfigRepository.findByModelNameAndUsedFor(modelName, usedFor, List.of(GeneralStatusEnum.ACTIVE));
            if (configs == null || configs.isEmpty()) {
                log.warn("未找到匹配的模型配置。key: '{}'", k);
                return Collections.emptyList();
            }
            return configs.stream()
                    .map(this::createModelInstanceWrapper)
                    // 现在，如果createModelInstanceWrapper失败，它会抛出异常，因此这里只会收到成功的实例
                    .collect(Collectors.toList());
        });
    }

    /**
     * 创建模型实例包装器。
     * 如果创建失败，此方法将把模型状态更新为INACTIVE，然后向上抛出异常。
     *
     * @param config AI模型配置对象
     * @return 包含ModelInstanceWrapper的Optional对象
     */
    private ModelInstanceWrapper createModelInstanceWrapper(AiModelConfig config) {
        try {
            // 获取并检查服务商配置
            Optional<AiProviderConfig> providerConfigOpt = providerConfigRepository.findById(config.getAmcProviderId());
            if (providerConfigOpt.isEmpty()) {
                throw new InfrastructureException(AIInfraExceptionEnum.MODEL_INSTANTIATION_FAILED, "未找到服务商配置，模型ID: " + config.getAmcId());
            }

            AiProviderConfig providerConfig = providerConfigOpt.get();
            if (!Objects.equals(providerConfig.getApcStatus(), GeneralStatusEnum.ACTIVE.getCode())) {
                throw new InfrastructureException(AIInfraExceptionEnum.MODEL_INSTANTIATION_FAILED, "服务商未激活，模型ID: " + config.getAmcId());
            }

            LanguageModel lm = null;
            StreamingLanguageModel slm = null;
            ChatModel cm = null;
            StreamingChatModel scm = null;
            EmbeddingModel em = null;

            // 根据模型类型创建相应的实例
            if ("EMBEDDING".equalsIgnoreCase(config.getAmcUsedFor())) {
                em = LLMFactory.createEmbeddingModel(config, providerConfig);
            } else {
                lm = LLMFactory.create(config, providerConfig);
                slm = LLMFactory.createStreaming(config, providerConfig);
                cm = ChatModelFactory.createChatModel(config, providerConfig);
                scm = ChatModelFactory.createStreamingChatModel(config, providerConfig);
            }
            // 返回一个健康的实例包装器
            return new ModelInstanceWrapper(lm, slm, cm, scm, em, config);

        } catch (Exception e) {
            log.error("创建模型实例失败，将自动停用该模型。模型ID: {}", config.getAmcId(), e);
            // 失败时，自动停用模型
            setModelStatusToInactive(config);
            // 向上抛出原始异常，立即向上层报告失败
            throw new InfrastructureException(AIInfraExceptionEnum.MODEL_INSTANTIATION_FAILED, e);
        }
    }

    /**
     * 根据权重选择最佳模型。
     */
    private ModelInstanceWrapper selectBestModel(List<ModelInstanceWrapper> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            // 如果加载后列表为空，说明所有可用模型都实例化失败并被停用了
            throw new InfrastructureException(AIInfraExceptionEnum.NO_AVAILABLE_MODEL_FOUND, "所有可用模型均实例化失败或无可用模型。");
        }

        ModelInstanceWrapper bestChoice = getModelInstanceWrapper(candidates);

        if (bestChoice == null) {
            // 理论上不应该发生
            throw new InfrastructureException(AIInfraExceptionEnum.NO_AVAILABLE_MODEL_FOUND);
        }

        return bestChoice;
    }

    /**
     * 将指定的模型配置在数据库中的状态更新为“停用”。
     * 此方法使用编程式事务，因此无需@Transactional注解。
     */
    public void setModelStatusToInactive(AiModelConfig config) {
        // 使用编程式事务确保状态更新的原子性
        new TransactionTemplate(transactionManager).execute(status -> {
            // 重新从数据库获取最新状态，避免脏数据
            AiModelConfig latestConfig = modelConfigRepository.findById(config.getAmcId()).orElse(null);
            if (latestConfig != null && !Objects.equals(latestConfig.getAmcStatus(), GeneralStatusEnum.INACTIVE.getCode())) {
                latestConfig.setAmcStatus(GeneralStatusEnum.INACTIVE.getCode());
                modelConfigRepository.updateById(latestConfig.getAmcId(), latestConfig);
                // 立即从缓存中移除，确保下次加载时会重新从数据库读取（届时会因状态不是ACTIVE而被过滤掉）
                invalidateModelCache(latestConfig);
                log.warn("模型 {} (ID: {}) 已被自动标记为停用状态并从缓存中移除。", latestConfig.getAmcName(), latestConfig.getAmcId());
            }
            return null;
        });
    }

    /**
     * 获取所有模型信息
     * TODO：没什么必要的一个方法，后续应该改成VO，只提供模型的名称和用途信息，外加添加上筛选功能
     * 该方法返回所有模型信息，包括模型名称、用途、配置、状态、创建时间和更新时间等信息。
     */
    @Override
    public List<AiModelConfig> listModels() {
        return modelConfigRepository.findAll();
    }

    @Override
    public List<AiProviderInfoDTO> listProviders() {
        return providerConfigRepository.findAll().stream()
                .map(config -> {
                    AiProviderInfoDTO dto = new AiProviderInfoDTO();
                    // 明确排除API Key
                    BeanUtils.copyProperties(config, dto, "apcApiKey");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveModelConfig(AiModelConfig config) {
        // 新保存的模型默认为激活状态
        config.setAmcStatus(GeneralStatusEnum.ACTIVE.getCode());
        modelConfigRepository.save(config);
        // 使缓存失效，以便下次请求时重新加载候选列表
        invalidateModelCache(config);
    }

    @Override
    @Transactional
    public void updateModelConfig(AiModelConfig config) {
        modelConfigRepository.updateById(config.getAmcId(), config);
        // 使缓存失效
        invalidateModelCache(config);
    }

    @Override
    @Transactional
    public void deleteModelConfig(Long modelId) {
        // 删除前需要先查询，以获取用于定位缓存key的`modelName`和`usedFor`
        modelConfigRepository.findById(modelId).ifPresent(config -> {
            modelConfigRepository.deleteById(modelId);
            invalidateModelCache(config);
        });
    }

    @Override
    public void saveProviderConfig(AiProviderConfig config) {
        providerConfigRepository.save(config);
    }

    @Override
    @Transactional
    public void updateProviderConfig(AiProviderConfig config) {
        providerConfigRepository.save(config);
        // 服务商更新，需要移除其下所有模型的缓存
        invalidateCacheForProvider(config.getApcId());
    }

    @Override
    @Transactional
    public void deleteProviderConfig(Long providerId) {
        providerConfigRepository.deleteById(providerId);
        // 服务商删除，需要移除其下所有模型的缓存
        invalidateCacheForProvider(providerId);
    }

    private void invalidateModelCache(AiModelConfig config) {
        String cacheKey = config.getAmcName() + ":" + (config.getAmcUsedFor() == null ? "default" : config.getAmcUsedFor());
        modelCache.remove(cacheKey);
        log.info("模型配置变更，缓存已失效。key: '{}'", cacheKey);
    }

    private void invalidateCacheForProvider(Long providerId) {
        List<AiModelConfig> modelsToInvalidate = modelConfigRepository.findByProviderId(providerId);
        modelsToInvalidate.forEach(this::invalidateModelCache);
        log.info("服务商ID: {} 的所有模型缓存已失效", providerId);
    }

    /**
     * 内部类，用于包装模型实例及其元数据
     */
    @AllArgsConstructor
    private static class ModelInstanceWrapper {
        final LanguageModel languageModel;
        final StreamingLanguageModel streamingLanguageModel;
        final ChatModel chatModel;
        final StreamingChatModel streamingChatModel;
        final EmbeddingModel embeddingModel;
        final AiModelConfig config;
        // 使用 Deque 存储最近一次使用的时间戳
        final Deque<Long> usageTimestamps = new ConcurrentLinkedDeque<>();

        /**
         * 记录一次新的使用
         */
        void recordUsage() {
            usageTimestamps.addLast(System.currentTimeMillis());
        }

        /**
         * 获取指定时间窗口内的使用次数
         *
         * @return 最近的使用次数
         */
        long getRecentUsageCount() {
            long cutoff = System.currentTimeMillis() - AiCoreServiceImpl.TIME_WINDOW_MS;
            // 清理掉过时的时间戳以防内存泄漏
            while (!usageTimestamps.isEmpty() && usageTimestamps.peekFirst() < cutoff) {
                usageTimestamps.pollFirst();
            }
            return usageTimestamps.size();
        }
    }

    /**
     * 负载均衡代理，用于 LanguageModel。
     * 每次调用 generate 方法时，都会向 AiCoreService 请求当前最佳的模型实例。
     */
    private record LoadBalancedLanguageModel(AiCoreServiceImpl aiCoreService, String modelName,
                                             String usedFor) implements LanguageModel {
        @Override
        public Response<String> generate(String prompt) {
            return aiCoreService.selectLanguageModel(modelName, usedFor).generate(prompt);
        }
    }

    /**
     * 负载均衡代理，用于 StreamingLanguageModel。
     */
    private record LoadBalancedStreamingLanguageModel(AiCoreServiceImpl aiCoreService, String modelName,
                                                      String usedFor) implements StreamingLanguageModel {
        @Override
        public void generate(String prompt, dev.langchain4j.model.StreamingResponseHandler<String> handler) {
            aiCoreService.selectStreamingLanguageModel(modelName, usedFor).generate(prompt, handler);
        }
    }

    private record LoadBalancedChatModel(AiCoreServiceImpl aiCoreService, String modelName,
                                         String usedFor) implements ChatModel {
        @Override
        public ChatResponse chat(ChatRequest chatRequest) {
            return aiCoreService.selectChatModel(modelName, usedFor).chat(chatRequest);
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            return aiCoreService.selectChatModel(modelName, usedFor).doChat(chatRequest);
        }

        @Override
        public String chat(String userMessage) {
            return aiCoreService.selectChatModel(modelName, usedFor).chat(userMessage);
        }

        @Override
        public ChatResponse chat(ChatMessage... messages) {
            return aiCoreService.selectChatModel(modelName, usedFor).chat(messages);
        }

        @Override
        public ChatResponse chat(List<ChatMessage> messages) {
            return aiCoreService.selectChatModel(modelName, usedFor).chat(messages);
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).defaultRequestParameters();
        }

        @Override
        public List<ChatModelListener> listeners() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).listeners();
        }

        @Override
        public ModelProvider provider() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).provider();
        }

        @Override
        public Set<Capability> supportedCapabilities() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).supportedCapabilities();
        }
    }

    private record LoadBalancedStreamingChatModel(AiCoreServiceImpl aiCoreService, String modelName,
                                                  String usedFor) implements StreamingChatModel {
        @Override
        public void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            aiCoreService.selectStreamingChatModel(modelName, usedFor).chat(chatRequest, handler);
        }

        @Override
        public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            aiCoreService.selectStreamingChatModel(modelName, usedFor).doChat(chatRequest, handler);
        }

        @Override
        public void chat(String userMessage, StreamingChatResponseHandler handler) {
            aiCoreService.selectStreamingChatModel(modelName, usedFor).chat(userMessage, handler);
        }

        @Override
        public void chat(List<ChatMessage> messages, StreamingChatResponseHandler handler) {
            aiCoreService.selectStreamingChatModel(modelName, usedFor).chat(messages, handler);
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).defaultRequestParameters();
        }

        @Override
        public List<ChatModelListener> listeners() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).listeners();
        }

        @Override
        public ModelProvider provider() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).provider();
        }

        @Override
        public Set<Capability> supportedCapabilities() {
            return aiCoreService.selectStreamingChatModel(modelName, usedFor).supportedCapabilities();
        }
    }

    /**
     * 负载均衡代理，用于 EmbeddingModel。
     */
    private record LoadBalancedEmbeddingModel(AiCoreServiceImpl aiCoreService, String modelName,
                                              String usedFor) implements EmbeddingModel {

        @Override
        public Response<Embedding> embed(String text) {
            return aiCoreService.selectEmbeddingModel(modelName, usedFor).embed(text);
        }

        @Override
        public Response<Embedding> embed(TextSegment textSegment) {
            return aiCoreService.selectEmbeddingModel(modelName, usedFor).embed(textSegment);
        }

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            return aiCoreService.selectEmbeddingModel(modelName, usedFor).embedAll(textSegments);
        }
    }
}
