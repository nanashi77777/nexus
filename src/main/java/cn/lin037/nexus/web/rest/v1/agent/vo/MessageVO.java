package cn.lin037.nexus.web.rest.v1.agent.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent聊天消息VO
 *
 * @author Lin037
 */
@Data
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习空间ID
     */
    private Long learningSpaceId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息角色
     * 1: 用户 (USER)
     * 2: 助手 (ASSISTANT)
     * 3: 系统 (SYSTEM)
     * 4: 工具 (TOOL)
     */
    private Integer role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     * 1: 文本 (TEXT)
     * 2: 图片 (IMAGE)
     * 3: 文件 (FILE)
     * 4: 工具调用 (TOOL_CALL)
     * 5: 工具结果 (TOOL_RESULT)
     */
    private Integer messageType;

    /**
     * 消息状态
     * 1: 正常 (NORMAL)
     * 2: 发送中 (SENDING)
     * 3: 发送失败 (FAILED)
     * 4: 已撤回 (RECALLED)
     */
    private Integer status;

    /**
     * 父消息ID（用于回复功能）
     */
    private Long parentMessageId;

    /**
     * 工具调用ID（当消息类型为工具调用时使用）
     */
    private String toolCallId;

    /**
     * 工具名称（当消息类型为工具调用或工具结果时使用）
     */
    private String toolName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从Entity转换为VO
     *
     * @param entity AgentChatMessageEntity实体
     * @return MessageVO
     */
    public static MessageVO fromEntity(AgentChatMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        
        MessageVO vo = new MessageVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setId(entity.getAcmId());
        vo.setUserId(entity.getAcmUserId());
        vo.setLearningSpaceId(entity.getAcmLearningSpaceId());
        vo.setSessionId(entity.getAcmSessionId());
        // vo.setRole(entity.getAcmRole()); // 类型不匹配，String无法转换为Integer，暂时注释
        vo.setContent(entity.getAcmContent());
        vo.setMessageType(entity.getAcmType()); // 使用acmType字段
        // vo.setStatus(entity.getAcmStatus()); // 字段不存在，注释
        // vo.setParentMessageId(entity.getAcmParentMessageId()); // 字段不存在，注释
        // vo.setToolCallId(entity.getAcmToolCallId()); // 字段不存在，注释
        // vo.setToolName(entity.getAcmToolName()); // 字段不存在，注释
        vo.setCreatedAt(entity.getAcmCreatedAt());
        vo.setUpdatedAt(entity.getAcmUpdatedAt());
        return vo;
    }
}