# NEXUS项目异常处理规范

本文档定义了NEXUS项目中异常处理的统一规范，包括异常分类、命名规则、使用指南和最佳实践。

---

## 1. 异常分层设计

NEXUS项目采用分层异常设计，将异常分为两个主要层级：

### 1.1. ApplicationException（应用层异常）

- **定义**: 位于 `@/src/main/java/cn/lin037/nexus/common/exception/ApplicationException.java`
- **用途**: 处理业务逻辑相关的异常
- **特点**:
    - 用户可理解的错误信息
    - 通常由业务规则验证失败引起
    - 错误码遵循业务模块命名

### 1.2. InfrastructureException（基础设施层异常）

- **定义**: 位于 `@/src/main/java/cn/lin037/nexus/infrastructure/common/exception/InfrastructureException.java`
- **用途**: 处理技术基础设施相关的异常
- **特点**:
    - 系统级错误，技术性较强
    - 通常由外部依赖或系统配置问题引起
    - 错误码遵循基础设施模块命名

---

## 2. 异常码接口规范

### 2.1. ResultCodeEnum（业务结果码接口）

- **定义**: `@/src/main/java/cn/lin037/nexus/common/constant/enums/result/ResultCodeEnum.java`
- **用途**: 定义业务层面的统一结果码规范
- **实现**: 所有业务异常枚举必须实现此接口

### 2.2. InfraExceptionCode（基础设施异常码接口）

- **定义**: `@/src/main/java/cn/lin037/nexus/infrastructure/common/exception/InfraExceptionCode.java`
- **用途**: 定义基础设施层面的统一异常码规范
- **实现**: 所有基础设施异常枚举必须实现此接口

---

## 3. 异常码命名规范

### 3.1. 业务异常码命名规则（ResultCodeEnum）

**结构**: `模块代码 + 错误分类 + 序号`

#### 3.1.1. 通用结果码（CommonResultCodeEnum）

- **成功码**: `200000` - 请求成功
- **客户端错误（4xxxxx）**:
    - `400000` - 参数错误
    - `400001` - 不存在的排序类型
    - `400002` - 必要参数为空
    - `400003` - 参数类型错误
    - `400004` - 参数格式错误
    - `400005` - 参数验证失败
    - `400006` - 缺少必要参数
    - `404000` - 请求的资源不存在
    - `405000` - 请求方法不支持
    - `409000` - 数据完整性错误
    - `429000` - 请求过于频繁
- **服务器错误（5xxxxx）**:
    - `500000` - 系统未知错误

#### 3.1.2. 业务模块异常码规范

- **用户模块**: `USER_6xxxxx`
- **资源模块**: `RESOURCE_6xxxxx`
- **ID生成模块**: `ID_607xxx`

### 3.2. 基础设施异常码命名规则（InfraExceptionCode）

**结构**: `INFRA_模块名_错误描述`

#### 3.2.1. 通用基础设施异常（CommonInfraExceptionEnum）

```java
INFRA_COMMON_UNKNOWN_ERROR("INFRA_COMMON_UNKNOWN_ERROR", "基础设施层未知错误")
INFRA_COMMON_INIT_ERROR("INFRA_COMMON_INIT_ERROR", "基础设施组件初始化失败")
INFRA_COMMON_CONFIG_ERROR("INFRA_COMMON_CONFIG_ERROR", "基础设施配置错误")
```

#### 3.2.2. AI模块异常（AIInfraExceptionEnum）

```java
UNSUPPORTED_AI_MODULE_TYPE("INFRA_AI_UNSUPPORTED_AI_MODULE_TYPE", "所选AI模块类型不支持")
PROVIDER_NOT_FOUND("INFRA_AI_PROVIDER_NOT_FOUND", "找不到指定的服务商配置")
PROVIDER_NOT_ACTIVE("INFRA_AI_PROVIDER_NOT_ACTIVE", "指定的服务商未激活")
MODEL_INSTANTIATION_FAILED("INFRA_AI_MODEL_INSTANTIATION_FAILED", "模型实例化失败")
NO_AVAILABLE_MODEL_FOUND("INFRA_AI_NO_AVAILABLE_MODEL_FOUND", "未找到可用的模型实例")
AI_RESPONSE_PARSE_ERROR("INFRA_AI_RESPONSE_PARSE_ERROR", "AI响应解析失败")
```

#### 3.2.3. 缓存模块异常（CacheExceptionCodeEnum）

```java
LUA_SCRIPT_EXECUTION_ERROR("INFRA_CACHE_LUA_SCRIPT_ERROR", "Lua脚本执行失败")
BLOOM_FILTER_INIT_FAILED("INFRA_CACHE_BLOOM_FILTER_INIT_FAILED", "布隆过滤器初始化失败")
RATE_LIMITER_INIT_FAILED("INFRA_CACHE_RATE_LIMITER_INIT_FAILED", "令牌桶初始化失败")
```

#### 3.2.4. 文件存储模块异常（FileExceptionCodeEnum）

```java
FILE_NOT_FOUND("INFRA_FILE_NOT_FOUND", "文件未找到")
ACCESS_LEVEL_NOT_FOUND("INFRA_FILE_ACCESS_LEVEL_NOT_FOUND", "文件访问级别不存在")
ACCESS_DENIED("INFRA_FILE_ACCESS_DENIED", "文件访问权限不足")
QUOTA_EXCEEDED("INFRA_FILE_QUOTA_EXCEEDED", "文件上传配额已超出")
STORAGE_ERROR("INFRA_FILE_STORAGE_ERROR", "文件存储失败")
DOWNLOAD_ERROR("INFRA_FILE_DOWNLOAD_ERROR", "文件下载失败")
DELETE_ERROR("INFRA_FILE_DELETE_ERROR", "文件删除失败")
INVALID_PATH("INFRA_FILE_INVALID_PATH", "无效的文件路径")
OWNER_MISMATCH("INFRA_FILE_OWNER_MISMATCH", "文件所有者不匹配")
INVALID_FILE_NAME("INFRA_FILE_INVALID_NAME", "无效的文件名，文件名不存在拓展名")
AMBIGUOUS_TEMPLATE_FILE("INFRA_FILE_AMBIGUOUS_TEMPLATE", "找到多个同名但不同后缀的模板文件")
```

#### 3.2.5. 通知模块异常（NotificationExceptionCodeEnum）

```java
RATE_LIMIT_EXCEEDED("INFRA_NOTIFICATION_RATE_LIMIT_EXCEEDED", "通知发送频率超出限制")
WAIT_QUEUE_FULL("INFRA_NOTIFICATION_WAIT_QUEUE_FULL", "通知发送等待队列已满")
TEMPLATE_NOT_FOUND("INFRA_NOTIFICATION_TEMPLATE_NOT_FOUND", "通知模板未找到")
TEMPLATE_RENDERING_ERROR("INFRA_NOTIFICATION_TEMPLATE_RENDERING_ERROR", "通知模板渲染失败")
SENDING_ERROR("INFRA_NOTIFICATION_SENDING_ERROR", "通知发送失败")
INVALID_CONFIGURATION("INFRA_NOTIFICATION_INVALID_CONFIGURATION", "通知服务配置无效")
```

#### 3.2.6. 任务模块异常（TaskExceptionCodeEnum）

```java
INFRA_TASK_NOT_FOUND("INFRA_TASK_NOT_FOUND", "任务不存在")
TASK_INTERRUPTED("TASK_INTERRUPTED", "任务被中断")
INFRA_TASK_CREATE_OR_UPDATE_FAILED("INFRA_TASK_CREATE_OR_UPDATE_FAILED", "任务创建或修改失败")
INFRA_TASK_EXECUTOR_NOT_FOUND("INFRA_TASK_EXECUTOR_NOT_FOUND", "找不到对应的任务执行器")
INFRA_TASK_ALREADY_RUNNING("INFRA_TASK_ALREADY_RUNNING", "任务已在运行中，无法重复启动")
INFRA_TASK_CANNOT_BE_CANCELLED("INFRA_TASK_CANNOT_BE_CANCELLED", "任务状态不正确，无法取消")
INFRA_TASK_SERIALIZATION_ERROR("INFRA_TASK_SERIALIZATION_ERROR", "任务参数或结果序列化/反序列化失败")
TASK_CANCELLED("TASK_CANCELLED", "任务被取消")
INFRA_TASK_CONFIG_ERROR("INFRA_TASK_CONFIG_ERROR", "任务模块配置错误")
INFRA_TASK_UNKNOWN_STATUS("INFRA_TASK_UNKNOWN_STATUS", "未知的任务状态")
```

---

## 4. 异常枚举使用规范

### 4.1. 枚举定义规范

#### 4.1.1. 业务异常枚举

```java
public enum UserErrorCodeEnum implements ResultCodeEnum {
    USERNAME_ALREADY_EXISTS("USER_601001", "用户名已存在"),
    EMAIL_ALREADY_EXISTS("USER_601002", "邮箱已存在"),
    INVALID_PASSWORD("USER_601003", "密码格式不正确");

    private final String code;
    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
```

#### 4.1.2. 基础设施异常枚举

```java
@Getter
@AllArgsConstructor
public enum AIInfraExceptionEnum implements InfraExceptionCode {
    PROVIDER_NOT_FOUND("INFRA_AI_PROVIDER_NOT_FOUND", "找不到指定的服务商配置"),
    MODEL_INSTANTIATION_FAILED("INFRA_AI_MODEL_INSTANTIATION_FAILED", "模型实例化失败");

    private final String code;
    private final String message;
}
```

### 4.2. 异常抛出使用规范

#### 4.2.1. 应用层异常使用

```java
// 推荐写法
if (userAccountRepository.existsByUsername(username)) {
    throw new ApplicationException(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS);
}

// 带附加信息的写法
throw new ApplicationException(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS, 
    "用户名: " + username);
```

#### 4.2.2. 基础设施层异常使用

```java
// 标准写法
throw new InfrastructureException(AIInfraExceptionEnum.PROVIDER_NOT_FOUND);

// 带原因异常的写法
throw new InfrastructureException(FileExceptionCodeEnum.STORAGE_ERROR, cause);

// 使用静态工厂方法（推荐）
throw InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_NOT_FOUND, 
    "Task ID: " + taskId);
```

---

## 5. 全局异常处理

### 5.1. GlobalExceptionHandler

- **位置**: `@/src/main/java/cn/lin037/nexus/common/exception/GlobalExceptionHandler.java`
- **功能**: 统一处理项目中的所有异常，转换为标准的API响应格式

### 5.2. 异常处理流程

1. **ApplicationException**: 直接返回业务错误码和信息
2. **InfrastructureException**: 记录详细日志，返回技术错误信息
3. **其他异常**: 记录完整堆栈，返回通用系统错误

---

## 6. 异常抛出指南

### 6.1. AppService 层

- **只能抛出 `ApplicationException`**。
- 业务规则验证失败、用户权限不足、资源状态不正确等场景。
- **示例**: `if (userRepository.exists(...)) { throw new ApplicationException(...) }`

### 6.2. Adapter 层

- `Adapter` 作为应用层和基础设施层的桥梁，其异常处理至关重要。
- **目标**: 将底层的 `InfrastructureException` 或其他技术异常，转换为对业务有意义的 `ApplicationException`，或者直接重抛
  `InfrastructureException`。

#### 6.2.1. 转换为 ApplicationException

- **场景**: 当基础设施的某个特定失败，对应一个明确的业务失败场景时。
- **示例**: `UserPresenceCheckerAdapter` 中，布隆过滤器判断用户**不存在**，这在业务上意味着“用户未找到”，因此应转换为业务异常。

```java
// 在UserPresenceCheckerAdapter中
public boolean existsByUsername(String username) {
    if (!bloomFilter.mightContain(username)) {
        // 布隆过滤器判定不存在，直接返回false，让上层业务判断
        return false; 
    }
    // 如果布隆过滤器认为可能存在，再查数据库确认
    return userAccountRepository.existsByUsername(username);
}

// 在AppService中
if (!userPresenceCheckerPort.existsByUsername(account)) {
    throw new ApplicationException(UserErrorCodeEnum.USER_NOT_FOUND);
}
```

#### 6.2.2. 重抛 InfrastructureException

- **场景**: 当发生无法恢复的技术性错误时，如数据库连接失败、Redis宕机等。
- **原则**: `Adapter` 在捕获到原始技术异常后，应将其包装成 `InfrastructureException` 并向上抛出，保留原始异常作为`cause`。

```java
// 在UserNotificationAdapter中
@Override
public void sendEmailVerification(...) {
    try {
        emailNotificationService.send(...);
    } catch (InfrastructureException e) {
        // 如果已经是InfrastructureException，直接重抛
        throw e; 
    } catch (Exception e) {
        // 将通用技术异常包装为基础设施异常
        log.error("邮件发送失败", e);
        throw new InfrastructureException(NotificationExceptionCodeEnum.SENDING_ERROR, e);
    }
}
```

### 6.3. Controller 层

- **不应处理异常**。
- 所有异常都应交由 `GlobalExceptionHandler` 统一处理。

### 6.4. 异常链使用

```java
try {
    // 外部服务调用
    externalService.call();
} catch (ExternalServiceException e) {
    // 包装为基础设施异常，保留原因链
    throw new InfrastructureException(
        AIInfraExceptionEnum.MODEL_INSTANTIATION_FAILED, e);
}
```

---

## 7. 最佳实践

### 7.1. 异常信息编写原则

- **用户友好**: ApplicationException的消息应该用户可理解
- **技术准确**: InfrastructureException的消息应该包含技术细节
- **避免敏感信息**: 不在异常消息中暴露系统内部信息

### 7.2. 异常日志记录

```java
try {
    // 业务逻辑
} catch (ApplicationException e) {
    log.warn("业务异常: {}", e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("系统异常", e);
    throw new InfrastructureException(CommonInfraExceptionEnum.INFRA_COMMON_UNKNOWN_ERROR, e);
}
```

### 7.3. 异常码分配规则

- **预留空间**: 每个模块预留足够的异常码空间
- **分类清晰**: 同类型错误使用连续的错误码
- **文档同步**: 异常码变更及时更新文档

---

## 8. 代码示例

### 8.1. 完整的业务异常处理示例

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAppServiceImpl implements UserAppService {
    
    private final UserAccountRepository userAccountRepository;
    
    @Override
    public void createUser(CreateUserCommand command) {
        log.info("开始创建用户: {}", command.getUsername());
        
        try {
            // 业务规则验证
            if (userAccountRepository.existsByUsername(command.getUsername())) {
                throw new ApplicationException(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS);
            }
            
            // 执行业务逻辑
            UserAccountEntity user = buildUserEntity(command);
            userAccountRepository.save(user);
            
            log.info("用户创建成功: userId={}", user.getUaId());
            
        } catch (ApplicationException e) {
            log.warn("用户创建失败 - 业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("用户创建失败 - 系统异常", e);
            throw new InfrastructureException(
                CommonInfraExceptionEnum.INFRA_COMMON_UNKNOWN_ERROR, e);
        }
    }
}
```

### 8.2. 基础设施层异常处理示例

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService implements NotificationService {
    
    @Override
    public void send(String to, String subject, String content) {
        try {
            // 检查频率限制
            if (!rateLimiter.tryAcquire()) {
                throw new InfrastructureException(
                    NotificationExceptionCodeEnum.RATE_LIMIT_EXCEEDED);
            }
            
            // 发送邮件
            mailSender.send(buildMimeMessage(to, subject, content));
            
        } catch (InfrastructureException e) {
            throw e;
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
            throw new InfrastructureException(
                NotificationExceptionCodeEnum.SENDING_ERROR, e);
        }
    }
}
```

---

## 9. 总结

NEXUS项目的异常处理规范强调：

1. **分层清晰**: ApplicationException处理业务异常，InfrastructureException处理技术异常
2. **码制统一**: 业务异常码和基础设施异常码有清晰的命名规范
3. **枚举规范**: 所有异常码通过枚举统一管理，实现相应接口
4. **使用一致**: 明确何时使用哪种异常类型
5. **日志完整**: 合理的日志记录策略，便于问题排查

遵循这些规范将确保项目异常处理的一致性和可维护性。 