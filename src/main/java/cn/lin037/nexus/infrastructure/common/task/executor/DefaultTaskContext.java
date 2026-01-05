package cn.lin037.nexus.infrastructure.common.task.executor;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TaskContext 的默认实现。
 * <p>
 * 该类是线程安全的。
 *
 * @author LinSanQi
 */
public class DefaultTaskContext implements TaskContext {

    private final AtomicBoolean cancellationRequested = new AtomicBoolean(false);
    @Getter
    private final Long taskId;
    @Getter
    @Setter
    private String userFriendlyMessage;

    public DefaultTaskContext(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public boolean isCancellationRequested() {
        return cancellationRequested.get();
    }

    /**
     * 请求取消任务。此方法由调度器调用。
     */
    public void requestCancellation() {
        this.cancellationRequested.set(true);
    }

    @Override
    public void updateProgress(Object progressInfo) {
        // 在此版本中，我们通过日志记录进度。
        // 未来可以扩展为将进度信息持久化或发送到消息队列。
        // log.info("Task {} progress updateById: {}", this.taskId, progressInfo);
    }
}