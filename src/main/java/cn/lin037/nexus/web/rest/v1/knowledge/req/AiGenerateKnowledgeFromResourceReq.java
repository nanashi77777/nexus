package cn.lin037.nexus.web.rest.v1.knowledge.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author LinSanQi
 */
@Data
@Schema(description = "从资源和用户Prompt中生成知识点的请求")
public class AiGenerateKnowledgeFromResourceReq {

    @Schema(description = "需要参考的分片ID列表")
    @Size(max = 100, message = "分片ID列表长度不能超过100")
    private List<Long> chunkIds;

    @Schema(description = "资源ID，如果分片ID列表为空，则使用该资源的所有分片")
    private Long resourceId;

    @Schema(description = "用户输入的额外Prompt")
    @NotBlank(message = "用户输入的额外Prompt不能为空")
    @Size(min = 1, max = 1000, message = "用户输入的额外Prompt长度必须在1到1000之间")
    private String prompt;

    @Schema(description = "存放的位置ID，实体模式为目标文件夹ID，虚体模式为目标知识图谱ID")
    @NotNull(message = "存放的位置ID不能为空")
    private Long targetDepositId;

    @Schema(description = "是否为虚体模式")
    @NotNull(message = "是否为虚体模式不能为空")
    private Boolean isVirtual;
}
