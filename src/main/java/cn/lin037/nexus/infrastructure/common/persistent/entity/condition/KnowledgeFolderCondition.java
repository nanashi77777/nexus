package cn.lin037.nexus.infrastructure.common.persistent.entity.condition;

import cn.lin037.nexus.infrastructure.common.persistent.entity.knowledge.KnowledgeFolderEntity;
import cn.xbatis.db.annotations.Condition;
import cn.xbatis.db.annotations.ConditionTarget;
import lombok.Data;

@Data
@ConditionTarget(value = KnowledgeFolderEntity.class)
public class KnowledgeFolderCondition {

    @Condition(value = Condition.Type.EQ, property = "kf_id")
    private Long folderId;

    @Condition(value = Condition.Type.EQ, property = "kf_created_by_user_id")
    private Long userId;

    @Condition(value = Condition.Type.EQ, property = "kf_learning_space_id")
    private Long learningSpaceId;

    @Condition(value = Condition.Type.EQ, property = "kf_parent_id")
    private Long parentId;

    @Condition(value = Condition.Type.EQ, property = "kf_name")
    private String folderName;

    @Condition(value = Condition.Type.EQ, property = "kf_level")
    private Integer level;
}