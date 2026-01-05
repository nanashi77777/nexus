package cn.lin037.nexus.infrastructure.adapter.knowledge.dto;

import dev.langchain4j.model.output.TokenUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI生成结果包装类
 * 用于包装AI生成的结果和Token使用情况
 *
 * @param <T> 结果数据的类型
 * @author LinSanQi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationResult<T> {

    /**
     * 生成的结果数据
     */
    private T result;

    /**
     * Token使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 创建成功结果的静态工厂方法
     *
     * @param result     结果数据
     * @param tokenUsage Token使用情况
     * @param <T>        结果数据的类型
     * @return AI生成结果对象
     */
    public static <T> AiGenerationResult<T> success(T result, TokenUsage tokenUsage) {
        return new AiGenerationResult<>(result, tokenUsage);
    }

    /**
     * 创建空结果的静态工厂方法（用于生成失败的情况）
     *
     * @param tokenUsage Token使用情况
     * @param <T>        结果数据的类型
     * @return AI生成结果对象
     */
    public static <T> AiGenerationResult<T> empty(TokenUsage tokenUsage) {
        return new AiGenerationResult<>(null, tokenUsage);
    }
}
