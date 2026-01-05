package cn.lin037.nexus.infrastructure.common.task.service;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.task.api.AsyncTaskManager;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.exception.TaskExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;
import cn.lin037.nexus.infrastructure.common.task.repository.AsyncTaskRepository;
import cn.lin037.nexus.infrastructure.common.task.scheduler.TaskScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * AsyncTaskManager 的默认实现。
 *
 * @author LinSanQi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskManagerImpl implements AsyncTaskManager {

    private final AsyncTaskRepository asyncTaskRepository;
    private final TaskScheduler taskScheduler;

    @Override
    @Transactional
    public Long submit(String taskType, Map<String, Object> parameters, String ownerIdentifier) {
        log.info("所有者 '{}' 提交了类型为 '{}' 的新任务", ownerIdentifier, taskType);

        // 1. 创建并持久化任务实体
        AsyncTask task = AsyncTask.builder()
                .atTaskType(taskType)
                .atParametersJson(JSONUtil.toJsonStr(parameters))
                .atStatus(TaskStatusEnum.WAITING.getCode())
                .atOwnerIdentifier(ownerIdentifier)
                .atCreatedAt(LocalDateTime.now())
                .build();

        task = asyncTaskRepository.saveOrUpdate(task);
        log.info("任务 {} 已创建并持久化，状态为 WAITING。", task.getAtId());

        // 2. [已移除] 不再立即调度，调度器将通过轮询来获取任务

        return task.getAtId();
    }

    @Override
    public Optional<AsyncTask> getTask(Long taskId) {
        return asyncTaskRepository.findById(taskId);
    }

    @Override
    @Transactional
    public void cancel(Long taskId, String operatorIdentifier) {
        log.info("操作员 '{}' 正在尝试取消任务 {}", operatorIdentifier, taskId);

        AsyncTask task = asyncTaskRepository.findById(taskId)
                .orElseThrow(() -> InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_NOT_FOUND));

        switch (Objects.requireNonNull(TaskStatusEnum.fromCode(task.getAtStatus()))) {
            case WAITING:
                // 对于等待中的任务，直接标记为已取消，防止被调度器获取
                task.setAtStatus(TaskStatusEnum.CANCELLED.getCode());
                task.setAtFinishedAt(LocalDateTime.now());
                task.setAtUserFriendlyMessage("任务已取消。");
                task.setAtAuditDetails("操作员 '" + operatorIdentifier + "' 尝试取消任务。");
                asyncTaskRepository.compareAndUpdate(task, TaskStatusEnum.WAITING);
                log.info("任务 {} 处于 WAITING 状态，已成功取消。", taskId);
                break;
            case RUNNING:
                // 对于运行中的任务，请求调度器尝试取消
                log.info("任务 {} 处于 RUNNING 状态，正在请求调度器进行取消...", taskId);
                taskScheduler.requestCancellation(taskId);
                break;
            case COMPLETED:
            case FAILED:
            case CANCELLED:
                // 对于已终结的任务，不允许应该重复取消
                // throw InfrastructureException.fromCode(TaskExceptionCodeEnum.INFRA_TASK_CANNOT_BE_CANCELLED,
                //         "任务已处于最终状态，无法取消。当前状态: " + task.getAtStatus());
            default:
                // 未知状态
                throw InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_UNKNOWN_STATUS,
                        "未知的任务状态: " + task.getAtStatus());
        }
    }
} 