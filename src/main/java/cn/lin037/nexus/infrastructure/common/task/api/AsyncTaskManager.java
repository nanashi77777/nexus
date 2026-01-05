package cn.lin037.nexus.infrastructure.common.task.api;

import cn.lin037.nexus.infrastructure.common.task.model.AsyncTask;

import java.util.Map;
import java.util.Optional;

/**
 * 异步任务管理器接口
 * <p>
 * 这是上层应用与任务模块交互的统一入口。
 *
 * @author LinSanQi
 */
public interface AsyncTaskManager {

    /**
     * 提交一个新任务。
     *
     * @param taskType        任务类型的唯一标识符 (e.g., "AI_DOCUMENT_GENERATION")
     * @param parameters      任务执行所需的参数对象。为了通用性，推荐使用 Map<String, Object>。
     *                        该对象必须可以被JSON序列化。
     * @param ownerIdentifier 任务所有者的标识，用于追踪和权限控制。
     * @return 已创建任务的唯一ID。
     */
    Long submit(String taskType, Map<String, Object> parameters, String ownerIdentifier);

    /**
     * 获取指定任务的当前状态和信息。
     *
     * @param taskId 任务ID
     * @return 任务的详细信息
     */
    Optional<AsyncTask> getTask(Long taskId);

    /**
     * 尝试取消一个任务。
     *
     * @param taskId             任务ID
     * @param operatorIdentifier 执行取消操作的用户或系统的标识 (用于审计)。
     */
    void cancel(Long taskId, String operatorIdentifier);
} 