package cn.lin037.nexus.web.rest.v1.user.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author LinSanQi
 */
@Data
public class DeactivateAccountReq {

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码必须为8-20个字符，并包含至少两种以下类型：大写字母、小写字母、数字、特殊字符(@$!%*?&#.)")
    private String password;

    // TODO：后续将原因进行记录
    private String deactivateReason;
}
