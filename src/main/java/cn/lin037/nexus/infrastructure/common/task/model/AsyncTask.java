package cn.lin037.nexus.infrastructure.common.task.model;

import cn.lin037.nexus.infrastructure.common.persistent.handler.JsonbTypeHandler;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Ignore;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import cn.xbatis.db.annotations.TypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 异步任务数据模型
 * <p>
 * 该类映射到 `async_tasks` 数据库表，用于持久化异步任务的状态和数据。
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "async_tasks")
public class AsyncTask implements Serializable {

    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;
    /**
     * 任务的唯一标识符，对应数据库主键。
     */
    @TableId(value = IdAutoType.AUTO)
    private Long atId;
    /**
     * 任务类型的唯一标识符
     * 用于关联到具体的 {@link cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor}。
     */
    private String atTaskType;
    /**
     * 任务的当前状态。
     *
     * @see TaskStatusEnum
     */
    private Integer atStatus;
    /**
     * 任务执行所需的参数，以JSON字符串形式存储。
     */
    @TypeHandler(value = JsonbTypeHandler.class)
    private String atParametersJson;
    /**
     * 如果任务成功完成，此处放置完成结果提示信息。
     * 如果任务执行失败，此处存储对用户友好的错误信息。
     */
    private String atUserFriendlyMessage;
    /**
     * 用于审计的详细信息。
     * <p>
     * 成功时，存储完整的、序列化为JSON的结果对象。
     * 失败时，存储包含堆栈跟踪的、序列化为JSON的ErrorDetails对象。
     */
    @TypeHandler(value = JsonbTypeHandler.class)
    private String atAuditDetails;
    /**
     * 任务所有者的标识，用于追踪和权限控制。
     */
    private String atOwnerIdentifier;
    /**
     * 任务的创建时间。
     */
    private LocalDateTime atCreatedAt;
    /**
     * 任务开始执行的时间。
     */
    private LocalDateTime atStartedAt;
    /**
     * 任务完成（成功、失败或取消）的时间。
     */
    private LocalDateTime atFinishedAt;
}