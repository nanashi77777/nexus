package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateKnowledgePointRelationReq {

    @NotBlank
    @Size(min = 1, max = 50, message = "关系类型长度不能超过50个字符")
    private String relationType;

    private String description;
}
