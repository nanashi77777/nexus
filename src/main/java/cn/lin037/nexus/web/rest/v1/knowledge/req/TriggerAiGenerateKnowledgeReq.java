package cn.lin037.nexus.web.rest.v1.knowledge.req;

import cn.lin037.nexus.application.knowledge.enums.AiSourceTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TriggerAiGenerateKnowledgeReq {

    @NotNull
    private Long folderId;

    @NotNull
    private AiSourceTypeEnum sourceType;

    @NotNull
    private String sourceInfo;
} 