package cn.lin037.nexus.web.rest.v1.knowledge.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 创建知识图谱请求
 *
 * @author LinSanQi
 */
@Data
public class CreateKnowledgeGraphReq {

    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    @NotBlank(message = "知识图谱标题不能为空")
    @Length(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @Length(max = 500, message = "图谱描述长度不能超过500个字符")
    private String description;

    @Length(max = 500, message = "缩略图URL长度不能超过255个字符")
    private String thumbnailUrl;
}