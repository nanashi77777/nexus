package cn.lin037.nexus.query.isolation.vo;

import cn.lin037.nexus.infrastructure.common.persistent.entity.LearningSpaceEntity;
import cn.xbatis.db.annotations.ResultEntity;
import cn.xbatis.db.annotations.ResultEntityField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ResultEntity(LearningSpaceEntity.class)
public class LearningSpacePageVO {

    @ResultEntityField(property = LearningSpaceEntity.Fields.lsId)
    private String id;
    @ResultEntityField(property = LearningSpaceEntity.Fields.lsName)
    private String name;
    @ResultEntityField(property = LearningSpaceEntity.Fields.lsDescription)
    private String description;
    @ResultEntityField(property = LearningSpaceEntity.Fields.lsCreatedAt)
    private LocalDateTime createdAt;
}
