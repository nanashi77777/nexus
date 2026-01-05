package cn.lin037.nexus.infrastructure.common.task.scheduler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.common.exception.ApplicationException;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import cn.lin037.nexus.infrastructure.common.exception.InfrastructureException;
import cn.lin037.nexus.infrastructure.common.task.config.TaskExecutorConfig;
import cn.lin037.nexus.infrastructure.common.task.config.TaskProperties;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.lin037.nexus.infrastructure.common.task.exception.TaskExceptionCodeEnum;
import cn.lin037.nexus.infrastructure.common.task.executor.DefaultTaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;
import cn.lin037.nexus.infrastructure.common.task.model.ErrorDetails;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import cn.lin037.nexus.infrastructure.common.task.repository.AsyncTaskRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskScheduler implements ApplicationRunner {

    private final AsyncTaskRepository asyncTaskRepository;
    private final List<TaskExecutor<?, ?>> taskExecutors;
    private final ExecutorService shortRunningTaskExecutor;
    private final ExecutorService longRunningTaskExecutor;
    private final ScheduledExecutorService schedulerExecutor;
    private final ScheduledExecutorService watchdogExecutor;
    private final TaskProperties taskProperties;
    private final Map<Long, RunningTaskExecution> runningTaskExecutions = new ConcurrentHashMap<>();
    private Map<String, TaskExecutor<?, ?>> executorMap;
    private Semaphore shortRunningTaskSemaphore;
    private TaskScheduler self;

    public TaskScheduler(AsyncTaskRepository asyncTaskRepository,
                         List<TaskExecutor<?, ?>> taskExecutors,
                         @Qualifier(TaskExecutorConfig.SHORT_RUNNING_TASK_EXECUTOR) ExecutorService shortRunningTaskExecutor,
                         @Qualifier(TaskExecutorConfig.LONG_RUNNING_TASK_EXECUTOR) ExecutorService longRunningTaskExecutor,
                         @Qualifier(TaskExecutorConfig.TASK_SCHEDULER_EXECUTOR) ScheduledExecutorService schedulerExecutor,
                         TaskProperties taskProperties) {
        this.asyncTaskRepository = asyncTaskRepository;
        this.taskExecutors = taskExecutors;
        this.shortRunningTaskExecutor = shortRunningTaskExecutor;
        this.longRunningTaskExecutor = longRunningTaskExecutor;
        this.schedulerExecutor = schedulerExecutor;
        this.taskProperties = taskProperties;
        this.watchdogExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Autowired
    public void setSelf(@Lazy TaskScheduler self) {
        this.self = self;
    }

    @PostConstruct
    public void init() {
        executorMap = taskExecutors.stream()
                .collect(Collectors.toMap(TaskExecutor::getTaskType, Function.identity()));
        log.info("已加载 {} 个任务执行器：{}", executorMap.size(), executorMap.keySet());

        this.shortRunningTaskSemaphore = new Semaphore(taskProperties.getShortRunning().getConcurrency());
        log.info("短时任务并发信号量初始化，许可数：{}", taskProperties.getShortRunning().getConcurrency());
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!taskProperties.isEnabled()) {
            log.warn("任务调度模块已禁用。");
            return;
        }
        log.info("开始任务恢复程序...");
        recoverInterruptedTasks();

        log.debug("启动任务调度轮询...");
        schedulerExecutor.scheduleWithFixedDelay(
                this::pollAndDispatchTasks, 5, taskProperties.getPolling().getInterval(), taskProperties.getPolling().getUnit()
        );

        if (taskProperties.getTimeout().isEnabled()) {
            log.info("启动任务超时看门狗...");
            watchdogExecutor.scheduleWithFixedDelay(
                    this::checkTaskTimeouts, 60, taskProperties.getTimeout().getCheckIntervalSeconds(), TimeUnit.SECONDS
            );
        }
    }

    /**
     * 恢复中断的任务
     */
    private void recoverInterruptedTasks() {
        List<AsyncTask> tasksToRecover = asyncTaskRepository.findByStatusIn(List.of(TaskStatusEnum.RUNNING));
        log.info("发现 {} 项正在运行的任务需要恢复。", tasksToRecover.size());
        tasksToRecover.forEach(this::failInterruptedTask);
        log.info("等待中的任务将由常规调度程序自动处理。");
    }

    /**
     * 将因系统重启而中断的任务标记为失败状态
     * <p>
     * 当系统重启时，所有正在运行的任务都会丢失其执行状态。
     * 此方法会将这些任务的状态从 RUNNING 更新为 FAILED，
     * 并记录相应的错误信息和审计详情。
     *
     * @param task 需要标记为失败的中断任务对象
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failInterruptedTask(AsyncTask task) {
        // 创建系统重启导致任务中断的错误详情
        ErrorDetails errorDetails = new ErrorDetails("SystemRestart", "Task interrupted due to system restart.", null);

        // 更新任务状态为失败
        task.setAtStatus(TaskStatusEnum.FAILED.getCode());
        task.setAtUserFriendlyMessage("任务因系统重启而中断。");
        task.setAtAuditDetails(JSONUtil.toJsonStr(errorDetails));
        task.setAtFinishedAt(LocalDateTime.now());
        asyncTaskRepository.saveOrUpdate(task);

        try {
            // 获取任务对应的执行器并调用失败回调
            TaskExecutor<Object, Object> executor = getExecutor(task.getAtTaskType());
            Object params = deserializeParameters(task.getAtParametersJson(), executor.getParametersType());
            executor.onFailure(params, new InfrastructureException(TaskExceptionCodeEnum.TASK_INTERRUPTED));
        } catch (Exception e) {
            log.error("在为中断的任务 {} 调用 onFailure 回调时出错。", task.getAtId(), e);
        }
        log.warn("任务 {} 处于 RUNNING 状态，已标记为 FAILED。", task.getAtId());
    }

    /**
     * 轮询数据库并分发任务。
     */
    private void pollAndDispatchTasks() {
        try {
            if (isAllExecutorsSaturated()) {
                log.debug("所有任务执行器均处于饱和状态，跳过本轮数据库轮询。");
                return;
            }
            List<AsyncTask> waitingTasks = asyncTaskRepository.findByStatusInWithLimit(TaskStatusEnum.WAITING, taskProperties.getPolling().getBatchSize());
            if (waitingTasks.isEmpty()) return;

            log.info("发现 {} 个待处理任务，开始分发...", waitingTasks.size());
            waitingTasks.forEach(this::dispatch);
        } catch (Exception e) {
            log.error("任务轮询期间发生意外错误。", e);
        }
    }

    /**
     * 检查所有任务执行器是否都已饱和（无可用容量处理新任务）。
     *
     * @return 如果所有执行器都饱和则返回true，否则返回false
     */
    private boolean isAllExecutorsSaturated() {
        // 检查短时任务执行器是否饱和（无可用信号量）
        boolean shortRunningSaturated = shortRunningTaskSemaphore.availablePermits() == 0;

        // 检查长时任务执行器是否饱和（队列无剩余容量）
        boolean longRunningSaturated = false;
        if (longRunningTaskExecutor instanceof ThreadPoolExecutor longExecutor) {
            longRunningSaturated = longExecutor.getQueue().remainingCapacity() == 0;
        }

        // 只有当两种执行器都饱和时才返回true
        return shortRunningSaturated && longRunningSaturated;
    }

    /**
     * 分发一个任务给相应的执行器。
     *
     * @param task 需要分发的任务对象
     */
    private void dispatch(AsyncTask task) {
        ExecutorService executor = isLongRunning(task.getAtTaskType()) ? longRunningTaskExecutor : shortRunningTaskExecutor;
        Semaphore semaphore = isLongRunning(task.getAtTaskType()) ? null : shortRunningTaskSemaphore;

        if (semaphore != null && !semaphore.tryAcquire()) {
            log.info("短时任务执行器并发已满，任务 {} 将在下一轮被重新调度。", task.getAtId());
            return;
        }
        submitTask(task, executor, semaphore);
    }

    /**
     * 将任务提交到指定的执行器中执行。
     * <p>
     * 该方法会创建一个任务上下文，并将任务包装成Runnable提交给执行器。
     * 同时会记录正在运行的任务信息，以便后续管理和取消。
     *
     * @param task      需要执行的异步任务对象
     * @param executor  用于执行任务的执行器服务
     * @param semaphore 用于控制并发的信号量，长时任务为null
     */
    private void submitTask(AsyncTask task, ExecutorService executor, Semaphore semaphore) {
        DefaultTaskContext context = new DefaultTaskContext(task.getAtId());
        Runnable taskRunnable = () -> {
            try {
                self.processTask(task, context);
            } finally {
                // 释放信号量许可，允许其他短时任务执行
                if (semaphore != null) semaphore.release();
            }
        };
        Future<?> future = executor.submit(taskRunnable);
        // 记录正在运行的任务执行信息
        runningTaskExecutions.put(task.getAtId(), new RunningTaskExecution(context, future, LocalDateTime.now()));
    }

    /**
     * 请求取消指定ID的任务执行。
     * <p>
     * 该方法会查找正在运行的任务执行信息，如果找到则通过任务上下文请求取消。
     *
     * @param taskId 需要取消的任务ID
     */
    public void requestCancellation(Long taskId) {
        RunningTaskExecution execution = runningTaskExecutions.get(taskId);
        if (execution != null) {
            log.info("请求取消任务 {}", taskId);
            execution.context().requestCancellation();
        } else {
            log.warn("无法请求取消任务 {}：当前未运行或未找到上下文。", taskId);
        }
    }

    /**
     * 处理单个异步任务的完整执行流程。
     * <p>
     * 包括：设置任务为运行状态、获取执行器、反序列化参数、执行任务、
     * 处理成功/失败结果、调用回调方法等。
     *
     * @param task    需要处理的任务对象
     * @param context 任务执行上下文
     */
    public void processTask(AsyncTask task, DefaultTaskContext context) {
        Long taskId = task.getAtId();
        log.info("开始处理任务 {}，类型：{}", taskId, task.getAtTaskType());

        // 尝试将任务状态设置为RUNNING，如果失败则直接返回
        if (self.setTaskToRunningInNewTransaction(taskId) == null) return;

        TaskExecutor<Object, Object> executor = null;
        Object parameters = null;

        try {
            // 获取对应的任务执行器
            executor = getExecutor(task.getAtTaskType());
            log.info("找到任务执行器：{}", executor.getClass().getSimpleName());

            // 反序列化任务参数
            parameters = deserializeParameters(task.getAtParametersJson(), executor.getParametersType());
            log.info("参数反序列化成功：{}", parameters);

            // 执行任务
            TaskResult<?> result = executor.execute(parameters, context);

            // 检查任务是否被取消
            if (context.isCancellationRequested()) {
                throw new InfrastructureException(TaskExceptionCodeEnum.TASK_CANCELLED);
            }

            log.info("任务 {} 已成功完成。", taskId);
            // 处理成功结果
            self.handleSuccessInNewTransaction(taskId, result, parameters, executor);

        } catch (Exception e) {
            log.error("任务 {} 失败，出现异常。", taskId, e);
            // 根据取消状态处理不同结果
            if (context.isCancellationRequested()) {
                self.handleCancellationInNewTransaction(taskId, context.getUserFriendlyMessage(), "任务执行期间被取消。");
            } else {
                String userMessage;
                // 确定用户友好的错误消息
                if (e instanceof InfrastructureException || e instanceof ApplicationException) {
                    userMessage = e.getMessage();
                } else {
                    userMessage = TaskExceptionCodeEnum.INFRA_TASK_UNEXPECTED_ERROR.getMessage();
                }

                if (executor != null) {
                    // 调用执行器的失败回调
                    self.handleFailureInNewTransaction(taskId, userMessage, e, parameters, executor);
                } else {
                    // 如果没有获取到执行器，只更新任务状态
                    self.handleExecutorNotFoundFailureInNewTransaction(taskId, userMessage, e);
                }
            }
        } finally {
            // 清理正在运行的任务记录
            runningTaskExecutions.remove(taskId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AsyncTask setTaskToRunningInNewTransaction(Long taskId) {
        if (asyncTaskRepository.compareAndSetStatus(taskId, TaskStatusEnum.RUNNING, TaskStatusEnum.WAITING) == 0) {
            log.warn("任务 {} 无法设置为 RUNNING，可能已被其他调度器处理。", taskId);
            return null;
        }
        return asyncTaskRepository.findById(taskId).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSuccessInNewTransaction(Long taskId, TaskResult<?> result, Object params, TaskExecutor<Object, Object> executor) {
        asyncTaskRepository.findById(taskId).ifPresent(task -> {
            task.setAtStatus(TaskStatusEnum.COMPLETED.getCode());



            if (result != null) {
                task.setAtUserFriendlyMessage(result.getUserFriendlyMessage());
                // 构建详细的审计信息
                task.setAtAuditDetails(JSONUtil.toJsonStr(result));
            } else {
                // 如果 result 为 null，设置默认消息
                task.setAtUserFriendlyMessage("任务执行成功（无返回结果）");
                task.setAtAuditDetails("{}");
            }






            //task.setAtUserFriendlyMessage(result.getUserFriendlyMessage());

            //构建详细的审计信息，正确序列化auditPayload
            //task.setAtAuditDetails(JSONUtil.toJsonStr(result));
            task.setAtFinishedAt(LocalDateTime.now());
            asyncTaskRepository.saveOrUpdate(task);
        });
        try {
            @SuppressWarnings("unchecked")
            TaskResult<Object> completeResult = (TaskResult<Object>) result;
            executor.onCompletion(params, completeResult);
        } catch (Exception e) {
            log.error("任务 {} 的 onCompletion 回调出错。", taskId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailureInNewTransaction(Long taskId, String userFriendlyMessage, Exception failureException, Object params, TaskExecutor<Object, Object> executor) {
        asyncTaskRepository.findById(taskId).ifPresent(task -> {
            task.setAtStatus(TaskStatusEnum.FAILED.getCode());
            task.setAtUserFriendlyMessage(userFriendlyMessage);

            // 基本错误信息
            ErrorDetails errorDetails = getDetailedErrorInfo(failureException);
            Object auditDetails = errorDetails;

            // 检查异常中是否包含额外数据
            if (failureException instanceof ApplicationException appEx) {
                Object data = appEx.getData();
                Class<?> targetType = appEx.getTargetType();

                if (data != null && targetType != null) {
                    // 将data转换为其声明的类型，并创建扩展的ErrorDetails
                    Object convertedData = convertDataToTargetType(data, targetType);
                    auditDetails = new TaskErrorDetailsWithData(
                            errorDetails.getExceptionClass(),
                            errorDetails.getMessage(),
                            errorDetails.getStackTrace(),
                            convertedData,
                            targetType.getName()
                    );

                    // 特殊处理Token使用量信息的日志记录 TODO: 或许应该删除
                    /*if (isTokenUsageType(targetType)) {
                        log.info("任务 {} 失败时记录Token使用量: {}", taskId, getTokenUsageDescription(convertedData));
                    } else {
                        log.info("任务 {} 失败时记录额外数据 ({}): {}", taskId, targetType.getSimpleName(), convertedData);
                    }*/
                }
            }

            task.setAtAuditDetails(JSONUtil.toJsonStr(auditDetails));
            task.setAtFinishedAt(LocalDateTime.now());
            asyncTaskRepository.saveOrUpdate(task);
        });
        try {
            executor.onFailure(params, failureException);
        } catch (Exception e) {
            log.error("任务 {} 的 onFailure 回调出错。", taskId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCancellationInNewTransaction(Long taskId, String friendlyMessage, String message) {
        asyncTaskRepository.findById(taskId).ifPresent(task -> {
            task.setAtStatus(TaskStatusEnum.CANCELLED.getCode());
            task.setAtUserFriendlyMessage(friendlyMessage);
            task.setAtAuditDetails(JSONUtil.toJsonStr(new ErrorDetails("Cancellation", message, null)));
            task.setAtFinishedAt(LocalDateTime.now());
            asyncTaskRepository.saveOrUpdate(task);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleExecutorNotFoundFailureInNewTransaction(Long taskId, String userFriendlyMessage, Exception failureException) {
        asyncTaskRepository.findById(taskId).ifPresent(task -> {
            task.setAtStatus(TaskStatusEnum.FAILED.getCode());
            task.setAtUserFriendlyMessage(userFriendlyMessage);
            task.setAtAuditDetails(JSONUtil.toJsonStr(getDetailedErrorInfo(failureException)));
            task.setAtFinishedAt(LocalDateTime.now());
            asyncTaskRepository.saveOrUpdate(task);
        });
    }

    private void checkTaskTimeouts() {
        try {
            LocalDateTime now = LocalDateTime.now();
            long timeoutMillis = taskProperties.getTimeout().getDefaultUnit().toMillis(taskProperties.getTimeout().getDefaultDuration());
            Duration timeout = Duration.ofMillis(timeoutMillis);

            runningTaskExecutions.forEach((taskId, execution) -> {
                if (Duration.between(execution.startTime(), now).compareTo(timeout) > 0) {
                    log.warn("任务 {} 已超时。", taskId);
                    execution.future().cancel(true);
                    self.handleTimeoutInNewTransaction(taskId, timeout);
                    runningTaskExecutions.remove(taskId);
                }
            });
        } catch (Exception e) {
            log.error("检查任务超时期间发生意外错误。", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleTimeoutInNewTransaction(Long taskId, Duration timeout) {
        asyncTaskRepository.findById(taskId).ifPresent(task -> {
            if (TaskStatusEnum.RUNNING.getCode().equals(task.getAtStatus())) {
                task.setAtStatus(TaskStatusEnum.FAILED.getCode());
                task.setAtUserFriendlyMessage("任务执行超时。");
                String auditMsg = "Task timed out after " + timeout.toString();
                task.setAtAuditDetails(JSONUtil.toJsonStr(new ErrorDetails("TimeoutException", auditMsg, null)));
                task.setAtFinishedAt(LocalDateTime.now());
                asyncTaskRepository.saveOrUpdate(task);
                log.info("任务 {} 已被标记为超时失败。", taskId);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <P, R> TaskExecutor<P, R> getExecutor(String taskType) {
        TaskExecutor<?, ?> executor = executorMap.get(taskType);
        if (executor == null) {
            throw InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_EXECUTOR_NOT_FOUND, "Type: " + taskType);
        }
        return (TaskExecutor<P, R>) executor;
    }

    private <T> T deserializeParameters(String json, Class<T> type) {
        try {
            return JSONUtil.toBean(json, type);
        } catch (Exception e) {
            throw InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_SERIALIZATION_ERROR, "参数反序列化失败", e);
        }
    }

    private ErrorDetails getDetailedErrorInfo(Exception e) {
        String stackTrace = ExceptionUtil.stacktraceToString(e, 2000);
        return new ErrorDetails(e.getClass().getName(), e.getMessage(), stackTrace);
    }

    private boolean isLongRunning(String taskType) {
        List<String> longRunningTypes = taskProperties.getMapping().get("long-running");
        return longRunningTypes != null && longRunningTypes.contains(taskType);
    }

    /**
     * 将数据转换为其目标类型
     */
    private Object convertDataToTargetType(Object data, Class<?> targetType) {
        if (data == null || targetType == null) {
            return data;
        }

        // 如果已经是目标类型，直接返回
        if (targetType.isInstance(data)) {
            return data;
        }

        // 其他情况直接返回原始数据
        return data;
    }

    /**
     * 检查是否是Token使用量相关的类型
     */
    private boolean isTokenUsageType(Class<?> type) {
        if (type == null) return false;

        String typeName = type.getName();
        // 检查常见的TokenUsage类型
        return typeName.contains("TokenUsage") ||
                typeName.equals("cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator");
    }

    /**
     * 获取Token使用量的描述信息
     */
    private String getTokenUsageDescription(Object tokenUsageData) {
        if (tokenUsageData == null) {
            return "无Token使用量信息";
        }

        if (tokenUsageData instanceof TokenUsageAccumulator accumulator) {
            var total = accumulator.getTotal();
            return String.format("输入: %d, 输出: %d, 总计: %d",
                    total.inputTokenCount(), total.outputTokenCount(), total.totalTokenCount());
        }

        // 对于其他类型的TokenUsage，尝试通过toString()获取信息
        return tokenUsageData.toString();
    }

    private record RunningTaskExecution(DefaultTaskContext context, Future<?> future, LocalDateTime startTime) {
    }

    /**
     * 扩展的错误详情，包含额外数据信息（如TokenUsage等）
     */
    @Getter
    public static class TaskErrorDetailsWithData extends ErrorDetails {
        private final Object data;
        private final String dataType;

        public TaskErrorDetailsWithData(String exceptionClass, String message, String stackTrace, Object data, String dataType) {
            super(exceptionClass, message, stackTrace);
            this.data = data;
            this.dataType = dataType;
        }

    }

    /**
     * 扩展的错误详情，包含TokenUsage信息（保持向后兼容）
     *
     * @deprecated 使用 TaskErrorDetailsWithData 替代
     */
    @Getter
    @Deprecated
    public static class TaskErrorDetailsWithTokenUsage extends ErrorDetails {
        private final Object tokenUsage;

        public TaskErrorDetailsWithTokenUsage(String exceptionClass, String message, String stackTrace, Object tokenUsage) {
            super(exceptionClass, message, stackTrace);
            this.tokenUsage = tokenUsage;
        }

    }

}