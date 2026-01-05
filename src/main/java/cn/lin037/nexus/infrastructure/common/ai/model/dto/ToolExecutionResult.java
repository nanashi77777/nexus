package cn.lin037.nexus.infrastructure.common.ai.model.dto;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.internal.Json;
import lombok.*;

/**
 * 增强版工具执行结果 DTO
 * 设计目标：
 * 1. 统一工具执行结果的数据结构，避免在不同层之间重复构造
 * 2. 集成 buildToolCallsContent 的文本格式化逻辑，实现统一格式化
 * 3. 支持丰富的元数据和状态管理
 * 4. 便于扩展，支持未来的工具执行能力增强
 *
 * @author Lin037
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResult {

    /**
     * 工具执行请求的原始信息
     */
    private ToolExecutionRequest request;

    /**
     * 工具执行的结果文本
     */
    private String resultText;

    /**
     * 工具执行状态
     */
    private ToolExecutionStatus status;

    /**
     * 错误信息（当状态为 ERROR 时）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 创建成功的工具执行结果
     */
    public static ToolExecutionResult success(ToolExecutionRequest request, String displayTextForModel) {
        return ToolExecutionResult.builder()
                .request(request)
                .resultText(displayTextForModel)
                .status(ToolExecutionStatus.SUCCESS)
                .build();
    }

    /**
     * 创建失败的工具执行结果
     */
    public static ToolExecutionResult error(ToolExecutionRequest request, String errorMessage) {
        return ToolExecutionResult.builder()
                .request(request)
                .resultText("工具执行失败: " + errorMessage)
                .status(ToolExecutionStatus.ERROR)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建跳过的工具执行结果
     */
    public static ToolExecutionResult skipped(ToolExecutionRequest request, String reason) {
        return ToolExecutionResult.builder()
                .request(request)
                .resultText("工具执行被跳过: " + reason)
                .status(ToolExecutionStatus.SKIPPED)
                .build();
    }

    @Override
    public String toString() {
        // 限制 errorMessage 的长度，避免日志过长
        String truncatedErrorMessage = (errorMessage != null && errorMessage.length() > 50)
                ? errorMessage.substring(0, 50) + "..."
                : errorMessage;

        return "ToolExecutionResult{" +
                "request=" + request +
                ", resultText='" + resultText + '\'' +
                ", status=" + status.getDesc() +
                ", errorMessage='" + truncatedErrorMessage + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }

    public ToolExecutionResultMessage toToolExecutionResultMessage() {
        String truncatedErrorMessage = (this.errorMessage != null && this.errorMessage.length() > 50)
                ? this.errorMessage.substring(0, 50) + "..."
                : this.errorMessage;

        String messageContent = "ToolExecutionResult{" +
                "resultText='" + this.resultText + '\'' +
                ", status=" + this.status.getDesc() +
                ", errorMessage='" + truncatedErrorMessage + '\'' +
                ", executionTimeMs=" + this.executionTimeMs +
                '}';

        return ToolExecutionResultMessage.from(this.request, messageContent);
    }

    public String toJsonStr() {
        return Json.toJson(this);
    }

    /**
     * 工具执行状态枚举
     */
    @Getter
    public enum ToolExecutionStatus {
        SUCCESS("执行成功"),
        ERROR("发生错误"),
        SKIPPED("用户选择跳过"),
        REJECTED("用户选择拒绝"),
        TIMEOUT("执行超时超时");

        private final String desc;

        ToolExecutionStatus(String desc) {
            this.desc = desc;
        }
    }

}
