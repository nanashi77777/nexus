package cn.lin037.nexus.infrastructure.common.task.executor;

/**
 * 任务执行上下文接口
 * <p>
 * 在任务执行期间，此对象被传递给 {@link TaskExecutor#execute} 方法，
 * 允许任务逻辑与任务管理器进行通信。
 *
 * @author LinSanQi
 */
public interface TaskContext {

    /**
     * 获取任务的唯一标识符。
     *
     * @return 任务的唯一标识符。
     */
    Long getTaskId();

    /**
     * 检查该任务是否已被请求取消。
     * <p>
     * 长时间运行的任务应周期性地调用此方法。如果返回 {@code true}，
     * 任务应尽快清理资源并优雅地中止执行。
     *
     * @return 如果已请求取消，则返回 {@code true}；否则返回 {@code false}。
     */
    boolean isCancellationRequested();

    /**
     * (可选) 向外报告任务的执行进度。
     * <p>
     * 进度信息可以是一个任意的可序列化对象，其结构由具体的任务执行器定义。
     *
     * @param progressInfo 描述当前进度的对象。
     */
    void updateProgress(Object progressInfo);
} 