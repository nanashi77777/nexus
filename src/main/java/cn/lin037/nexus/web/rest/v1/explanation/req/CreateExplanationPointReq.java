package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建讲解知识点请求
 *
 * @author LinSanQi
 */
@Data
public class CreateExplanationPointReq {

    /**
     * 所属讲解文档ID
     */
    @NotNull(message = "讲解文档ID不能为空")
    private Long explanationDocumentId;

    /**
     * 知识点标题
     */
    @NotBlank(message = "知识点标题不能为空")
    @Size(max = 200, message = "知识点标题长度不能超过200个字符")
    private String title;

    /**
     * 知识点定义
     */
    private String definition;

    /**
     * 知识点解释
     */
    private String explanation;

    /**
     * 公式或代码
     */
    private String formulaOrCode;

    /**
     * 使用示例
     */
    private String example;

    /**
     * 节点样式配置
     */
    private Object styleConfig;
}
