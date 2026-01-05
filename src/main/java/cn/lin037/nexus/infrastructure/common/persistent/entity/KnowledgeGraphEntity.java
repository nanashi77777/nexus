package cn.lin037.nexus.infrastructure.common.persistent.entity;

import cn.lin037.nexus.infrastructure.common.persistent.handler.JsonbTypeHandler;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableField;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识图谱实体
 *
 * @author LinSanQi
 */
@Data
@Table("knowledge_graphs")
public class KnowledgeGraphEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图谱ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long kgId;

    /**
     * 学习空间ID
     */
    private Long kgLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long kgCreatedByUserId;

    /**
     * 图谱标题
     */
    private String kgTitle;

    /**
     * 图谱描述
     */
    private String kgDescription;

    /**
     * 缩略图URL
     */
    private String kgThumbnailUrl;

    /**
     * 图谱数据配置
     */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String kgGraphConfigData;

    /**
     * 创建时间
     */
    private LocalDateTime kgCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime kgUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime kgDeletedAt;
}
