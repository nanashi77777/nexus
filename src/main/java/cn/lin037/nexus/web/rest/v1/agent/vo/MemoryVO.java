package cn.lin037.nexus.web.rest.v1.agent.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent记忆VO
 *
 * @author Lin037
 */
@Data
public class MemoryVO {

    /**
     * 记忆ID
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
     * 记忆等级
     * 0: 不启用
     * 1: 会话级
     * 2: 全局
     */
    private Integer level;

    /**
     * 记忆标题
     */
    private String title;

    /**
     * 记忆内容
     */
    private String content;

    /**
     * 重要性评分（1-10，10为最重要）
     */
    private Integer importanceScore;

    /**
     * 标签列表（用于分类和检索）
     */
    private List<String> tags;

    /**
     * 记忆来源
     * chat: 聊天
     * learning: 学习
     * manual: 手动添加
     */
    private String source;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;

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
     * @param entity AgentMemoryEntity实体
     * @return MemoryVO
     */
    public static MemoryVO fromEntity(AgentMemoryEntity entity) {
        if (entity == null) {
            return null;
        }
        
        MemoryVO vo = new MemoryVO();
        BeanUtil.copyProperties(entity, vo);
        vo.setId(entity.getAmId());
        vo.setUserId(entity.getAmUserId());
        vo.setLearningSpaceId(entity.getAmLearningSpaceId());
        vo.setSessionId(entity.getAmSessionId());
        vo.setLevel(entity.getAmLevel());
        vo.setTitle(entity.getAmTitle());
        vo.setContent(entity.getAmContent());
        vo.setImportanceScore(entity.getAmImportanceScore());
        vo.setTags(entity.getAmTags());
        vo.setSource(entity.getAmSource());
        vo.setAccessCount(entity.getAmAccessCount());
        vo.setLastAccessedAt(entity.getAmLastAccessedAt());
        vo.setCreatedAt(entity.getAmCreatedAt());
        vo.setUpdatedAt(entity.getAmUpdatedAt());
        return vo;
    }
}