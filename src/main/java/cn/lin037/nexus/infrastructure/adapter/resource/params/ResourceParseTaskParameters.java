package cn.lin037.nexus.infrastructure.adapter.resource.params;

import cn.lin037.nexus.application.resource.enums.ResourceSourceTypeEnum;
import cn.lin037.nexus.application.resource.enums.SliceStrategyEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源解析任务参数
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceParseTaskParameters {
    private Long resourceId;
    private ResourceSourceTypeEnum sourceType;
    private String sourceUri;
    private SliceStrategyEnum sliceStrategy;
    private Long learningSpaceId;
    private Long createdByUserId;
} 