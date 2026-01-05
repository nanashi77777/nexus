package cn.lin037.nexus.infrastructure.common.task.executor;

import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;

/**
 * 任务执行器接口
 * <p>
 * 所有具体的任务逻辑都必须实现此接口，并作为一个Spring Bean注册到容器中。
 * 这是实现策略模式的关键，将任务的定义（数据）与任务的执行（逻辑）解耦。
 *
 * @param <P> 任务参数 (Parameters) 对象的类型
 * @param <R> 任务结果 (Result) 对象的类型, 即 TaskResult 中的 auditPayload 类型
 * @author LinSanQi
 */
public interface TaskExecutor<P, R> {

    /**
     * 返回该执行器能处理的任务类型的唯一标识符。
     * <p>
     * 这个字符串必须与提交任务时指定的 `taskType` 完全匹配。
     *
     * @return 任务类型字符串
     */
    String getTaskType();

    /**
     * 任务的核心执行逻辑。
     *
     * @param parameters 反序列化后的任务参数对象。
     * @param context    任务执行上下文，用于检查取消状态等。
     * @return 包含用户友好消息和详细审计负载的任务结果对象。
     * @throws Exception 如果任务执行失败，则抛出异常。
     */
    TaskResult<R> execute(P parameters, TaskContext context) throws Exception;

    /**
     * (可选) 当 `execute` 方法成功完成时调用的回调。
     * <p>
     * 这是一个默认方法，子类可以选择性地覆盖它以实现成功后的处理逻辑。
     *
     * @param parameters 反序列化后的任务参数对象。
     * @param result     `execute` 方法返回的 `TaskResult` 对象。
     */
    default void onCompletion(P parameters, TaskResult<R> result) {
        // 默认无操作
    }

    /**
     * (可选) 当 `execute` 方法抛出异常时调用的回调。
     * <p>
     * 这是一个默认方法，子类可以选择性地覆盖它以实现失败后的清理或通知逻辑。
     *
     * @param parameters 反序列化后的任务参数对象。
     * @param exception  `execute` 方法抛出的异常。
     */
    default void onFailure(P parameters, Exception exception) {
        // 默认无操作
    }

    /**
     * 获取任务参数的类类型。
     * 用于在调度器中进行类型安全的JSON反序列化。
     *
     * @return 参数的Class对象
     */
    Class<P> getParametersType();

    /**
     * 获取任务结果的类类型。
     * 用于在调度器中进行类型安全的JSON反序列化。
     *
     * @return 结果的Class对象
     */
    Class<R> getResultType();
}