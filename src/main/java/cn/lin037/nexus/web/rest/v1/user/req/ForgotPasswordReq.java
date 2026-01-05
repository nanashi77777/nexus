package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码请求
 *
 * @author LinSanQi
 */
@Data
public class ForgotPasswordReq {

    /**
     * 用户邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /*
     * 忘记密码时，用户名不能为空
     * 不清楚是否需要验证用户名
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = "用户名必须是3-16个字符，只能包含字母、数字、下划线和连字符")
    private String username;
    */
}