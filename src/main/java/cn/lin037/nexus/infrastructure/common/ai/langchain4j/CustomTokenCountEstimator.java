package cn.lin037.nexus.infrastructure.common.ai.langchain4j;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.EncodingType;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.TokenCountEstimator;
import org.springframework.stereotype.Component;


/**
 * @author LinSanQi
 */
@Component
public class CustomTokenCountEstimator implements TokenCountEstimator {

    private final Encoding encoding;

    // 默认使用cl100k_base编码，适用于GPT-4和GPT-3.5-Turbo模型
    public CustomTokenCountEstimator() {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncoding(EncodingType.CL100K_BASE);
    }

    // 可以指定使用特定的编码类型
    public CustomTokenCountEstimator(EncodingType encodingType) {
        EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
        this.encoding = registry.getEncoding(encodingType);
    }

    @Override
    public int estimateTokenCountInText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return encoding.countTokens(text);
    }

    @Override
    public int estimateTokenCountInMessage(ChatMessage message) {
        if (message == null) {
            return 0;
        }

        // 基础token数 (每条消息格式开销)
        // 每个消息有3个格式化token开销
        int tokenCount = 3;

        // 根据消息类型计算token
        switch (message) {
            case SystemMessage systemMessage -> {
                // 系统消息: content
                tokenCount += estimateTokenCountInText("system");
                tokenCount += estimateTokenCountInText(systemMessage.text());
            }
            case UserMessage userMessage -> {
                // 用户消息: content
                tokenCount += estimateTokenCountInText("user");

                // 如果有name，额外计算
                tokenCount += estimateTokenCountInText(userMessage.singleText());
                if (userMessage.name() != null && !userMessage.name().isEmpty()) {
                    tokenCount += estimateTokenCountInText(userMessage.name());
                }
            }
            case AiMessage aiMessage -> {
                // AI消息: content
                tokenCount += estimateTokenCountInText("assistant");
                tokenCount += estimateTokenCountInText(aiMessage.text());
            }
            default ->
                // 其他类型消息
                    tokenCount += estimateTokenCountInText(message.toString());
        }

        return tokenCount;
    }

    @Override
    public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
        if (messages == null) {
            return 0;
        }

        int totalTokenCount = 0;

        // 汇总所有消息的token计数
        for (ChatMessage message : messages) {
            totalTokenCount += estimateTokenCountInMessage(message);
        }

        // 添加消息集合格式的开销tokens
        // 额外的3个token表示对话格式开销
        totalTokenCount += 3;

        return totalTokenCount;
    }
}
