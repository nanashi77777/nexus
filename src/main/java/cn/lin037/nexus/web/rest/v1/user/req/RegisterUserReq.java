package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册请求
 *
 * @author LinSanQi
 */
@Data
public class RegisterUserReq {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$", message = "用户名必须是3-16个字符，只能包含字母、数字、下划线和连字符")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码必须为8-20个字符，并包含至少两种以下类型：大写字母、小写字母、数字、特殊字符(@$!%*?&#.)")
    private String password;

    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "邀请码格式错误")
    private String inviteCode;

    @Pattern(regexp = "^\\d+$", message = "验证码格式错误")
    private String verificationCode;
}
