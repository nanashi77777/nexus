package cn.lin037.nexus.web.rest.v1.isolation.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author GitHub Copilot
 */
@Data
public class CreateLearningSpaceReq {
    /**
     * 学习空间的名称
     */
    @NotBlank(message = "学习空间名称不能为空")
    @Size(max = 100, message = "学习空间名称不能超过100个字符")
    private String name;

    /**
     * 对学习空间的详细描述
     */
    @Size(max = 512, message = "学习空间描述不能超过512个字符")
    private String description;

    /**
     * 空间内AI的全局参考Prompt
     */
    private String spacePrompt;

    /**
     * 空间封面图URL
     */
    @Size(max = 255, message = "封面图片URL不能超过255个字符")
    private String coverImageUrl;
}

