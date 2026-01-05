package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户登录请求
 *
 * @author LinSanQi
 */
@Data
public class LoginReq {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名/邮箱不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "请输入有效的用户名或邮箱")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码必须为8-20个字符，并包含至少两种以下类型：大写字母、小写字母、数字、特殊字符(@$!%*?&#.)")
    private String password;
}
