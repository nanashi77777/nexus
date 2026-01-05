package cn.lin037.nexus.web.rest.v1.agent.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent聊天会话VO
 *
 * @author Lin037
 */
@Data
public class SessionVO {

    /**
     * 会话ID
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
     * 会话标题
     */
    private String title;

    /**
     * 会话类型
     * 1: 普通聊天 (CHAT)
     * 2: 学习会话 (LEARNING)
     */
    private Integer type;

    /**
     * 会话状态
     * 1: 正常可以对话 (NORMAL)
     * 2: 响应中 (RESPONDING)
     * 3: 暂停 (PAUSED)
     * 4: 工具调用中 (TOOL_CALLING)
     * 5: 等待工具授权 (WAITING_TOOL_AUTHORIZATION)
     * 6: 错误 (ERROR)
     */
    private Integer status;

    /**
     * 会话所属
     * 1: 图谱 (GRAPH)
     * 2: 讲解 (EXPLANATION)
     * 3: 笔记 (NOTE)
     * 4: 学习 (LEARNING)
     */
    private Integer belongsTo;

    /**
     * 自动调用工具的权限
     * 1: 只读
     * 2: 只写
     * 3: 可读可写
     * 4: 关闭
     */
    private Integer isAutoCallTool;

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
     * @param entity AgentChatSessionEntity实体
     * @return SessionVO
     */
    public static SessionVO fromEntity(AgentChatSessionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SessionVO vo = new SessionVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setId(entity.getAcsId());
        vo.setUserId(entity.getAcsUserId());
        vo.setLearningSpaceId(entity.getAcsLearningSpaceId());
        vo.setTitle(entity.getAcsTitle());
        vo.setType(entity.getAcsType());
        vo.setStatus(entity.getAcsStatus());
        vo.setBelongsTo(entity.getAcsBelongsTo());
        vo.setIsAutoCallTool(entity.getAcsIsAutoCallTool());
        vo.setCreatedAt(entity.getAcsCreatedAt());
        vo.setUpdatedAt(entity.getAcsUpdatedAt());
        return vo;
    }
}