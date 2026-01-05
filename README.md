# Nexus Server

一个基于Spring Boot 3的现代化企业级应用服务器，提供完整的微服务基础设施支持。

## 🚀 项目简介

Nexus Server是一个企业级的Java应用服务器，采用Spring Boot 3框架构建，提供丰富的微服务基础设施功能。项目采用模块化设计，支持高并发、高可用的企业级应用场景。

## 🛠️ 技术栈

### 核心框架

- **Spring Boot 3.5.0** - 主框架
- **Java 21** - 编程语言
- **Gradle** - 构建工具

### 安全认证

- **Sa-Token 1.38.0** - 权限认证框架
- **Redis** - 会话存储

### 数据库

- **PostgreSQL** - 主数据库
- **Druid** - 数据库连接池
- **Xbatis** - 持久层框架

### 缓存与分布式

- **Redisson** - Redis客户端，支持分布式锁、布隆过滤器等
- **Bloom Filter** - 布隆过滤器实现

### AI与文档处理

- **LangChain4j** - AI应用开发框架
- **Apache Tika** - 文档解析
- **JTokkit** - 大模型Token计算

### 基础设施

- **Spring Mail** - 邮件服务
- **Spring Validation** - 参数校验
- **SpringDoc OpenAPI** - API文档

### 工具库

- **Hutool** - 工具类库
- **Lombok** - 代码简化

## 📁 项目结构

```
nexus_v1/
├── src/main/java/cn/lin037/nexus/
│   ├── common/                    # 通用模块
│   │   ├── constant/             # 常量定义
│   │   ├── exception/            # 异常处理
│   │   ├── handler/              # 类型处理器
│   │   ├── model/                # 通用模型
│   │   └── util/                 # 工具类
│   ├── config/                   # 配置类
│   ├── infrastructure/           # 基础设施模块
│   │   ├── common/              # 通用基础设施
│   │   │   ├── cache/           # 缓存服务
│   │   │   ├── file/            # 文件存储
│   │   │   ├── id/              # ID生成器
│   │   │   ├── notification/    # 通知服务
│   │   │   └── task/            # 异步任务
│   │   └── adapter/             # 适配器
│   └── NexusServerApplication.java
├── src/main/resources/
│   ├── application.yml           # 主配置文件
│   ├── application-*.yml         # 模块配置文件
│   ├── sql/                     # SQL脚本
│   └── templates/               # 模板文件
└── src/test/                    # 测试代码
```

## 🚀 快速开始

### 环境要求

- JDK 21+
- PostgreSQL 12+
- Redis 6+
- Gradle 8+

### 1. 克隆项目

```bash
git clone <repository-url>
cd nexus_v1
```

### 2. 配置数据库

```bash
# 创建数据库
createdb nexus_v1

# 执行初始化脚本
psql -d nexus_v1 -f schema.sql
```

### 3. 配置应用

编辑 `src/main/resources/application.yml`，配置数据库和Redis连接信息：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/nexus_v1
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

### 4. 启动应用

```bash
# 使用Gradle
./gradlew bootRun

# 或构建后运行
./gradlew build
java -jar build/libs/nexus_v1-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## 📋 核心功能

### 🔐 认证授权

- 基于Sa-Token的权限认证
- 支持多种认证方式
- 分布式会话管理

### 💾 数据存储

- PostgreSQL主数据库
- Redis缓存支持
- 分布式锁实现

### 📁 文件管理

- 本地文件存储
- 文件元数据管理
- 访问权限控制

### 🤖 AI集成

- LangChain4j集成
- 文档解析处理
- Token计算支持

### 📧 通知服务

- 邮件通知
- 模板引擎
- 异步发送

### ⚡ 异步任务

- 任务队列管理
- 状态跟踪
- 错误处理

## 🧪 测试

```bash
# 运行所有测试
./gradlew test

# 运行特定测试
./gradlew test --tests CacheServiceTest
```

## 📦 构建部署

```bash
# 构建项目
./gradlew build

# 构建Docker镜像
docker build -t nexus-server .

# 运行Docker容器
docker run -p 8080:8080 nexus-server
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👥 作者

- **LinSanQi** - 初始工作

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和开源社区。

---

**注意**: 这是一个初始版本，包含基础的基础设施模块。更多业务功能模块将在后续版本中逐步添加。 