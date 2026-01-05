package cn.lin037.nexus.infrastructure.common.persistent.entity.agent;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AcsAutoCallToolPermissionEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionBelongsToEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionStatusEnum;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatSessionTypeEnum;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Ignore;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent聊天会话实体
 * 用于管理用户的聊天会话，支持普通聊天和学习会话两种类型
 *
 * @author Lin037
 */
@Data
@FieldNameConstants
@Table("agent_chat_sessions")
public class AgentChatSessionEntity implements Serializable {

    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long acsId;

    /**
     * 用户ID
     */
    private Long acsUserId;

    /**
     * 学习空间ID
     */
    private Long acsLearningSpaceId;

    /**
     * 会话标题（用户自定义）
     */
    private String acsTitle;

    /**
     * 会话类型
     * @see AgentChatSessionTypeEnum
     * 1: 普通聊天 (CHAT)
     * 2: 学习会话 (LEARNING)
     */
    private Integer acsType;

    /**
     * 会话状态
     * @see AgentChatSessionStatusEnum
     * 1: 正常可以对话 (NORMAL)
     * 2: 响应中 (RESPONDING)
     * 3: 暂停 (PAUSED)
     * 4: 工具调用中 (TOOL_CALLING)
     * 5: 等待工具授权 (WAITING_TOOL_AUTHORIZATION)
     * 6: 错误 (ERROR)
     */
    private Integer acsStatus;

    /**
     * 会话所属
     * @see AgentChatSessionBelongsToEnum
     * 1: 图谱 (GRAPH)
     * 2: 讲解 (EXPLANATION)
     * 3: 笔记 (NOTE)
     * 4: 学习 (LEARNING)
     */
    private Integer acsBelongsTo;

    /**
     * 自动调用工具的权限
     * @see AcsAutoCallToolPermissionEnum
     * 1: 只读
     * 2: 只写
     * 3: 可读可写
     * 4: 关闭
     */
    private Integer acsIsAutoCallTool;

    /**
     * 创建时间
     */
    private LocalDateTime acsCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime acsUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime acsDeletedAt;
}