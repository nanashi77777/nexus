package cn.lin037.nexus.web.rest.v1.knowledge.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * @author LinSanQi
 */
@Data
@Schema(description = "拓展已有知识点的请求")
public class AiExpandKnowledgeReq {

    @Schema(description = "需要拓展的知识点ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "知识点ID列表不能为空")
    @Length(min = 1, max = 100, message = "知识点ID数量必须在1到100之间")
    private List<Long> knowledgePointIds;

    @Schema(description = "分片ID列表")
    @NotEmpty
    @Length(min = 1, max = 50, message = "分片ID列表长度必须在1到50之间")
    private List<Long> chunkIds;

    @Schema(description = "用户输入的额外Prompt")
    private String prompt;

    @Schema(description = "存放的位置ID，实体模式为目标文件夹ID，虚体模式为目标知识图谱ID")
    @NotNull
    private Long targetDepositId;

    @Schema(description = "是否为虚体模式")
    @NotNull
    private Boolean isVirtual;
}
