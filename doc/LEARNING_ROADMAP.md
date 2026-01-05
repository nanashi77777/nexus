# NEXUS 项目零基础接手学习路线

这份文档专为**没有 Java 基础**但希望接手 NEXUS 项目的开发者设计。路线图从零开始，循序渐进，涵盖了从 Java 语言基础到本项目核心架构的所有必要知识点。

## 第一阶段：Java 语言基础 (预计 2-3 周)

NEXUS 项目使用的是 **Java 21**，因此你需要掌握现代 Java 的特性。

### 1. Java 核心语法
- **环境搭建**: 安装 JDK 21 和 IntelliJ IDEA (推荐)。
- **基础语法**: 变量、数据类型、运算符、控制流 (if/else, loops)。
- **面向对象编程 (OOP)**: 类与对象、继承、封装、多态、接口 (Interface) 与抽象类。
- **异常处理**: try-catch-finally, 自定义异常。

### 2. Java 进阶特性
- **集合框架 (Collections)**: List (ArrayList), Map (HashMap), Set。
- **泛型 (Generics)**: 理解 `<T>` 的用法。
- **Java 8+ 新特性 (重点)**:
    - **Lambda 表达式**: 简化代码。
    - **Stream API**: 处理集合数据的神器 (filter, map, collect)。
    - **Optional**: 优雅地处理空指针。
- **Lombok**: 了解 `@Data`, `@RequiredArgsConstructor`, `@Builder` 等注解，项目中大量使用。

**推荐资源**:
- [廖雪峰 Java 教程](https://www.liaoxuefeng.com/wiki/1252599548343744)
- [Java 21 新特性概览](https://openjdk.org/projects/jdk/21/)

---

## 第二阶段：Web 开发与 Spring Boot (预计 2-3 周)

本项目基于 **Spring Boot 3.5.0**，这是 Java Web 开发的事实标准。

### 1. Web 基础
- **HTTP 协议**: 理解 GET, POST, PUT, DELETE 方法及状态码 (200, 400, 404, 500)。
- **RESTful API**: 理解资源、URI 设计风格。
- **JSON**: 数据交换格式。

### 2. Spring Boot 核心
- **依赖注入 (IoC/DI)**: 理解 `@Component`, `@Service`, `@Autowired`, `@RequiredArgsConstructor`。
- **REST Controller**: 使用 `@RestController`, `@GetMapping`, `@PostMapping`, `@RequestBody` 编写接口。
- **配置管理**: 理解 `application.yml` 和 `@Value`, `@ConfigurationProperties`。
- **全局异常处理**: `@ControllerAdvice`, `@ExceptionHandler` (参考项目中的 `GlobalExceptionHandler`)。

**推荐资源**:
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Boot 教程 (纯洁的微笑)](http://www.ityouknow.com/spring-boot.html)

---

## 第三阶段：数据持久化与数据库 (预计 1-2 周)

本项目使用 **PostgreSQL** 数据库和 **xbatis** (基于 MyBatis) 框架。

### 1. 数据库基础
- **SQL**: 基本的 CRUD 操作 (SELECT, INSERT, UPDATE, DELETE)。
- **PostgreSQL**: 安装与基本使用，理解 JSONB 类型 (项目中可能用到)。

### 2. 持久层框架 (xbatis/MyBatis)
- **ORM 概念**: 对象关系映射。
- **MyBatis**: Mapper 接口与 XML 映射文件 (或注解)。
- **xbatis**: 本项目使用的增强框架，重点学习其动态 SQL 和 Lambda 构造器用法。

**参考项目代码**:
- `src/main/resources/sql/`: 查看建表语句。
- `infrastructure/adapter/resource/ResourceRepositoryImpl.java`: 查看数据库操作实现。

---

## 第四阶段：项目核心技术栈 (预计 2 周)

掌握 NEXUS 项目特有的技术组件。

### 1. 架构模式：六边形架构 (Hexagonal Architecture)
- **核心思想**: 业务逻辑 (Application) 与外部依赖 (Infrastructure) 解耦。
- **分层职责**:
    - `web`: 接口层。
    - `application`: 纯业务逻辑，定义接口 (Port)。
    - `infrastructure`: 技术实现，实现接口 (Adapter)。
- **学习任务**: 阅读 `doc/NEXUS项目架构规范.md`。

### 2. 关键组件
- **Sa-Token**: 权限认证框架。学习如何获取当前登录用户 (`StpUtil.getLoginIdAsLong()`)。
- **LangChain4j**: AI 大模型集成框架。学习如何调用 LLM (OpenAI, DashScope) 和处理 Embedding。
- **Redis & Redisson**: 缓存与分布式锁。
- **Hutool**: Java 工具包，项目中大量使用其工具类 (如 `FileUtil`, `StrUtil`)。

---

## 第五阶段：实战演练 (持续进行)

开始阅读和修改代码。

### 1. 代码阅读路径
建议按照一个 HTTP 请求的流向来阅读代码：
1. **Controller**: `web/rest/v1/resource/ResourceController.java` (入口)
2. **Service**: `application/resource/service/ResourceAppService.java` (业务逻辑)
3. **Port**: `application/resource/port/ResourceRepository.java` (接口定义)
4. **Adapter**: `infrastructure/adapter/resource/ResourceRepositoryImpl.java` (数据落地)

### 2. 练手任务
1. **环境跑通**: 能够本地启动 `NexusServerApplication`，连接本地 PostgreSQL 和 Redis。
2. **Hello World**: 尝试在 `web` 层添加一个新的 Controller，返回 "Hello Nexus"。
3. **新增字段**: 尝试给某个实体类 (如 `Resource`) 增加一个字段，并更新数据库表、DTO 和相关逻辑。
4. **编写测试**: 为某个 Service 方法编写 JUnit 测试用例。

## 总结

接手这个项目不仅是学习 Java，更是学习现代企业级应用的架构设计。不要被大量的代码吓倒，**先跑通，再理解，最后修改**。遇到不懂的类或注解，善用 IDE 的 "Go to Declaration" (Ctrl/Cmd + Click) 和 AI 助手进行查询。
