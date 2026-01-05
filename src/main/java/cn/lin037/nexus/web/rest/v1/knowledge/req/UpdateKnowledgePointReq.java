package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

@Data
public class UpdateKnowledgePointReq {

    @NotBlank
    @Length(min = 1, max = 255, message = "知识点标题长度不能超过255个字符")
    private String title;

    @Length(max = 1000, message = "节点定义长度不能超过1000个字符")
    private String definition;

    private String explanation;

    private String formulaOrCode;

    private String example;

    @Min(value = 0, message = "难度值必须在0到1之间")
    @Max(value = 1, message = "难度值必须在0到1之间")
    private BigDecimal difficulty;
} 