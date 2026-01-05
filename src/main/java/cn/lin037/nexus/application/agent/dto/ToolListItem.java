package cn.lin037.nexus.application.agent.dto;

import cn.lin037.nexus.application.agent.enums.ToolItemStatus;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具列表项数据传输对象
 * 用于存储工具列表中每个工具的执行状态和相关信息
 *
 * @author Lin037
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolListItem {

    /**
     * 工具项唯一标识ID
     * 使用雪花算法生成，用于标识工具列表中的每个工具项
     */
    private Long id;

    /**
     * 工具名称
     * 存储工具名称，用于显示工具名称
     */
    private String name;

    /**
     * 工具参数
     * 存储工具执行时需要的参数信息
     */
    private String arguments;

    /**
     * 工具执行状态
     * @see ToolItemStatus
     */
    private ToolItemStatus status;

    /**
     * 状态消息
     * 根据不同状态存储相应信息：
     * - 执行成功时：成功消息
     * - 执行失败时：错误消息
     * - 等待授权时：授权提示消息
     * - 其他状态：相关描述信息
     */
    private String message;

    /**
     * 执行耗时（毫秒）
     * 工具执行的总耗时，未执行或正在执行时为null
     */
    private Long executionTimeMs;

    /**
     * 工具开始执行时间戳
     * 用于计算执行耗时，执行开始时设置
     */
    private Long startTimeMs;

    /**
     * 创建工具列表项
     *
     * @param id 工具项ID
     * @param toolExecutionRequest 工具执行请求
     * @return 初始状态的工具列表项
     */
    public static ToolListItem create(Long id, ToolExecutionRequest toolExecutionRequest) {
        return ToolListItem.builder()
                .id(id)
                .name(toolExecutionRequest.name())
                .arguments(toolExecutionRequest.arguments())
                .status(ToolItemStatus.NOT_EXECUTED)
                .message("工具尚未执行")
                .build();
    }

    /**
     * 标记工具开始执行
     */
    public void markExecuting() {
        this.status = ToolItemStatus.EXECUTING;
        this.message = "工具正在执行中";
        this.startTimeMs = System.currentTimeMillis();
    }

    /**
     * 标记工具执行成功
     *
     * @param successMessage 成功消息
     */
    public void markSuccess(String successMessage) {
        this.status = ToolItemStatus.SUCCESS;
        this.message = successMessage;
        if (this.startTimeMs != null) {
            this.executionTimeMs = System.currentTimeMillis() - this.startTimeMs;
        }
    }

    /**
     * 标记工具执行失败
     *
     * @param errorMessage 错误消息
     */
    public void markFailed(String errorMessage) {
        this.status = ToolItemStatus.FAILED;
        this.message = errorMessage;
        if (this.startTimeMs != null) {
            this.executionTimeMs = System.currentTimeMillis() - this.startTimeMs;
        }
    }

    /**
     * 标记工具等待授权
     */
    public void markWaitingAuthorization() {
        this.status = ToolItemStatus.WAITING_AUTHORIZATION;
        this.message = "工具需要用户授权才能执行";
    }

    /**
     * 标记工具被用户取消
     */
    public void markCancelled() {
        this.status = ToolItemStatus.USER_REJECTED;
        this.message = "工具已被用户取消";
        if (this.startTimeMs != null) {
            this.executionTimeMs = System.currentTimeMillis() - this.startTimeMs;
        }
    }
}