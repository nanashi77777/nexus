# Repository 接口规范

- **详细规范文档**: [NEXUS项目通用编码与命名规范 - Repository接口命名规范](mdc:doc/NEXUS项目通用编码与命名规范.md)
- **实现框架**: 使用 `xbatis`，一个基于 Mybatis 的 ORM 框架。

## 命名规范

- **接口名**: `实体名(单数) + Repository`。例如: `UserAccountRepository`。
- **方法名**: 遵循 `[操作类型][目标字段][By][条件字段]` 结构。
    - **查询**: `find...`, `exists...`, `count...`
    - **写入**: `save`, `update...`, `delete...`
    - **辅助**: `generate...`, `move...`
- **返回值**:
    - 查询单个结果: `Optional<T>`
    - 查询多个结果: `List<T>`
    - 存在性检查: `boolean`
    - 删除/更新: `void` 或 `Boolean`
    - 数量: `int`

## 方法顺序

1. 查询方法 (`find`, `exists`, `count`)
2. 删除方法 (`delete`)
3. 写操作 (`save`, `update`)
4. 辅助方法 (`generate`)

## 示例

```java
public interface UserAccountRepository {

    // ====== 查询操作 ======

    Optional<Long> findUserIdByInviteCode(String inviteCode);

    Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters);

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

## 工具类使用

- 对于复杂的查询构建，可以使用 `RepositoryUtils`。

```java
// 在 RepositoryImpl 中使用
public Optional<UserAccountEntity> findByUsername(String username, List<Getter<UserAccountEntity>> getters) {
    return RepositoryUtils.getQueryChainWithFields(mapper, getters)
            .where(UserAccountEntity::getUaUsername).eq(username)
            .one();
}
```

description:
globs:
alwaysApply: false
---
