package cn.lin037.nexus.web.rest.v1.knowledge.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author LinSanQi
 */
@Data
@Schema(description = "根据主题生成知识体系的请求")
public class AiGenerateKnowledgeTopicReq {
    @Schema(description = "用户输入的主题或要求", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Prompt不能为空")
    private String prompt;

    @Schema(description = "存放的位置ID，实体模式为目标文件夹ID，虚体模式为目标知识图谱ID")
    @NotNull
    private Long targetDepositId;

    @Schema(description = "是否为虚体模式")
    @NotNull
    private Boolean isVirtual;
}
