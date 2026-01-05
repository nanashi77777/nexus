package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author LinSanQi
 */
@Data
public class ChangeEmailReq {
    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String newEmail;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d+$", message = "验证码格式错误")
    private String verificationCode;
}
