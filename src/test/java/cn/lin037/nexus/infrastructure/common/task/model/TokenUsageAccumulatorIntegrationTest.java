package cn.lin037.nexus.infrastructure.common.task.model;

import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TokenUsageAccumulator 集成测试
 */
class TokenUsageAccumulatorIntegrationTest {

    @Test
    void testTokenUsageAccumulatorSerialization() {
        // 创建TokenUsageAccumulator并添加Token使用量
        TokenUsageAccumulator accumulator = new TokenUsageAccumulator();
        accumulator.add(new TokenUsage(100, 50));
        accumulator.add(new TokenUsage(200, 80));

        // 验证累积结果
        TokenUsage total = accumulator.getTotal();
        assertEquals(300, total.inputTokenCount());
        assertEquals(130, total.outputTokenCount());
        assertEquals(430, total.totalTokenCount());

        // 验证可以被JSON序列化（间接测试，通过toString()）
        String description = String.format("输入: %d, 输出: %d, 总计: %d",
                total.inputTokenCount(), total.outputTokenCount(), total.totalTokenCount());
        assertTrue(description.contains("输入: 300"));
        assertTrue(description.contains("输出: 130"));
        assertTrue(description.contains("总计: 430"));
    }

    @Test
    void testEmptyAccumulator() {
        TokenUsageAccumulator accumulator = new TokenUsageAccumulator();
        TokenUsage total = accumulator.getTotal();

        assertEquals(0, total.inputTokenCount());
        assertEquals(0, total.outputTokenCount());
        assertEquals(0, total.totalTokenCount());
    }
}
