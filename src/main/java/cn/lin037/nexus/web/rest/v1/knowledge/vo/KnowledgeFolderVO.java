package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点文件夹视图对象
 *
 * @author LinSanQi
 */
@Data
public class KnowledgeFolderVO {
    private Long id;
    private Long parentId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeFolderVO fromEntity(KnowledgeFolderEntity entity) {
        KnowledgeFolderVO vo = new KnowledgeFolderVO();
        vo.setId(entity.getKfId());
        vo.setParentId(entity.getKfParentId());
        vo.setName(entity.getKfName());
        vo.setCreatedAt(entity.getKfCreatedAt());
        vo.setUpdatedAt(entity.getKfUpdatedAt());
        return vo;
    }
} 