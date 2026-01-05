package cn.lin037.nexus.infrastructure.adapter.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Agent工具执行响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResponse {

    /**
     * 执行是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 错误信息（当success为false时使用）
     */
    private String error;

    /**
     * 单个ID结果（如记忆ID、计划ID等）
     */
    private Long id;

    /**
     * 批量ID结果（如批量创建、删除的ID列表）
     */
    private List<Long> ids;

    /**
     * 总数
     */
    private Integer total;

    /**
     * 创建成功响应
     */
    public static ToolExecutionResponse success(String message) {
        return new ToolExecutionResponse(true, message, null, null, null, null);
    }

    /**
     * 创建成功响应（带单个ID）
     */
    public static ToolExecutionResponse success(String message, Long id) {
        return new ToolExecutionResponse(true, message, null, id, null, null);
    }

    /**
     * 创建成功响应（带批量ID）
     */
    public static ToolExecutionResponse success(String message, List<Long> ids) {
        return new ToolExecutionResponse(true, message, null, null, ids, ids != null ? ids.size() : 0);
    }

    /**
     * 创建失败响应
     */
    public static ToolExecutionResponse error(String error) {
        return new ToolExecutionResponse(false, null, error, null, null, null);
    }
}