# NEXUS 项目架构规范

本文档定义了 NEXUS 项目的核心架构、分层职责以及模块间的交互原则，旨在为所有开发者提供一个清晰、统一的架构遵循指南，以保证项目的可维护性、可扩展性和代码质量。

## 1. 核心架构思想：六边形架构 (Hexagonal Architecture)

NEXUS 项目采用**六边形架构**（也称作端口与适配器架构），其核心思想是**将业务逻辑（应用核心）与外部依赖（如数据库、消息队列、第三方API）彻底解耦
**。

- **内部（Inside）**: 包含业务领域模型和应用服务，这是我们系统最核心、最稳定的部分。
- **外部（Outside）**: 包含所有与外部世界的交互，如UI、数据库、缓存、AI模型等。
- **端口（Ports）**: 是内部定义的接口，定义了应用核心需要与外部进行何种交互。
- **适配器（Adapters）**: 是外部对端口的具体实现，它负责将外部技术与应用核心的接口进行对接。

这种架构使得我们可以轻松地替换外部依赖（例如，将数据库从 PostgreSQL 更换为 MySQL），而无需修改任何核心业务代码。

参考代码：

- **端口定义**: `@/src/main/java/cn/lin037/nexus/application/resource/port/ResourceRepository.java`
- **适配器实现**: `@/src/main/java/cn/lin037/nexus/infrastructure/adapter/resource/ResourceRepositoryImpl.java`

## 2. 项目分层结构

根据六边形架构的思想，项目被划分为以下几个主要层级，体现在包结构中：

```
cn.lin037.nexus
├── application  // 应用层 (业务逻辑核心)
├── common       // 公共模块 (横切关注点)
├── config       // 配置层
├── infrastructure // 基础设施层 (外部依赖的实现)
└── web          // 表现层 (API接口)
```

### 2.1. `application` (应用层)

这是业务的核心，包含了所有的业务规则和流程。它**不应该依赖**任何具体的基础设施技术。

- **`application/{domain}/port`**: **端口**定义。
    - **职责**: 定义应用核心需要外部世界提供何种能力。例如，`ResourceRepository` 接口定义了需要一个能够持久化和检索
      `Resource` 实体的方法，但它不关心这是如何实现的。
    - **命名规范**: 接口名以 `Repository`, `Port`, `Checker` 等后缀结尾，明确其职责。
- **`application/{domain}/service`**: **应用服务**。
    - **职责**: 编排业务流程，调用端口（仓储、外部服务等）来完成一个完整的业务用例。这是业务逻辑的主要实现地。
    - **规则**:
        - **禁止**直接依赖 `infrastructure` 层的任何类。
        - **必须**通过 `port` 中定义的接口与外部交互。
        - 服务实现类应放在 `impl` 包下。

### 2.2. `infrastructure` (基础设施层)

这一层是所有外部依赖的具体技术实现。

- **`infrastructure/adapter/{domain}`**: **适配器**实现。
    - **职责**: 实现 `application` 层定义的 `port` 接口。例如，`ResourceRepositoryImpl` 使用 `xbatis` 和 `PostgreSQL` 来实现
      `ResourceRepository` 接口。
    - **规则**: 这是唯一允许出现具体技术框架（如MyBatis, Redis, LangChain4j）代码的地方。
- **`infrastructure/common/{tech}`**: **基础设施通用组件**。
    - **职责**: 封装与特定技术相关的通用逻辑，供多个适配器使用。例如，`infrastructure/common/ai` 封装了所有与AI模型交互的逻辑，
      `infrastructure/common/task` 封装了异步任务框架。

### 2.3. `web` (表现层)

负责处理HTTP请求，并将它们转换为对 `application` 层的服务调用。

- **`web/rest/v1/{domain}`**: **RESTful API 控制器**。
    - **职责**: 定义API端点，接收HTTP请求，对请求参数进行校验，调用 `application` 层的服务，并将服务返回的领域对象或DTO转换为统一的
      `ResultVO` 响应格式。
    - **规则**:
        - 控制器应该保持“薄”，不包含任何业务逻辑。
        - 请求体和响应体应定义在 `req` 和 `vo` 包中。
        - 所有公开的API都应返回 `@/src/main/java/cn/lin037/nexus/common/model/vo/ResultVO.java`。

### 2.4. `common` (公共模块)

存放项目中与业务无关、可被各层共享的通用代码。

- **`common/exception`**: 定义全局异常类，如 `ApplicationException`。
- **`common/model`**: 定义通用的数据结构，如 `ResultVO`, `PageResult`。
- **`common/util`**: 存放纯粹的工具类，如 `CodeGenerator`。

## 3. 依赖关系原则 (The Dependency Rule)

**核心原则：所有依赖关系都必须指向内部。**

- `web` 层可以依赖 `application` 层。
- `infrastructure` 层可以依赖 `application` 层。
- `application` 层**不能**依赖 `web` 或 `infrastructure` 层。
- 所有层都可以依赖 `common` 层。

这个规则保证了业务核心的纯粹性和独立性，是六边形架构的基石。

---

*这份文档旨在提供一个高层次的架构概览。后续的文档将针对命名规范、API设计、异常处理等具体方面进行更详细的规定。* 