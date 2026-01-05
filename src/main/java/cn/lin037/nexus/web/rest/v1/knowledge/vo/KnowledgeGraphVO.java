package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.KnowledgeGraphEntity;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识图谱视图对象 (列表用)
 *
 * @author LinSanQi
 */
@Data
public class KnowledgeGraphVO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeGraphVO fromEntity(KnowledgeGraphEntity entity) {
        KnowledgeGraphVO vo = new KnowledgeGraphVO();
        vo.setId(entity.getKgId());
        vo.setTitle(entity.getKgTitle());
        vo.setDescription(entity.getKgDescription());
        vo.setThumbnailUrl(entity.getKgThumbnailUrl());
        vo.setCreatedAt(entity.getKgCreatedAt());
        vo.setUpdatedAt(entity.getKgUpdatedAt());
        return vo;
    }
} 