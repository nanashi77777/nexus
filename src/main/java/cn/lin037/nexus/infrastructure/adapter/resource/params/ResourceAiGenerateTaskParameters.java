package cn.lin037.nexus.infrastructure.adapter.resource.params;

import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAiGenerateTaskParameters {
    private Long resourceId;
    private ResourceSourceTypeEnum sourceType;
    private String rsPrompt;
    private Long learningSpaceId;
    private Long createdByUserId;
}