package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建讲解关系请求
 *
 * @author LinSanQi
 */
@Data
public class CreateExplanationRelationReq {

    /**
     * 所属讲解文档ID
     */
    @NotNull(message = "讲解文档ID不能为空")
    private Long explanationDocumentId;

    /**
     * 源知识点ID
     */
    @NotNull(message = "源知识点ID不能为空")
    private Long sourcePointId;

    /**
     * 目标知识点ID
     */
    @NotNull(message = "目标知识点ID不能为空")
    private Long targetPointId;

    /**
     * 关系类型
     */
    @NotBlank(message = "关系类型不能为空")
    @Size(max = 50, message = "关系类型长度不能超过50个字符")
    private String relationType;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 边样式配置
     */
    private Object styleConfig;
}
