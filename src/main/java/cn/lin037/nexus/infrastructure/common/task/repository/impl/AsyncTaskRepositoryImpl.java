package cn.lin037.nexus.infrastructure.common.task.repository.impl;

import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.exception.TaskExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;
import cn.lin037.nexus.infrastructure.common.task.repository.AsyncTaskRepository;
import cn.lin037.nexus.infrastructure.common.task.repository.mapper.AsyncTaskMapper;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import cn.xbatis.core.sql.executor.chain.UpdateChain;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsyncTaskRepositoryImpl implements AsyncTaskRepository {

    private final AsyncTaskMapper asyncTaskMapper;

    public AsyncTaskRepositoryImpl(AsyncTaskMapper asyncTaskMapper) {
        this.asyncTaskMapper = asyncTaskMapper;
    }

    @Override
    public AsyncTask saveOrUpdate(AsyncTask task) {
        int count = asyncTaskMapper.saveOrUpdate(task);
        if (count <= 0 || task.getAtId() == null) {
            throw new InfrastructureException(TaskExceptionCodeEnum.INFRA_TASK_CREATE_OR_UPDATE_FAILED);
        }
        return task;
    }

    @Override
    public Optional<AsyncTask> findById(Long taskId) {
        return Optional.ofNullable(asyncTaskMapper.getById(taskId));
    }

    @Override
    public List<AsyncTask> findByStatusIn(List<TaskStatusEnum> statuses) {
        List<Integer> statusCodes = statuses.stream().map(TaskStatusEnum::getCode).toList();
        List<AsyncTask> list = QueryChain.of(asyncTaskMapper)
                .in(AsyncTask::getAtStatus, statusCodes).list();
        return Optional.ofNullable(list).orElse(List.of());
    }

    @Override
    public int compareAndSetStatus(Long taskId, TaskStatusEnum newStatus, TaskStatusEnum expectedStatus) {

        return UpdateChain.of(asyncTaskMapper)
                .eq(AsyncTask::getAtId, taskId)
                .eq(AsyncTask::getAtStatus, expectedStatus.getCode())
                .set(AsyncTask::getAtStatus, newStatus.getCode())
                .set(TaskStatusEnum.RUNNING.equals(newStatus), AsyncTask::getAtStartedAt, LocalDateTime.now())
                .execute();
    }

    @Override
    public List<AsyncTask> findByStatusInWithLimit(TaskStatusEnum status, int limit) {
        return QueryChain.of(asyncTaskMapper)
                .eq(AsyncTask::getAtStatus, status.getCode())
                .limit(limit)
                .list();
    }

    @Override
    public void compareAndUpdate(AsyncTask task, TaskStatusEnum expectedStatus) {
        UpdateChain.of(asyncTaskMapper)
                .eq(AsyncTask::getAtId, task.getAtId())
                .eq(AsyncTask::getAtStatus, expectedStatus.getCode())
                .set(AsyncTask::getAtStatus, task.getAtStatus())
                .execute();
    }

    @Override
    public void updateStartedAt(Long taskId, LocalDateTime startedAt) {
        UpdateChain.of(asyncTaskMapper)
                .eq(AsyncTask::getAtId, taskId)
                .set(AsyncTask::getAtStartedAt, startedAt)
                .execute();
    }
}
