package cn.lin037.nexus.web.rest.v1.explanation.req;

import cn.lin037.nexus.infrastructure.common.persistent.entity.dto.NodeStyleConfig;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

/**
 * 批量更新样式请求
 *
 * @author LinSanQi
 */
@Data
public class BatchUpdateStyleReq {

    /**
     * 讲解文档ID
     */
    private Long documentId;

    /**
     * 知识点样式更新列表
     */
    @Valid
    private List<PointStyleUpdate> pointStyles;

    /**
     * 知识点样式更新
     */
    @Data
    public static class PointStyleUpdate {
        private Long pointId;
        private NodeStyleConfig styleConfig;
    }
}
