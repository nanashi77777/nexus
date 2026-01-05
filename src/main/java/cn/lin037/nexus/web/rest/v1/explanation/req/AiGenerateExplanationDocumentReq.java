package cn.lin037.nexus.web.rest.v1.explanation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI生成讲解文档请求
 *
 * @author LinSanQi
 */
@Data
public class AiGenerateExplanationDocumentReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 讲解文档标题
     */
    @NotBlank(message = "讲解文档标题不能为空")
    @Size(max = 200, message = "讲解文档标题长度不能超过200个字符")
    private String title;

    /**
     * 讲解文档描述
     */
    @Size(max = 1000, message = "讲解文档描述长度不能超过1000个字符")
    private String description;

    /**
     * 资源分片ID列表
     */
//    @NotEmpty(message = "资源分片ID列表不能为空")
    private List<Long> chunkIdList;

    /**
     * 知识点ID列表
     */
//    @NotEmpty(message = "知识点ID列表不能为空")
    private List<Long> knowledgePointIdList;

    /**
     * 知识点关系ID列表
     */
    private List<Long> knowledgeRelationIdList;

    /**
     * 用户额外要求的prompt
     */
    @Size(max = 2000, message = "用户prompt长度不能超过2000个字符")
    private String userPrompt;
}