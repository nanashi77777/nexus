package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import dev.langchain4j.model.output.TokenUsage;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Token使用量累加器
 * 用于统计AI调用过程中的Token使用情况
 * 线程安全实现
 *
 * @author LinSanQi
 */
@Getter
public class TokenUsageAccumulator {

    private final AtomicInteger inputTokens = new AtomicInteger(0);
    private final AtomicInteger outputTokens = new AtomicInteger(0);

    /**
     * 添加Token使用量（线程安全）
     *
     * @param tokenUsage Token使用情况
     */
    public void add(TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            this.inputTokens.addAndGet(tokenUsage.inputTokenCount() != null ? tokenUsage.inputTokenCount() : 0);
            this.outputTokens.addAndGet(tokenUsage.outputTokenCount() != null ? tokenUsage.outputTokenCount() : 0);
        }
    }

    /**
     * 获取累计的Token使用量
     *
     * @return 累计的Token使用量
     */
    public TokenUsage getTotal() {
        return new TokenUsage(inputTokens.get(), outputTokens.get());
    }

    /**
     * 重置累加器（线程安全）
     */
    public void reset() {
        this.inputTokens.set(0);
        this.outputTokens.set(0);
    }

    /**
     * 获取输入Token数量
     *
     * @return 输入Token数量
     */
    public int getInputTokens() {
        return inputTokens.get();
    }

    /**
     * 获取输出Token数量
     *
     * @return 输出Token数量
     */
    public int getOutputTokens() {
        return outputTokens.get();
    }
}
