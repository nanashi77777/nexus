package cn.lin037.nexus.infrastructure.common.persistent.entity;

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
 * 学习空间表实体，实现多租户数据隔离的核心。用户的每一份独立学习内容都对应一个学习空间。
 *
 * @author GitHub Copilot
 */
@Data
@FieldNameConstants
@Table(value = "learning_spaces")
public class LearningSpaceEntity implements Serializable {
    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 主键, 学习空间雪花ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long lsId;

    /**
     * 所属用户的ID (外键)
     */
    private Long lsUserId;

    /**
     * 学习空间的名称
     */
    private String lsName;

    /**
     * 对学习空间的详细描述
     */
    private String lsDescription;

    /**
     * 空间内AI的全局参考Prompt
     */
    private String lsSpacePrompt;

    /**
     * 空间封面图URL, 可为NULL
     */
    private String lsCoverImageUrl;

    /**
     * 创建时间, 默认 NOW()
     */
    private LocalDateTime lsCreatedAt;

    /**
     * 最后修改时间, 默认 NOW()
     */
    private LocalDateTime lsUpdatedAt;

    /**
     * 逻辑删除时间, 为 NULL 表示未删除
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime lsDeletedAt;
}
