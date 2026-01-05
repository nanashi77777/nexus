package cn.lin037.nexus.web.rest.v1.knowledge.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author LinSanQi
 */
@Data
@Schema(description = "为已有知识点生成关联关系的请求")
public class AiConnectKnowledgeReq {
    @Schema(description = "需要关联的知识点ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "知识点ID列表不能为空")
    private List<Long> knowledgePointIds;

    @Schema(description = "用户输入的额外Prompt")
    private String prompt;

    @Schema(description = "存放的位置ID，实体模式为目标文件夹ID，虚体模式为目标知识图谱ID")
    @NotNull
    private Long targetDepositId;

    @Schema(description = "是否为虚体模式")
    @NotNull
    private Boolean isVirtual;
}
