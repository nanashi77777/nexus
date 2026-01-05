package cn.lin037.nexus.infrastructure.common.persistent.entity.agent;

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
 * Agent聊天消息实体
 * 用于存储用户与AI的对话消息，支持知识点关联和资源引用
 *
 * @author Lin037
 */
@Data
@FieldNameConstants
@Table("agent_chat_messages")
public class AgentChatMessageEntity implements Serializable {

    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long acmId;

    /**
     * 会话ID
     */
    private Long acmSessionId;

    /**
     * 用户ID
     */
    private Long acmUserId;

    /**
     * 学习空间ID
     */
    private Long acmLearningSpaceId;

    /**
     * 消息角色
     * USER: 用户消息
     * ASSISTANT: AI助手消息
     * SYSTEM: 系统消息
     * @see cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageRoleEnum
     */
    private String acmRole;

    /**
     * 消息内容
     */
    private String acmContent;

    /**
     * 消息类型
     * 0: 普通消息
     * 1：工具请求
     * 2：工具响应
     *
     * @see cn.lin037.nexus.infrastructure.common.persistent.enums.AgentChatMessageTypeEnum
     */
    private Integer acmType;

    /**
     * 关联的内容
     */
    private String acmCorrelationContent;

    /**
     * 输入/输出的token数量
     */
    private Integer acmTokens;

    /**
     * 创建时间
     */
    private LocalDateTime acmCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime acmUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime acmDeletedAt;
}