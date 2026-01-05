# NEXUS项目Cursor Rules

本文档为IDE Cursor提供NEXUS项目的开发规范和自动化提示规则，涵盖整个基础设施层和各个子模块的最佳实践。

---

## 总体架构规则

### 项目分层原则

```
当你在以下目录工作时，遵循对应的架构原则：

- application/: 应用层，包含业务逻辑、用例、端口定义
  - 只能依赖：同层其他模块、common模块、JDK标准库
  - 不能依赖：infrastructure层、web层
  - 异常：只抛出ApplicationException

- infrastructure/: 基础设施层，包含技术实现、适配器
  - 可以依赖：application层接口、第三方库、技术框架
  - 不能依赖：web层
  - 异常：主要抛出InfrastructureException

- web/: 表现层，包含REST控制器、DTO
  - 可以依赖：application层接口
  - 不能依赖：infrastructure层具体实现
  - 异常：统一由GlobalExceptionHandler处理

- common/: 通用层，包含工具类、常量、公共组件
  - 不能依赖：其他业务层
  - 只能依赖：JDK标准库、通用第三方库
```

### 依赖注入规则

```java
// 推荐：使用构造函数注入
@Service
@RequiredArgsConstructor
public class ServiceImpl {
    private final Repository repository;
    private final CacheService cacheService;
}

// 避免：使用字段注入
@Autowired
private Repository repository; // 不推荐
```

---

## Infrastructure/Common模块规则

### AI模块规则

```java
// AI服务使用规范
when working with AI module:
1. 总是通过AiCoreService接口获取模型实例
2. 使用AIInfraExceptionEnum定义的异常码
3. 模型配置错误时自动将状态设为INACTIVE
4. 记录详细的模型调用日志

// 正确使用方式
@Service
@RequiredArgsConstructor
public class AiService {
    private final AiCoreService aiCoreService;
    
    public String generateContent(String prompt, String usedFor) {
        try {
            LanguageModel model = aiCoreService.getLanguageModel("gpt-4-turbo", usedFor);
            return model.generate(prompt);
        } catch (InfrastructureException e) {
            log.error("AI模型调用失败: prompt={}, usedFor={}", prompt, usedFor, e);
            throw e;
        }
    }
}
```

### 缓存模块规则

```java
// 缓存服务使用规范
when working with cache module:
1. 使用CacheKeyBuilder构建标准化的Redis Key
2. 为缓存操作设置合理的过期时间
3. 使用CacheExceptionCodeEnum定义的异常码
4. 缓存操作失败时要有降级策略

// Key构建规范
String cacheKey = CacheKeyBuilder.buildKey("user", "profile", userId);
// 结果格式: NEXUS:user:profile:123456

// 缓存操作模板
public <T> T getCachedData(String key, Supplier<T> dataSupplier, Duration expiration) {
    T cached = cacheService.get(key);
    if (cached != null) {
        return cached;
    }
    
    T data = dataSupplier.get();
    cacheService.set(key, data, expiration.toMinutes(), TimeUnit.MINUTES);
    return data;
}
```

### 文件存储模块规则

```java
// 文件存储使用规范
when working with file storage module:
1. 文件上传前检查权限和配额
2. 使用AccessLevel明确访问级别
3. 文件操作失败时清理已创建的资源
4. 使用FileExceptionCodeEnum定义的异常码

// 文件上传模板
public InfraFileMetadata uploadFile(MultipartFile file, String owner, AccessLevel accessLevel) {
    try {
        // 前置检查
        validateFileType(file);
        validateQuota(owner);
        
        // 执行上传
        return fileStorageService.upload(file, owner, accessLevel);
    } catch (InfrastructureException e) {
        log.error("文件上传失败: filename={}, owner={}", file.getOriginalFilename(), owner, e);
        throw e;
    }
}
```

### ID生成模块规则

```java
// ID生成使用规范
when working with ID generation:
1. 优先使用HutoolSnowflakeIdGenerator的静态方法
2. 在实体创建时设置ID
3. ID生成失败时使用IdGeneratorResultCodeEnum异常码

// ID生成模板
@Entity
public class SomeEntity {
    @TableId(value = IdAutoType.NONE)
    private Long id;
    
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = HutoolSnowflakeIdGenerator.generateLongId();
        }
    }
}
```

### 通知模块规则

```java
// 通知服务使用规范
when working with notification module:
1. 使用模板发送通知，避免硬编码内容
2. 注意频率限制，处理RATE_LIMIT_EXCEEDED异常
3. 使用NotificationExceptionCodeEnum定义的异常码
4. 模板变量要进行安全检查

// 通知发送模板
public void sendNotification(String to, String templateId, Map<String, Object> variables) {
    try {
        // 变量安全检查
        validateTemplateVariables(variables);
        
        // 发送通知
        notificationService.send(to, getSubject(templateId), templateId, variables);
        log.info("通知发送成功: to={}, template={}", to, templateId);
    } catch (InfrastructureException e) {
        if (e.getCode().equals(NotificationExceptionCodeEnum.RATE_LIMIT_EXCEEDED.getCode())) {
            log.warn("通知发送频率超限: to={}", to);
            // 考虑延期重试或降级处理
        } else {
            log.error("通知发送失败: to={}, template={}", to, templateId, e);
        }
        throw e;
    }
}
```

### 任务模块规则

```java
// 异步任务使用规范
when working with task module:
1. 实现TaskExecutor接口时必须指定正确的参数类型
2. 在execute方法中定期检查取消状态
3. 实现onCompletion和onFailure回调方法
4. 使用TaskExceptionCodeEnum定义的异常码
5. 长时间运行的任务要配置为long-running类型

// 任务执行器模板
@Component
public class MyTaskExecutor implements TaskExecutor<MyParams, MyResult> {
    
    @Override
    public String getTaskType() {
        return "MY_TASK_TYPE";
    }
    
    @Override
    public MyResult execute(MyParams parameters, TaskContext context) throws Exception {
        // 检查取消状态
        if (context.isCancellationRequested()) {
            throw new InfrastructureException(TaskExceptionCodeEnum.TASK_CANCELLED);
        }
        
        // 业务逻辑
        MyResult result = processTask(parameters);
        
        // 长时间运行的任务要定期检查取消状态
        if (context.isCancellationRequested()) {
            throw new InfrastructureException(TaskExceptionCodeEnum.TASK_CANCELLED);
        }
        
        return result;
    }
    
    @Override
    public Class<MyParams> getParametersType() {
        return MyParams.class;
    }
    
    @Override
    public void onCompletion(MyParams parameters, MyResult result) {
        log.info("任务执行成功: {}", result);
    }
    
    @Override
    public void onFailure(MyParams parameters, Exception exception) {
        log.error("任务执行失败: {}", parameters, exception);
    }
}
```

### 持久化模块规则

```java
// 持久化模块使用规范
when working with persistent module:
1. 实体类必须实现Serializable接口
2. 使用@TableId(value = IdAutoType.NONE)手动设置雪花ID
3. 软删除字段使用@LogicDelete注解
4. 复杂类型使用@TypeHandler处理
5. Mapper接口继承MybatisMapper<T>

// 实体类模板
@Data
@Table(value = "table_name")
public class SomeEntity implements Serializable {
    @Serial
    @Ignore
    private static final long serialVersionUID = 1L;

    @TableId(value = IdAutoType.NONE)
    private Long id;
    
    private String name;
    
    @LogicDelete(beforeValue = "", afterValue = "{NOW}")
    private LocalDateTime deletedAt;
    
    @TypeHandler(value = JsonbTypeHandler.class)
    private String jsonData;
}

// Mapper接口模板
@Mapper
public interface SomeMapper extends MybatisMapper<SomeEntity> {
    // 继承基础CRUD操作
}
```

---

## Repository接口规则

### 命名规范

```java
// Repository接口命名规范
when creating repository interfaces:
1. 接口名: 实体名 + Repository (不带复数)
2. 方法名遵循: [操作类型][目标字段][By][条件字段]

// 方法命名示例
public interface UserAccountRepository {
    // 查询方法
    Optional<UserAccountEntity> findByUsername(String username);
    Optional<Long> findUserIdByInviteCode(String inviteCode);
    
    // 存在性检查
    boolean existsByUsername(String username);
    
    // 删除方法
    Boolean deleteById(Long userId);
    
    // 保存方法
    void save(UserAccountEntity user);
    
    // 更新方法
    void updateById(Consumer<UserAccountEntity> updater);
    
    // 生成方法
    String generateUniqueInviteCode();
}
```

### 方法顺序规范

```java
// Repository方法排列顺序
interface排列顺序:
1. 查询方法 (find/exists/count)
2. 删除方法 (delete)
3. 写操作 (save/update)
4. 辅助方法 (generate/reset)
```

---

## 异常处理规则

### 异常分层使用

```java
// 异常使用规范
when handling exceptions:
1. 业务异常使用ApplicationException + ResultCodeEnum
2. 基础设施异常使用InfrastructureException + InfraExceptionCode
3. 异常消息要用户友好且安全
4. 记录完整的异常日志

// 业务异常模板
if (userRepository.existsByUsername(username)) {
    throw new ApplicationException(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS);
}

// 基础设施异常模板
try {
    externalService.call();
} catch (ExternalException e) {
    log.error("外部服务调用失败", e);
    throw new InfrastructureException(SomeInfraExceptionEnum.SERVICE_CALL_FAILED, e);
}
```

### 异常码定义规范

```java
// 异常码枚举定义规范
when defining exception enums:
1. 业务异常码格式: 模块代码 + 错误分类 + 序号
2. 基础设施异常码格式: INFRA_模块名_错误描述
3. 实现对应的接口(ResultCodeEnum或InfraExceptionCode)
4. 使用@Getter和@AllArgsConstructor注解

// 业务异常码示例
public enum UserErrorCodeEnum implements ResultCodeEnum {
    USERNAME_ALREADY_EXISTS("USER_601001", "用户名已存在"),
    EMAIL_ALREADY_EXISTS("USER_601002", "邮箱已存在");
    
    private final String code;
    private final String message;
    
    // 构造函数和getter方法
}

// 基础设施异常码示例
@Getter
@AllArgsConstructor
public enum FileInfraExceptionEnum implements InfraExceptionCode {
    FILE_NOT_FOUND("INFRA_FILE_NOT_FOUND", "文件未找到"),
    STORAGE_ERROR("INFRA_FILE_STORAGE_ERROR", "文件存储失败");
    
    private final String code;
    private final String message;
}
```

---

## 服务实现规则

### 服务类模板

```java
// 服务实现类规范
when implementing services:
1. 使用@Service注解标注
2. 使用@RequiredArgsConstructor进行依赖注入
3. 使用@Slf4j添加日志支持
4. 方法要有适当的事务边界
5. 记录关键操作的日志

@Service
@RequiredArgsConstructor
@Slf4j
public class SomeAppServiceImpl implements SomeAppService {
    
    private final SomeRepository repository;
    private final CacheService cacheService;
    
    @Override
    @Transactional
    public void createSomething(CreateCommand command) {
        log.info("开始创建资源: {}", command.getName());
        
        try {
            // 业务验证
            validateCommand(command);
            
            // 执行业务逻辑
            SomeEntity entity = buildEntity(command);
            repository.save(entity);
            
            // 清理缓存
            String cacheKey = CacheKeyBuilder.buildKey("some", "list");
            cacheService.delete(cacheKey);
            
            log.info("资源创建成功: id={}", entity.getId());
            
        } catch (ApplicationException e) {
            log.warn("资源创建失败 - 业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("资源创建失败 - 系统异常", e);
            throw new InfrastructureException(
                CommonInfraExceptionEnum.INFRA_COMMON_UNKNOWN_ERROR, e);
        }
    }
}
```

---

## 配置和部署规则

### 配置文件规范

```yaml
# 配置文件组织规范
when managing configurations:
1. 主配置文件: application.yml
2. 模块配置文件: application-模块名.yml
3. 环境配置文件: application-环境.yml
4. 敏感配置使用环境变量

# 配置示例
nexus:
  cache:
    bloomFilter:
      expectedInsertions: ${CACHE_BLOOM_EXPECTED_INSERTIONS:10000}
      falseProbability: ${CACHE_BLOOM_FALSE_PROBABILITY:0.01}
  task:
    enabled: ${TASK_ENABLED:true}
    mapping:
      long-running: ${TASK_LONG_RUNNING_TYPES:}
```

### 日志规范

```java
// 日志使用规范
when adding logs:
1. 使用@Slf4j注解
2. 记录关键业务操作的开始和结束
3. 异常日志包含完整上下文信息
4. 使用占位符而非字符串拼接

// 日志示例
log.info("开始处理用户请求: userId={}, operation={}", userId, operation);
log.warn("业务规则验证失败: userId={}, rule={}, reason={}", userId, rule, reason);
log.error("系统异常: userId={}, operation={}", userId, operation, exception);
```

---

## 测试规则

### 单元测试规范

```java
// 测试类规范
when writing tests:
1. 测试类名: 被测试类名 + Test
2. 使用@ExtendWith(MockitoExtension.class)
3. Mock外部依赖，测试业务逻辑
4. 测试方法名描述测试场景

@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {
    
    @Mock
    private UserAccountRepository userRepository;
    
    @Mock
    private CacheService cacheService;
    
    @InjectMocks
    private UserAppServiceImpl userAppService;
    
    @Test
    void createUser_should_success_when_valid_input() {
        // Given
        CreateUserCommand command = new CreateUserCommand("testuser", "test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        
        // When
        assertDoesNotThrow(() -> userAppService.createUser(command));
        
        // Then
        verify(userRepository).save(any(UserAccountEntity.class));
    }
    
    @Test
    void createUser_should_throw_exception_when_username_exists() {
        // Given
        CreateUserCommand command = new CreateUserCommand("testuser", "test@example.com");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        
        // When & Then
        ApplicationException exception = assertThrows(
            ApplicationException.class,
            () -> userAppService.createUser(command)
        );
        assertEquals(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS.getCode(), exception.getCode());
    }
}
```

---

## 代码质量规则

### 代码风格

```java
// 代码风格规范
when writing code:
1. 使用4个空格缩进
2. 大括号使用K&R风格
3. 每行最大长度120字符
4. 导入语句按包名分组排序
5. 避免使用通配符导入

// 方法长度控制
单个方法不应超过50行，如果超过应该拆分为多个私有方法

// 类职责单一
每个类应该只有一个职责，如果职责过多应该拆分
```

### 性能规则

```java
// 性能优化规范
when optimizing performance:
1. 数据库查询要添加合适的索引
2. 缓存热点数据，设置合理的过期时间
3. 大数据量操作使用分页或批处理
4. 避免N+1查询问题
5. 长时间运行的任务使用异步处理

// 缓存使用示例
public List<User> getActiveUsers() {
    String cacheKey = CacheKeyBuilder.buildKey("user", "active");
    List<User> cached = cacheService.get(cacheKey);
    if (cached != null) {
        return cached;
    }
    
    List<User> users = userRepository.findActiveUsers();
    cacheService.set(cacheKey, users, 10, TimeUnit.MINUTES);
    return users;
}
```

---

## 安全规则

### 数据安全

```java
// 数据安全规范
when handling sensitive data:
1. 密码等敏感信息必须加密存储
2. 日志中不能包含敏感信息
3. API响应不能暴露内部错误细节
4. 文件访问要进行权限检查

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

### 输入验证

```java
// 输入验证规范
when validating input:
1. 所有外部输入都要进行验证
2. 使用白名单而非黑名单验证
3. 文件上传要检查文件类型和大小
4. SQL参数使用预编译语句

// 输入验证示例
public void validateCreateUserCommand(CreateUserCommand command) {
    if (StringUtils.isBlank(command.getUsername())) {
        throw new ApplicationException(CommonResultCodeEnum.PARAM_NULL_ERROR);
    }
    
    if (!command.getUsername().matches("^[a-zA-Z0-9_-]{3,16}$")) {
        throw new ApplicationException(UserErrorCodeEnum.INVALID_USERNAME_FORMAT);
    }
    
    if (!EmailValidator.isValid(command.getEmail())) {
        throw new ApplicationException(UserErrorCodeEnum.INVALID_EMAIL_FORMAT);
    }
}
```

---

这些规则涵盖了NEXUS项目开发的各个方面，包括架构设计、模块使用、异常处理、代码质量、性能优化和安全要求。遵循这些规则将确保代码的一致性、可维护性和高质量。 