# NEXUS 项目开发指南

本文档是 NEXUS 项目的核心开发指南，旨在为所有团队成员提供一套统一的架构、编码、安全和协作标准。所有开发活动都应严格遵守本指南。

## 1. 核心架构与分层原则

项目采用经典的四层领域驱动设计（DDD）架构，严格遵循单向依赖原则。

- **`web` (表现层)**
    - **职责**: 包含 RESTful Controller，负责处理 HTTP 请求，调用应用层服务，并返回统一的 `ResultVO<R>` 进行响应。
    - **实现**:
        - Controller 接收以 `...Req.java` 结尾的 DTO 对象作为请求体。
        - **必须** 使用 `@Valid` 注解对请求 DTO 进行输入验证。
        - **严禁** 在 Controller 方法参数中直接传递 `userId`。
    - **依赖**: 只能依赖 `application` 层的接口。
    - **另外**：对于较为复杂的req，请使用直接提供req对象给appService。
    - **放置的代码**：应该在其下放置`...Req.java`对象而非`application`层放置。

- **`application` (应用层)**
    - **职责**: 定义业务用例（Use Case），编排领域逻辑。它是业务的核心。
        - `port/`: 定义端口（接口），如 `...Repository.java`、`...Port.java`，描述了业务需要什么样的基础设施能力。
        - `service/`: 应用服务，实现具体的业务流程，是端口的调用方。
    - **实现**:
        - 通过 `StpUtil.getLoginIdAsLong()` 安全地获取当前登录用户的 ID。
        - 业务逻辑验证失败时，**必须** 抛出 `ApplicationException`异常，并且使用枚举参数来构造异常（枚举需要继承
          `ResultCodeEnum`）。
    - **依赖**: 只能依赖同层的其他模块、`common` 层、`web`层的req和vo对象。**严禁** 依赖 `infrastructure` 层。

- **`infrastructure` (基础设施层)**
    - **职责**: 提供所有技术细节的实现，是应用层 `port` 接口的具体实现方。
        - `adapter/`: 适配器，实现 `application` 层定义的端口，例如 `...RepositoryImpl.java`、`...Adapter.java`。
        - `common/`: 项目范围内的通用基础设施模块（如缓存、AI、文件存储等）。
    - **实现**:
        - 将底层的技术异常（如 `SQLException`, `IOException`）捕获并包装为 `InfrastructureException` 抛出。
    - **依赖**: 可以依赖 `application` 层的接口、第三方库（如 `xbatis`, `langchain4j`）。
    - **另外**：对于RepositoryImpl，应当在其中使用Mapper进行数据库的操作，并且要注意，该mapper是来自于Xbatis框架的，切勿使用Spring-jpa的逻辑。

- **`common` (通用层)**
    - **职责**: 存放与业务无关的通用工具类、常量、公共模型（如 `ResultVO`）。
    - **依赖**: **严禁** 依赖任何其他业务层。

### 依赖注入

- **强制** 使用构造函数注入，不复杂的情况下可以配合 Lombok 的 `@RequiredArgsConstructor`。
- **严禁** 使用 `@Autowired` 进行字段注入。

```java
// 正确示例
@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {
    private final UserAccountRepository userAccountRepository;
    private final UserNotificationPort userNotificationPort;
}
```

## 2. 命名与编码规范

- **代码风格**:
    - **缩进**: 4个空格。
    - **大括号**: K&R 风格（左大括号不换行）。
    - **行长**: 不超过 120 字符。
    - **导入**: 避免使用 `*` 通配符。
- **类与方法**:
    - **单一职责**: 每个类和方法都应只做一件事。
    - **方法长度**: 尽量不超过 50 行，过长则应重构为多个私有方法。
- **包命名**:
    - `.../application/{module}/port`: 应用层接口
    - `.../application/{module}/service`: 应用层服务
    - `.../infrastructure/adapter/{module}`: 基础设施层适配器
    - `.../web/rest/v1/{module}`: RESTful API 控制器

## 3. 数据持久化 (Repository) 规范

- **框架**: 使用 `xbatis`，一个基于 Mybatis 的增强 ORM 框架。
- **接口命名**: `实体名(单数) + Repository`，例如 `UserAccountRepository`。
- **方法命名**: 遵循 `[操作类型][目标字段][By][条件字段]` 结构。
    - **查询**: `find...`, `exists...`, `count...`
    - **写入**: `save`, `update...`
    - **删除**: `delete...`
    - **具体业务逻辑**: `generate...`, `move...`
- **返回值**:
    - 查询单个对象: `Optional<T>`
    - 查询列表: `List<T>`
    - 检查存在性: `boolean`
    - 更新/删除: `void` 或 `Boolean`
    - 数量：`int`
- **实现**: Repository 接口的实现类位于 `infrastructure/adapter/{module}` 目录下，命名为 `...RepositoryImpl.java`。

```java
// application/user/port/UserAccountRepository.java
public interface UserAccountRepository {
    Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);
    boolean existsByUsername(String username);
    void save(UserAccountEntity user);
    Boolean deleteById(Long userId);
}
```

## 4. 异常处理机制

遵循清晰的、分层的异常处理策略。

1. **Controller (web)**: 不处理异常，由全局处理器负责。
2. **AppService (application)**:
    - **只抛出 `ApplicationException`**。
    - 此异常代表一个明确的业务错误（如“用户名已存在”）。
    - 必须使用预定义的业务错误码 `...ErrorCodeEnum.java` 来构造异常。
    - 枚举参数必须实现`cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum`接口;
3. **Adapter (infrastructure)**:
    - 负责捕获所有技术相关的异常（数据库连接失败、文件读写错误、API调用超时等）。
    - 将捕获的技术异常包装成 `InfrastructureException` 向上抛出。
    - 枚举参数必须实现`cn.lin037.nexus.infrastructure.common.exception.InfraExceptionCode`接口;
4. **GlobalExceptionHandler (common)**:
    - 最终捕获所有未处理的异常。
    - 将 `ApplicationException` 转换为对用户友好的、包含错误码的 `ResultVO`。
    - 将 `InfrastructureException` 和其他未知异常记录详细日志，并返回统一的“系统内部错误”响应。

```java
// AppService 示例
if (userPresenceCheckerPort.existsByUsername(request.getUsername())) {
    throw new ApplicationException(UserErrorCodeEnum.USERNAME_EXISTS);
}

// Adapter 示例
try {
    // ... 调用第三方服务
} catch (Exception e) {
    throw new InfrastructureException(NotificationExceptionCodeEnum.SENDING_ERROR, e);
}
```

## 5. 基础设施 (`infrastructure/common`) 核心模块使用指南

`infrastructure/common` 提供了强大的、可复用的技术能力。

- **缓存 (`common/cache`)**:
    - **服务**: `CacheService`, `RateLimiterService`, `BloomFilterService`。
    - **规则**:
        - **强制** 使用 `CacheKeyBuilder` 构建缓存键，格式为 `SYSTEM:MODULE:KEY`。
        - 使用 `BloomFilterService` 进行存在性预判，防止缓存穿透。
        - 参考 `UserVerificationAdapter` 实现复杂的验证码和速率限制逻辑。

- **AI (`common/ai`)**:
    - **服务**: `AiCoreService` (获取模型), `StructuredOutputTool` (获取结构化输出), `VectorizationTool` (向量化)。
    - **规则**:
        - **必须** 通过 `AiCoreService` 获取 AI 模型实例。
        - 当需要 AI 返回特定 Java 对象时，**必须** 使用 `StructuredOutputTool`。
        - 所有向量化操作应通过应用层的 `VectorPort` 接口进行。

- **文件 (`common/file`)**:
    - **服务**: `FileStorageService`。
    - **规则**:
        - **严禁** 直接注入 `FileStorageService`，**必须** 通过 `FilePort` -> `FileAdapter` 的方式调用。
        - 上传文件时，**必须** 指定 `ownerIdentifier` 和 `AccessLevel` (访问级别)。

- **ID生成 (`common/id`)**:
    - **服务**: `HutoolSnowflakeIdGenerator`。
    - **规则**: 无需注入，直接通过静态方法 `HutoolSnowflakeIdGenerator.generateId()` 获取全局唯一的 `String` 类型 ID。

- **后台任务 (`common/task`)**:
    - **用途**: 用于执行异步、后台或耗时的操作（如 AI 内容生成、文件解析）。
    - **实现步骤**:
        1. **定义参数**: 创建一个可序列化为 JSON 的参数类（POJO）。
        2. **实现 `TaskExecutor`**:
            - `getTaskType()`: 返回一个全局唯一的任务类型字符串常量。
            - `execute()`: 实现核心任务逻辑，并使用 `TaskContext` 检查取消请求。
        3. **配置长时任务**: 对于耗时任务（如 I/O 或 AI 调用），需在 `application-task.yml` 的
           `nexus.task.mapping.long-running` 列表中添加其任务类型字符串，使其在专用线程池中运行。
    - **事务**: `execute` 方法本身非事务性。**必须** 使用 `TransactionTemplate` 或调用其他 `@Transactional`
      服务来确保数据库状态的原子性更新。

## 6. 安全规范

- **密码**: **必须** 使用 `PasswordEncryptionUtil` 进行加密和验证。
- **输入验证**: Controller 层所有 `@RequestBody` **必须** 使用 `@Valid` 和 JSR 303 注解（如 `@NotBlank`, `@Pattern`）进行验证。
- **SQL注入**: Mybatis/xbatis 的参数化查询机制已从根本上防御了 SQL 注入，保持使用即可。
- **日志**: **严禁** 在日志中记录密码、Token、API Key 等任何敏感信息。
- **文件**: 文件上传时**必须**检查类型和大小。文件访问时**必须**检查所有权。

## 7. 配置管理

- **文件结构**:
    - `application.yml`: 主配置，用于激活 profiles 和包含其他模块。
    - `application-{module}.yml`: 模块化配置，如 `application-task.yml`。
    - `application-{profile}.yml`: 环境相关配置，如 `application-dev.yml`。
- **命名空间**: 项目自定义配置**必须**放在 `nexus.*` 根命名空间下。
- **敏感信息**:
    - **严禁** 将任何密码、密钥硬编码在配置文件中。
    - **必须** 使用环境变量注入，格式为 `${ENV_VARIABLE_NAME:defaultValue}`。

```yaml
# application-prod.yml
ai:
  providers:
    - name: "openai"
      type: "OPEN_AI"
      config:
        apiKey: "${OPENAI_API_KEY}" # 从环境变量获取
```

## 8. 项目核心依赖

- **Web**: `spring-boot-starter-web`, `springdoc-openapi` (API文档)
- **认证与授权**: `sa-token`
- **持久化**: `xbatis` (ORM), `postgresql` (数据库), `druid` (连接池)
- **缓存**: `redisson` (Redis客户端)
- **AI**: `langchain4j` (核心框架), `langchain4j-open-ai`, `langchain4j-community-dashscope`
- **工具**: `lombok`, `hutool`, `jtokkit` (Token计算)
- **验证**: `spring-boot-starter-validation`

## 9. 实体类Entity与数据库表

- 实体类Entity与数据库表的映射关系**必须**使用`xbatis`的注解。
- 实体类Entity的字段**必须**使用`lombok`的注解。
- 实体类Entity的字段**必须**使用`xbatis`的注解。

## 10. 特别说明与外部资源

- **持续学习**: 本项目技术栈更新快，尤其在 AI 领域。遇到未知问题或需要实现复杂功能时，在动手编码前，**务必优先查阅相关框架的官方文档
  **。
- **DeepWiki**: 对于特定或专有的业务逻辑和算法，如果遇到困难，请**联网搜索 DeepWiki 相关文档**以获取解决方案和最佳实践。
- **代码审查**: 所有代码在合并到 `dev` 分支前，必须经过至少一位其他团队成员的 Code Review。

## 11. 表结构设计规范

1. 表命名

- 使用小写英文，多个单词使用下划线 _ 分隔。
- 表名具有明确业务含义，如 resources 表示资源表，resource_chunks 表示资源分片表。
- 多个单词组成的表名，采用复数形式，如 resources。

2. 字段命名前缀

- 字段名使用小写英文，多个单词使用下划线 _ 分隔。
- 字段名具有明确业务含义，如 rs_id 表示资源ID，rc_content 表示分片内容。
- 表字段使用统一前缀，如：
    - 资源表字段前缀为 rs_（Resource表）。
    - 资源分片表字段前缀为 rc_（Resource Chunks表）。

3. 主键

- 每张表都有一个主键字段，命名格式为 <prefix>_id，如 rs_id, rc_id。
- 主键类型为 BIGINT，使用后端的 `HutoolSnowflakeIdGenerator` 生成的雪花ID。

4. Comment 注释

- 每张表和每个字段都添加 COMMENT 注释，描述其用途和含义。
- 语法：COMMENT ON COLUMN 表名.字段名 IS '注释'。
- 注释内容清晰，包括字段业务含义、可空性说明、状态值含义等。

5. 枚举类型字段，如状态字段

- 字段使用 SMALLINT 类型，表示状态码（如 0, 1, 2）。
- 实际状态映射（枚举值）在后端代码中定义，数据库中不使用枚举类型。
- Entity 实体类中状态字段使用 Integer 类型，不直接使用枚举类型。
- 值含义应在对应的枚举类中明确定义，如：0-草稿，1-已发布，2-已归档等。

6. 逻辑删除

- 使用字段 xxx_deleted_at（如 rs_deleted_at, rc_deleted_at）表示逻辑删除。
- 字段类型为 TIMESTAMP，默认值为 NULL，表示未删除；删除时设置为当前时间戳。

7. 时间字段

- 所有表都包含以下时间字段：
    - xxx_created_at：创建时间，字段类型为 TIMESTAMP，默认值为 NOW()。
    - xxx_updated_at：最后修改时间，字段类型为 TIMESTAMP，默认值为 NOW()，并自动更新。
    - xxx_deleted_at：逻辑删除时间（可选）。

8. 索引

- 根据业务需求，合理添加索引。

---
*本指南是动态的，将随着项目的发展而持续更新。*
