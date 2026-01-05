package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新Agent记忆请求
 *
 * @author Lin037
 */
@Data
public class UpdateMemoryReq {

    /**
     * 记忆标题
     */
    @NotBlank(message = "记忆标题不能为空")
    @Size(max = 200, message = "记忆标题长度不能超过200个字符")
    private String title;

    /**
     * 记忆内容
     */
    @NotBlank(message = "记忆内容不能为空")
    @Size(max = 5000, message = "记忆内容长度不能超过5000个字符")
    private String content;

    /**
     * 重要性评分（1-10，10为最重要）
     */
    @Min(value = 1, message = "重要性评分最小值为1")
    @Max(value = 10, message = "重要性评分最大值为10")
    private Integer importanceScore;

    /**
     * 标签列表（用于分类和检索）
     */
    private List<String> tags;
}