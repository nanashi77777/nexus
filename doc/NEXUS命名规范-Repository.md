## 📌 一、接口命名规范

- **类名命名**：

    - 接口名称应为 `实体名(不带复数) + Repository`
      ，如 [UserAccountRepository](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L15-L94)。
    - 表示对某一实体的持久化操作管理。

- **接口命名示例**：
  ```java
  public interface UserAccountRepository
  ```

---

## 📌 二、方法命名规范

### 🧩 基本命名结构

```
[操作类型][目标字段][By][条件字段]
```

- **操作类型**：如
  `find`, [exists](file://cn\xbatis\core\sql\executor\chain\QueryChain.java#L40-L40), [save](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L87-L87), [update](file://cn\xbatis\db\annotations\TableField.java#L11-L11), [delete](file://org\redisson\api\RObject.java#L25-L25), [count](file://cn\xbatis\core\sql\executor\chain\QueryChain.java#L39-L39)
  等。也可以是明确的**业务动词**，如 `move`。
- **目标字段**：表示操作的目标字段（可省略，若操作对象是整个实体）。
- **By**：用于分隔查询条件。
- **条件字段**：表示查询或操作的依据字段。

---

### ✅ 1. 查询方法（find）

- **命名建议**：`find[By][条件字段]`
- **返回值类型**：
    - 单个结果 → `Optional<T>`
    - 多个结果 → `List<T>`
- **说明**：
    - 如果操作对象是整个实体，不需标注实体名。
    - 如果是字段级查询，应显式标注字段名。

#### ✅ 示例：

```java
Optional<Long> findUserIdByInviteCode(String inviteCode);

Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);

Optional<UserAccountEntity> findByEmail(String email, List<Getter<UserAccountEntity>> getters);

Optional<UserAccountEntity> findByAccount(String account, List<Getter<UserAccountEntity>> getters);

UserAccountEntity findById(Long userId, List<Getter<UserAccountEntity>> getters);
```

---

### ✅ 2. 删除方法（delete）

- **命名建议**：`delete[By][条件字段]`
- **返回值类型**：
    - 成功与否 → `Boolean`
    - 删除数量 → `Integer`
- **说明**：
    - 推荐使用 `deleteById`，表示从持久化存储中删除。

#### ✅ 示例：

```java
Boolean deleteById(Long userId);
```

---

### ✅ 3. 保存方法（save）

- **命名建议**：`save`
- **返回值类型**：
    - 无返回值（适用于插入新实体）
- **说明**：
    - 若是插入新实体，不需加 `insert`。**方法内部可包含创建时的校验逻辑**。
    - 若是插入或更新混合操作，建议命名为 `saveOrUpdate`。

#### ✅ 示例：

```java
void save(UserAccountEntity user);
```

---

### ✅ 4. 更新方法（update）

- **命名建议**：`update[By][条件字段]`
- **返回值类型**：
    - 成功与否 → `Boolean`
- **说明**：
    - 使用 `updateById` 表示根据 ID 更新。
    - 避免使用 `modify`，语义不明确。

#### ✅ 示例：

```java
void updateById(Consumer<UserAccountEntity> updater);
```

---

### ✅ 5. 存在性检查（exists）

- **命名建议**：`exists[By][条件字段]`
- **返回值类型**：`boolean`
- **说明**：
    - 用于布隆过滤器、缓存等场景，判断是否存在。

#### ✅ 示例：

```java
boolean existsByUsername(String username);
```

---

### ✅ 6. 业务行为方法

- **命名建议**：使用能描述业务的动词，如 `move`, `publish`
- **返回值类型**：`void` 或 `Boolean`
- **说明**：
    - 用于封装超越简单CRUD的、具有明确业务含义的操作。
    - 方法内部应包含完成该操作所需的所有校验逻辑。

#### ✅ 示例：

```java
void move(Long folderId, Long newParentId, Long userId);
```

---

## 📌 三、接口结构顺序建议

为提升可读性，建议接口中方法按以下顺序组织：

1. **查询方法**（find / exists / count）
2. **存在性检查**
3. **删除方法**
4. **写操作**（save / update / 业务行为方法）
5. **辅助方法**（generate / reset）

---

## ✅ 四、最终命名规范总结表

| 方法类别 | 命名结构           | 示例                                                                                                                                             | 返回类型             | 说明          |
|------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------|------------------|-------------|
| 查询   | find[By][字段]   | [findByUsername](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L32-L32)           | `Optional<T>`    | 查询单个实体      |
| 查询   | find[By][字段]   | [findUserIdByInviteCode](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L24-L24)   | `Optional<Long>` | 查询字段        |
| 存在性  | exists[By][字段] | [existsByUsername](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserBloomFilterPort.java#L15-L15)           | `boolean`        | 布隆过滤器等      |
| 删除   | delete[By][字段] | [deleteById](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L73-L73)               | `Boolean`        | 删除操作        |
| 保存   | save           | [save](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L87-L87)                     | `void`           | 插入新实体，可包含校验 |
| 更新   | update[By][字段] | [updateById](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L93-L93)               | `void`           | 更新操作        |
| 业务行为 | `[业务动词]`       | `move(Long folderId, ...)`                                                                                                                     | `void`           | 封装业务，包含校验   |
| 辅助   | generate[目标]   | [generateUniqueInviteCode](file://E:\JavaWork\nexus_v1\src\main\java\cn\lin037\nexus\application\user\port\UserAccountRepository.java#L81-L81) | `String`         | 生成唯一值       |

---

## ✅ 五、修改后的接口结构示例

```java
package cn.lin037.nexus.application.user.port;

import cn.lin037.nexus.infrastructure.common.persistent.entity.UserAccountEntity;
import db.sql.api.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 用户仓储接口
 * 负责与用户账户相关的持久化操作
 *
 * @author LinSanQi
 */
public interface UserAccountRepository {

    // ====== 查询操作 ======

    Optional<Long> findUserIdByInviteCode(String inviteCode);

    Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);

    Optional<UserAccountEntity> findByEmail(String email, List<Getter<UserAccountEntity>> getters);

    Optional<UserAccountEntity> findByAccount(String account, List<Getter<UserAccountEntity>> getters);

    UserAccountEntity findById(Long userId, List<Getter<UserAccountEntity>> getters);

    // ====== 删除操作 ======

    Boolean deleteById(Long userId);

    // ====== 写操作 ======

    void save(UserAccountEntity user);

    void updateById(Consumer<UserAccountEntity> updater);

    // ====== 业务行为操作 ======

    void move(Long folderId, Long newParentId, Long userId);

    // ====== 辅助操作 ======

    String generateUniqueInviteCode();
}
```

---

### ✅ 总结

该命名规范强调：

- **语义清晰**：方法名清晰表达操作意图
- **统一风格**：所有方法命名风格统一，便于维护
- **职责分明**：不同操作分类明确，提升可读性
- **可扩展性强**：便于未来添加新方法
