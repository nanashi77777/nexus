package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码请求
 *
 * @author LinSanQi
 */
@Data
public class ChangePasswordReq {

    @NotBlank(message = "旧密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码必须为8-20个字符，并包含至少两种以下类型：大写字母、小写字母、数字、特殊字符(@$!%*?&#.)")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码必须为8-20个字符，并包含至少两种以下类型：大写字母、小写字母、数字、特殊字符(@$!%*?&#.)")
    private String newPassword;

}
