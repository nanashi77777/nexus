package cn.lin037.nexus.application.user.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应视图对象
 *
 * @author LinSanQi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 令牌名称
     */
    private String tokenName;

    /**
     * 令牌值
     */
    private String tokenValue;
}
