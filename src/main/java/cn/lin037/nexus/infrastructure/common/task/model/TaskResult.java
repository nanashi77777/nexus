package cn.lin037.nexus.infrastructure.common.task.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 任务执行结果的封装对象。
 * <p>
 * TaskExecutor 的 execute 方法应返回此对象，以提供标准化的输出。
 *
 * @param <T> 审计负载的类型
 * @author LinSanQi
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResult<T> {

    /**
     * 对用户友好的结果或状态消息。
     * <p>
     * 例如："文档分析完成" 或 "无法连接到外部服务"。
     */
    private String userFriendlyMessage;

    /**
     * 用于审计的完整、结构化的数据负载。
     * <p>
     * 在成功时，这通常是任务的完整结果对象。
     * 在失败时，它可以是 null 或包含部分数据。
     * 此对象将被序列化为JSON并存储在 atAuditDetails 字段中。
     */
    private T auditPayload;


    /**
     * 创建一个表示成功的任务结果。
     *
     * @param userFriendlyMessage 对用户友好的消息
     * @param auditPayload        完整的结果数据
     * @param <T>                 结果数据的类型
     * @return TaskResult 实例
     */
    public static <T> TaskResult<T> success(String userFriendlyMessage, T auditPayload) {
        return new TaskResult<>(userFriendlyMessage, auditPayload);
    }

    /**
     * 创建一个表示成功的任务结果，使用默认的成功消息。
     *
     * @param auditPayload 完整的结果数据
     * @param <T>          结果数据的类型
     * @return TaskResult 实例
     */
    public static <T> TaskResult<T> success(T auditPayload) {
        return new TaskResult<>("任务成功完成。", auditPayload);
    }
}
