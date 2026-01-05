package cn.lin037.nexus.infrastructure.common.task.repository;

import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 异步任务仓库接口
 * <p>
 * 定义了与 `async_tasks` 表交互的数据库操作。
 * 实现将由 ORM 框架（如 xbatis）提供。
 *
 * @author LinSanQi
 */
public interface AsyncTaskRepository {

    /**
     * 保存或更新异步任务。
     * <p>
     * 如果任务ID为空，则插入新任务；否则，更新现有任务。
     *
     * @param task 任务对象
     * @return 返回持久化的任务对象（可能包含数据库生成的ID）
     */
    AsyncTask saveOrUpdate(AsyncTask task);

    /**
     * 根据ID查找任务。
     *
     * @param taskId 任务ID
     * @return 包含任务的Optional，如果找不到则为空
     */
    Optional<AsyncTask> findById(Long taskId);

    /**
     * 根据一组状态查找任务。
     * <p>
     * 此方法主要用于系统启动时恢复中断的任务。
     *
     * @param statuses 状态列表
     * @return 符合状态的任务列表
     */
    List<AsyncTask> findByStatusIn(List<TaskStatusEnum> statuses);

    /**
     * 以原子方式更新任务状态。
     * 仅当任务的当前状态与期望状态匹配时，才执行更新。
     *
     * @param id             任务ID
     * @param newStatus      新状态
     * @param expectedStatus 期望的当前状态
     * @return 受影响的行数（1表示成功，0表示失败）
     */
    int compareAndSetStatus(Long id, TaskStatusEnum newStatus, TaskStatusEnum expectedStatus);

    /**
     * 根据状态查找任务，并限制返回数量。
     * <p>
     * 用于调度器轮询，以高效地获取待处理任务。
     *
     * @param status 任务状态码
     * @param limit  最大返回数量
     * @return 符合状态的任务列表
     */
    List<AsyncTask> findByStatusInWithLimit(TaskStatusEnum status, int limit);

    /**
     * 以原子方式更新任务状态。
     * 仅当任务的当前状态与期望状态匹配时，才执行更新。
     *
     * @param task           任务对象
     * @param expectedStatus 期望的当前状态
     */
    void compareAndUpdate(AsyncTask task, TaskStatusEnum expectedStatus);

    /**
     * 更新任务的开始时间。
     *
     * @param taskId    任务ID
     * @param startedAt 开始时间
     */
    void updateStartedAt(Long taskId, LocalDateTime startedAt);

} 