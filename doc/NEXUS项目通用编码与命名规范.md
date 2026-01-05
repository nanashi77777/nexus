# NEXUS项目通用编码与命名规范

## 1. 包（Package）命名规范

### 1.1. 包命名结构

- **全部小写字母**，使用 `.` 分隔。
- **根包名**: `cn.lin037.nexus`。
- **子包规范**:
    - `application`: 应用层，存放业务逻辑、用例
    - `infrastructure`: 基础设施层，存放技术实现
    - `web`: 表现层，存放REST控制器
    - `common`: 通用工具类和公共组件

### 1.2. 示例

```
cn.lin037.nexus.application.user.service
cn.lin037.nexus.infrastructure.common.cache
cn.lin037.nexus.web.rest.v1.user
```

---

## 2. 类（Class）命名规范

### 2.1. 通用类命名规则

- **采用大驼峰命名法（PascalCase）**。
- **类名应为名词或名词短语**，清晰表达类的职责。
- **避免缩写**，优先选择完整的单词。

### 2.2. 分层架构类命名约定

#### 2.2.1. 应用层（Application Layer）

- **应用服务**: 以 `AppService` 结尾，其实现类以 `AppServiceImpl` 结尾。
    - *示例*: `UserAppService`, `UserAppServiceImpl`
- **查询服务**：以 `QueryService` 结尾，其实现类以 `QueryServiceImpl` 结尾。
- **领域实体**: 直接使用领域名词。
    - *示例*: `Resource`, `UserAccount`
- **端口接口（Port）**: 以 `Port` 或 `Repository` 结尾。
    - *示例*: `FilePort`, `UserAccountRepository`

#### 2.2.2. 基础设施层（Infrastructure Layer）

- **适配器**: 以 `Adapter` 或 `RepositoryImpl` 结尾。
    - *示例*: `FileAdapter`, `UserAccountRepositoryImpl`
- **配置类**: 以 `Config` 结尾。
    - *示例*: `RedisConfig`, `TaskProperties`
- **异常类**: 以 `Exception` 结尾。
    - *示例*: `ApplicationException`, `InfrastructureException`
- **数据实体类（Entity）**: 命名为 `非复数表名 + Entity`。
    - *示例*: `UserAccountEntity`, `ResourceEntity`
- **工具类**: 以 `Utils` 或 `Util` 结尾，通常提供静态方法。
    - *示例*: `RepositoryUtils`

#### 2.2.3. Web层

- **控制器**: 以 `Controller` 结尾。
    - *示例*: `UserController`, `ResourceController`

### 2.3. Repository接口命名规范

Repository接口作为应用层与基础设施层之间的重要端口，遵循特定的命名模式：

#### 2.3.1. 接口命名规则

- **类名命名**: 接口名称应为 `实体名(不带复数) + Repository`
    - *示例*: `UserAccountRepository`, `ResourceRepository`, `LearningSpaceRepository`
- **表示职责**: 对某一实体的持久化操作管理

#### 2.3.2. 方法命名规范

**基本命名结构**:

```
[操作类型][目标字段][By][条件字段]
```

**操作类型说明**:

- `find`: 查询操作，返回单个或多个结果
- `exists`: 存在性检查，返回布尔值
- `save`: 保存操作，插入新实体，**可包含创建时的校验逻辑**
- `update`: 更新操作，修改现有实体
- `delete`: 删除操作
- `count`: 计数操作
- `generate`: 生成唯一值操作
- **业务动词**: 如 `move`, `publish` 等，用于封装具有明确业务含义的操作，**方法内部应包含完成该操作所需的所有校验逻辑**。

**具体规范**:

| 方法类别   | 命名结构                  | 返回类型               | 示例                              |
|--------|-----------------------|--------------------|---------------------------------|
| 查询单个实体 | `find[By][条件字段]`      | `Optional<T>`      | `findByUsername`, `findByEmail` |
| 查询字段值  | `find[字段名][By][条件字段]` | `Optional<字段类型>`   | `findUserIdByInviteCode`        |
| 存在性检查  | `exists[By][条件字段]`    | `boolean`          | `existsByUsername`              |
| 删除操作   | `delete[By][条件字段]`    | `Boolean`          | `deleteById`                    |
| 保存操作   | `save`                | `void`             | `save`                          |
| 更新操作   | `update[By][条件字段]`    | `void`             | `updateById`                    |
| 业务操作   | `[业务动词]`              | `void` / `Boolean` | `move`, `publish`               |
| 生成唯一值  | `generate[目标]`        | `String`           | `generateUniqueInviteCode`      |

**方法排列顺序建议**:

1. 查询方法（find / exists / count）
2. 删除方法
3. 写操作（save / update / [业务动词]）
4. 辅助方法（generate / reset）

---

## 3. 方法（Method）命名规范

### 3.1. 通用方法命名规则

- **采用小驼峰命名法（camelCase）**。
- **方法名应为动词或动词短语**，清晰表达方法的行为。

### 3.2. 常见方法命名模式

- **查询方法**: `get`, `find`, `query`, `list`
    - *示例*: `getUserById`, `findActiveUsers`
- **检查方法**: `is`, `has`, `can`, `exists`
    - *示例*: `isActive`, `hasPermission`, `canAccess`
- **转换方法**: `to`, `from`, `convert`
    - *示例*: `toDTO`, `fromEntity`, `convertToJson`
- **业务操作**: 使用具体的业务动词
    - *示例*: `createUser`, `updatePassword`, `deleteResource`

---

## 4. 变量和字段命名规范

### 4.1. ���例变量和局部变量

- **采用小驼峰命名法（camelCase）**。
- **使用有意义的名称**，避免单字母变量（除循环计数器外）。

### 4.2. 常量

- **全部大写，使用下划线分隔**。
- *示例*: `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`

### 4.3. 集合类型变量

- **使用复数形式**或添加描述性后缀。
- *示例*: `users`, `userList`, `userMap`

---

## 5. 枚举（Enum）命名规范

### 5.1. 枚举类命名

- **以 `Enum` 结尾**。
- *示例*: `UserStatusEnum`, `ResourceTypeEnum`

### 5.2. 枚举值命名

- **全部大写，使用下划线分隔**。
- *示例*: `ACTIVE`, `PENDING_VERIFICATION`, `BANNED`

---

## 6. 接口命名规范

### 6.1. 通用接口

- **使用形容词或能力描述**，通常以 `-able` 结尾。
- *示例*: `Serializable`, `Comparable`

### 6.2. 服务接口

- **直接使用服务名称**，不添加 `I` 前缀。
- *示例*: `UserService`, `CacheService`

---

## 7. 注解使用规范

### 7.1. Spring注解

- **服务层**: 使用 `@Service`
- **仓储层**: 使用 `@Repository`
- **控制器**: 使用 `@Controller` 或 `@RestController`
- **配置类**: 使用 `@Configuration`

### 7.2. 业务注解

- **事务处理**: 优先使用 `@Transactional`
- **缓存操作**: 使用 `@Cacheable`, `@CacheEvict`
- **异步操作**: 使用 `@Async`

---

## 8. 日志使用规范

### 8.1. 日志级别使用

- **ERROR**: 系统错误，需要立即关注
- **WARN**: 警告信息，可能的问题
- **INFO**: 重要的业务流程信息
- **DEBUG**: 调试信息，详细的执行流程

### 8.2. 日志内容规范

- **包含关键业务参数**。
- **使用占位符而非字符串拼接**。
- *示例*: `log.info("用户 {} 创建了资源 {}", userId, resourceId)`

---

## 9. 编码风格规范

### 9.1. 缩进和格式

- **使用4个空格缩进**，不使用Tab。
- **大括号使用K&R风格**（开括号不换行）。
- **每行最大长度120字符**。

### 9.2. 导入语句

- **按包名分组排序**。
- **避免使用通配符导入**（`import java.util.*`）。

### 9.3. 注释规范

- **类和公共方法必须有JavaDoc注释**。
- **复杂业务逻辑添加行内注释**。
- **避免无意义的注释**。

---

## 10. 示例代码

### 10.1. Repository接口示例

```java
/**
 * 用户账户仓储接口
 * 负责与用户账户相关的持久化操作
 *
 * @author LinSanQi
 */
public interface UserAccountRepository {
    
    // ====== 查询操作 ======
    Optional<Long> findUserIdByInviteCode(String inviteCode);
    Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);
    Optional<UserAccountEntity> findByEmail(String email, List<Getter<UserAccountEntity>> getters);
    UserAccountEntity findById(Long userId, List<Getter<UserAccountEntity>> getters);
    
    // ====== 存在性检查 ======
    boolean existsByUsername(String username);
    
    // ====== 删除操作 ======
    Boolean deleteById(Long userId);
    
    // ====== 写操作 ======
    void save(UserAccountEntity user);
    void updateById(Consumer<UserAccountEntity> updater);
    
    // ====== 辅助操作 ======
    String generateUniqueInviteCode();
}
```

### 10.2. 服务实现示例

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAppServiceImpl implements UserAppService {
    
    private final UserAccountRepository userAccountRepository;
    
    @Override
    @Transactional
    public void createUser(CreateUserCommand command) {
        log.info("开始创建用户: {}", command.getUsername());
        
        // 检查用户名是否已存在
        if (userAccountRepository.existsByUsername(command.getUsername())) {
            throw new ApplicationException(UserErrorCodeEnum.USERNAME_ALREADY_EXISTS);
        }
        
        // 创建用户实体
        UserAccountEntity user = UserAccountEntity.builder()
                .uaUsername(command.getUsername())
                .uaEmail(command.getEmail())
                .build();
                
        userAccountRepository.save(user);
        log.info("用户创建成功: userId={}", user.getUaId());
    }
}
```

---

## 11. 总结

本规范旨在提供清晰、一致的编码标准，确保代码的可读性和可维护性。所有开发人员应严格遵循这些规范，特别注意：

1. **命名的一致性和语义化**
2. **Repository接口的标准化命名**
3. **分层架构的职责清晰**
4. **注释和文档的完整性**
5. **日志记录的规范性**

遵循这些规范将有助于提高团队协作效率，降低维护成本，提升代码质量。

---

## 12. 数据库设计规范

### 12.1. 表命名

- **全部小写**，单词之间使用下划线 `_` 分隔。
- **使用复数形式**，表示多条记录的集合。
- *示例*: `user_accounts`, `learning_spaces`, `resource_chunks`

### 12.2. 字段命名

- **全部小写**，单词之间使用下划线 `_` 分隔。
- **必须添加表名缩写作为前缀**，以两个字母为准。
    - *示例*: `user_accounts` 表的字段前缀为 `ua_`。`ua_id`, `ua_username`
    - *示例*: `learning_spaces` 表的字段前缀为 `ls_`。`ls_id`, `ls_name`
- **与实体类映射**: 表字段的下划线命名法会自动映射到实体类的驼峰命名法。
    - *示例*: `ua_username` 映射到 `uaUsername`。

### 12.3. 数据类型

- **时间类型**: 使用 `TIMESTAMP` 或 `DATETIME`，**不使用带时区**的类型。
- **枚举类型**: 在数据库中使用**数字类型**（如 `INTEGER`, `SMALLINT`）存储枚举的 `code` 值，而不是字符串。

### 12.4. 主键

- **主键命名**: `表名缩写_id`。
- **类型**: 统一使用 `BIGINT`，对应Java中的 `Long` 类型，用于存储雪花算法生成的ID。

### 12.5. SQL示例

```sql
CREATE TABLE user_accounts (
    ua_id BIGINT PRIMARY KEY,
    ua_username VARCHAR(16) NOT NULL UNIQUE,
    ua_email VARCHAR(255) NOT NULL UNIQUE,
    ua_password VARCHAR(255) NOT NULL,
    ua_status INTEGER NOT NULL DEFAULT 0, -- (0:PENDING, 1:ACTIVE, 2:BANNED)
    ua_invite_code VARCHAR(10) NOT NULL UNIQUE,
    ua_inviter_id BIGINT,
    ua_created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ua_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ua_deleted_at TIMESTAMP
);
```
