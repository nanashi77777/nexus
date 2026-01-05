# 总体架构规则

## 项目分层原则

当你在以下目录工作时，遵循对应的架构原则：

- `application/`: 应用层，包含业务逻辑、用例、端口定义
    - **只能依赖**：同层其他模块、`common`模块、JDK标准库
    - **不能依赖**：`infrastructure`层、`web`层（但可以使用web层的req/vo对象）
    - **异常**：只抛出 `ApplicationException`

- `infrastructure/`: 基础设施层，包含技术实现、适配器
    - **可以依赖**：`application`层接口、第三方库、技术框架
    - **不能依赖**：`web`层
    - **异常**：主要抛出 `InfrastructureException`

- `web/`: 表现层，包含REST控制器、DTO
    - **可以依赖**：`application`层接口
    - **不能依赖**：`infrastructure`层具体实现
    - **异常**：统一由 `GlobalExceptionHandler` 处理

- `common/`: 通用层，包含工具类、常量、公共组件
    - **不能依赖**：其他业务层
    - **只能依赖**：JDK标准库、通用第三方库

## 依赖注入规则

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

# 代码质量规则

## 代码风格

- **缩进**: 使用4个空格，不使用Tab。
- **大括号**: 使用K&R风格（开括号不换行）。
- **行长**: 每行最大长度120字符。
- **导入**: 按包名分组排序，避免使用 `*` 通配符导入。
- **参考**: [NEXUS项目通用编码与命名规范](mdc:doc/NEXUS项目通用编码与命名规范.md)

## 方法和类长度

- **方法长度**: 单个方法不应超过50行，如果超过应该拆分为多个逻辑清晰的私有方法。
- **类职责**: 每个类应该只有一个明确的职责（单一职责原则），如果职责过多应该拆分。
  description:
  globs:
  alwaysApply: false

---
