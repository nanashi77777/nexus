package cn.lin037.nexus.infrastructure.common.persistent.entity.agent;

import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentMemorySourceEnum;
import cn.lin037.nexus.infrastructure.common.persistent.handler.StringListTypeHandler;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 记忆实体类
 * 用于存储全局和会话级的用户相关记忆内容
 *
 * @author Lin037
 */
@Data
@FieldNameConstants
@Table("agent_memories")
public class AgentMemoryEntity implements Serializable {

    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 记忆ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long amId;

    /**
     * 用户ID
     */
    private Long amUserId;

    /**
     * 学习空间ID
     */
    private Long amLearningSpaceId;

    /**
     * 会话ID
     */
    private Long amSessionId;

    /**
     * 记忆等级
     * 0: 不启用
     * 1: 会话级
     * 2: 全局
     */
    private Integer amLevel;

    /**
     * 记忆标题
     */
    private String amTitle;

    /**
     * 记忆内容
     */
    private String amContent;

    /**
     * 重要性评分（1-10，10为最重要）
     */
    private Integer amImportanceScore;

    /**
     * 标签列表（用于分类和检索）
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> amTags;

    /**
     * 记忆来源
     * chat: 聊天
     * learning: 学习
     * manual: 手动添加
     * @see AgentMemorySourceEnum
     */
    private String amSource;

    /**
     * 访问次数
     */
    private Integer amAccessCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime amLastAccessedAt;

    /**
     * 创建时间
     */
    private LocalDateTime amCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime amUpdatedAt;

    /**
     * 删除时间（逻辑删除）
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime amDeletedAt;
}