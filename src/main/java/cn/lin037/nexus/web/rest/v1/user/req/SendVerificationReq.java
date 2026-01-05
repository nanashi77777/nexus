package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author LinSanQi
 */
@Data
public class SendVerificationReq {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}
