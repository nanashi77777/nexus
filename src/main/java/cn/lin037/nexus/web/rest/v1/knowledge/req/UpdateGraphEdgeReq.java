package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新图谱边请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateGraphEdgeReq {
    /**
     * 边ID
     */
    @NotNull(message = "边ID不能为空")
    private Long edgeId;

    /**
     * 关系类型
     */
    @Length(min = 1, max = 50, message = "关系类型长度不能超过50个字符")
    private String relationType;

    /**
     * 关系描述
     */
    @Length(min = 1, max = 500, message = "关系描述长度不能超过500个字符")
    private String description;

    /**
     * 边样式配置
     */
    private String styleConfig;
}

