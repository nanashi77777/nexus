package cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.LogicDelete;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 知识点文件夹实体
 *
 * @author LinSanQi
 */
@Data
@Table("knowledge_folders")
public class KnowledgeFolderEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件夹ID
     */
    @TableId(value = IdAutoType.NONE)
    private Long kfId;

    /**
     * 学习空间ID
     */
    private Long kfLearningSpaceId;

    /**
     * 创建者用户ID
     */
    private Long kfCreatedByUserId;

    /**
     * 父文件夹ID
     */
    private Long kfParentId;

    /**
     * 文件夹名称
     */
    private String kfName;

    /**
     * 文件夹层级（0-5，0为根目录，最高为5级）
     */
    private Integer kfLevel;

    /**
     * 创建时间
     */
    private LocalDateTime kfCreatedAt;

    /**
     * 更新时间
     */
    private LocalDateTime kfUpdatedAt;

    /**
     * 删除时间
     */
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime kfDeletedAt;

} 