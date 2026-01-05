package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 创建虚体节点请求
 *
 * @author LinSanQi
 */
@Data
public class CreateVirtualNodeReq {
    @NotNull(message = "所属图谱ID不能为空")
    private Long graphId;

    @NotBlank(message = "节点标题不能为空")
    @Length(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @Length(max = 1000, message = "定义长度不能超过1000个字符")
    private String definition;

    @Length(max = 2000, message = "解释内容长度不能超过2000个字符")
    private String explanation;

    @Length(max = 1000, message = "公式或代码长度不能超过1000个字符")
    private String formulaOrCode;

    @Length(max = 1000, message = "示例长度不能超过1000个字符")
    private String example;

    @Valid
    private NodeStyleConfigReq styleConfig;
}

