package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.Valid;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 更新图谱节点请求
 *
 * @author LinSanQi
 */
@Data
public class UpdateGraphNodeReq {

    /**
     * 节点标题
     */
    @Length(max = 255, message = "节点标题长度不能超过255个字符")
    private String title;

    /**
     * 节点定义
     */
    @Length(max = 1000, message = "节点定义长度不能超过1000个字符")
    private String definition;

    /**
     * 节点解释
     */
    @Length(max = 2000, message = "节点解释长度不能超过2000个字符")
    private String explanation;

    /**
     * 节点公式或代码
     */
    private String formulaOrCode;

    /**
     * 节点示例解释
     */
    private String example;

    /**
     * 节点样式配置
     */
    @Valid
    private NodeStyleConfigReq styleConfig;
}

