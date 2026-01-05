package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建Agent记忆请求
 *
 * @author Lin037
 */
@Data
public class CreateMemoryReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 会话ID（可选，如果为空则为全局记忆）
     */
    private Long sessionId;

    /**
     * 记忆等级
     * 0: 不启用
     * 1: 会话级
     * 2: 全局
     */
    @NotNull(message = "记忆等级不能为空")
    @Min(value = 0, message = "记忆等级最小值为0")
    @Max(value = 2, message = "记忆等级最大值为2")
    private Integer level;

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

    /**
     * 记忆来源
     * chat: 聊天
     * learning: 学习
     * manual: 手动添加
     */
    private String source;
}