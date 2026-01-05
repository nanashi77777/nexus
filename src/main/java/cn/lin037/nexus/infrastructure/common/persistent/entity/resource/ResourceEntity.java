package cn.lin037.nexus.infrastructure.common.persistent.entity.resource;

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
 * 资源表实体，存储用户导入的原始资料元数据
 *
 * @author LinSanQi
 */
@Data
@FieldNameConstants
@Table(value = "resources")
public class ResourceEntity implements Serializable {
    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    /**
     * 主键, 资源雪花ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long rsId;

    /**
     * 所属用户的ID (外键)
     */
    private Long rsCreatedByUserId;

    /**
     * 所属学习空间的ID (外键)
     */
    private Long rsLearningSpaceId;

    /**
     * 资源标题
     */
    private String rsTitle;

    /**
     * 资源描述（用于提供给后续AI进行检索的）
     */
    private String rsDescription;

    /**
     * 来源类型 (0-'UPLOAD', 1-'LINK', 2-'AI_GENERATED')
     */
    private Integer rsSourceType;

    /**
     * 来源URI (文件路径, URL等)
     */
    private String rsSourceUri;

    /**
     * 提示语，用于后续AI进行检索的
     */
    private String rsPrompt;

    /**
     * 资源状态 (0-PENDING_PARSE, 1-PARSING, 2-PARSE_COMPLETED, 3-PARSE_FAILED)
     * @see cn.lin037.nexus.application.resource.enums.ResourceStatusEnum
     */
    private Integer rsStatus;

    /**
     * 解析失败时的错误信息
     */
    private String rsParseErrorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime rsCreatedAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime rsUpdatedAt;

    /**
     * 逻辑删除时间，为NULL表示未删除
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime rsDeletedAt;
}