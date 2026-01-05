package cn.lin037.nexus.web.rest.v1.knowledge.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgePointVersionEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 知识点视图对象
 *
 * @author LinSanQi
 */
@Data
public class KnowledgePointVO {
    private Long id;
    private Long folderId;
    private String title;
    private String definition;
    private String explanation;
    private String formulaOrCode;
    private String example;
    private BigDecimal difficulty;
    private Long currentVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgePointVO fromEntity(KnowledgePointEntity entity, KnowledgePointVersionEntity versionEntity) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(entity.getKpId());
        vo.setFolderId(entity.getKpFolderId());
        vo.setTitle(versionEntity.getKpvTitle());
        vo.setDefinition(versionEntity.getKpvDefinition());
        vo.setExplanation(versionEntity.getKpvExplanation());
        vo.setFormulaOrCode(versionEntity.getKpvFormulaOrCode());
        vo.setExample(versionEntity.getKpvExample());
        vo.setDifficulty(versionEntity.getKpvDifficulty() != null ? versionEntity.getKpvDifficulty() : null);
        vo.setCurrentVersionId(entity.getKpCurrentVersionId() != null ? entity.getKpCurrentVersionId() : null);
        vo.setCreatedAt(entity.getKpCreatedAt());
        vo.setUpdatedAt(entity.getKpUpdatedAt());
        return vo;
    }
} 