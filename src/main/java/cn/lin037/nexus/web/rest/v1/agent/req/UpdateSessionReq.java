package cn.lin037.nexus.web.rest.v1.agent.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新Agent聊天会话请求
 *
 * @author Lin037
 */
@Data
public class UpdateSessionReq {

    /**
     * 会话标题
     */
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 200, message = "会话标题长度不能超过200个字符")
    private String title;

    /**
     * 自动调用工具的权限
     * 1: 只读
     * 2: 只写
     * 3: 可读可写
     * 4: 关闭
     */
    private Integer isAutoCallTool;
}