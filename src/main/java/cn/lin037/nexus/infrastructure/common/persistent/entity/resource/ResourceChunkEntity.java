package cn.lin037.nexus.infrastructure.common.persistent.entity.resource;

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
 * 资源分片表实体
 *
 * @author LinSanQi
 */
@Data
@FieldNameConstants
@Table(value = "resource_chunks")
public class ResourceChunkEntity implements Serializable {
    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 主键, 分片雪花ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long rcId;

    /**
     * 所属学习空间的ID (外键)
     */
    private Long rcLearningSpaceId;

    /**
     * 所属资源的ID (外键)
     */
    private Long rcResourceId;

    /**
     * 所属用户的ID (外键)
     */
    private Long rcCreatedByUserId;

    /**
     * 分片的文本内容
     */
    private String rcContent;

    /**
     * 所在的页码索引
     */
    private Integer rcPageIndex;

    /**
     * 在页内的分片顺序索引
     */
    private Integer rcChunkIndex;

    /**
     * 预估的Token数量
     */
    private Integer rcTokenCount;

    /**
     * 关键词列表
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> rcKeywords;

    /**
     * 关联的向量数据库中的ID, 可为NULL
     */
    private String rcVectorId;

    /**
     * 向量维度
     */
    private Integer rcVectorDimension;

    /**
     * 标记是否已向量化, 默认 FALSE
     */
    private Boolean rcIsVectorized;

    /**
     * 创建时间
     */
    private LocalDateTime rcCreatedAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime rcUpdatedAt;

    /**
     * 逻辑删除时间, 为NULL表示未删除
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime rcDeletedAt;
} 