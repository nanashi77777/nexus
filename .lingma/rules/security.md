# 安全规范

## 数据安全

1. **密码存储**: 必须使用 `PasswordEncryptionUtil` 进行加密存储和验证。严禁明文存储密码。
2. **日志**: 严禁在日志中记录任何敏感信息，如密码、Token、API Key等。
3. **API响应**: 严禁在API响应中暴露内部错误细节（如堆栈跟踪）。`GlobalExceptionHandler` 会统一处理。
4. **文件访问**: 所有文件访问操作（下载、删除）都必须严格检查操作者权限。

```java
// 密码处理示例
public void updatePassword(Long userId, String newPassword) {
    // 密码加密
    String encryptedPassword = PasswordEncryptionUtil.encrypt(newPassword);
    
    // 更新数据库
    userRepository.updatePassword(userId, encryptedPassword);
    
    // 日志不包含密码信息
    log.info("用户密码更新成功: userId={}", userId);
}
```

## 输入验证

1. **强制验证**: 所有外部输入（尤其是 `Controller` 的 `@RequestBody`）都必须使用 `@Valid` 注解和 JSR 303 注解进行验证。
2. **白名单原则**: 对于有固定格式或范围的输入，优先使用白名单进行验证（例如，使用正则表达式或枚举），而不是黑名单。
3. **文件上传**: 必须检查文件类型、大小和内容，防止恶意文件上传。
4. **SQL注入**: 使用 `xbatis` 或 `Mybatis` 等 ORM 框架，参数化查询，从根本上防止SQL注入。

```java
// 输入验证示例
@Data
public class LoginReq {

    @NotBlank(message = "用户名/邮箱不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,16}$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "请输入有效的用户名或邮箱")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z\\d@$!%*?&#.]{8,20}$)(?=(.*[A-Z].*)|(.*[a-z].*)|(.*\\d.*)|(.*[@$!%*?&#.].*)){2}.*$",
            message = "密码格式不正确")
    private String password;
}

// Controller
@PostMapping("/login")
public ResultVO<LoginVO> login(@Valid @RequestBody LoginReq request) {
    // ...
}
```

description:
globs:
alwaysApply: false
---
