package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建Agent聊天会话请求
 *
 * @author Lin037
 */
@Data
public class CreateSessionReq {

    /**
     * 学习空间ID
     */
    @NotNull(message = "学习空间ID不能为空")
    private Long learningSpaceId;

    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String title;

    /**
     * 会话类型
     * 1: 普通聊天 (CHAT)
     * 2: 学习会话 (LEARNING)
     */
    @NotNull(message = "会话类型不能为空")
    private Integer type;

    /**
     * 会话所属
     * 1: 图谱 (GRAPH)
     * 2: 讲解 (EXPLANATION)
     * 3: 笔记 (NOTE)
     * 4: 学习 (LEARNING)
     */
    @NotNull(message = "会话所属不能为空")
    private Integer belongsTo;

    /**
     * 自动调用工具的权限
     * 1: 只读
     * 2: 只写
     * 3: 可读可写
     * 4: 关闭
     */
    private Integer isAutoCallTool;
}