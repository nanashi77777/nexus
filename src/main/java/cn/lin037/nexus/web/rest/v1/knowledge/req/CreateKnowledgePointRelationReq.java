package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CreateKnowledgePointRelationReq {

    @NotNull
    private Long learningSpaceId;

    @NotNull
    private Long sourcePointId;

    @NotNull
    private Long targetPointId;

    @NotBlank
    @Length(min = 1, max = 50, message = "关系类型长度不能超过50个字符")
    private String relationType;

    private String description;
} 