package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import dev.langchain4j.model.output.TokenUsage;
import lombok.Data;

@Data
public class StructResult<T> {

    private String content;
    private T result;
    private TokenUsage tokenUsage;

    public static <T> StructResult<T> of(String content, T result, TokenUsage tokenUsage) {
        StructResult<T> structResult = new StructResult<>();
        structResult.setContent(content);
        structResult.setResult(result);
        structResult.setTokenUsage(tokenUsage);
        return structResult;
    }
}
