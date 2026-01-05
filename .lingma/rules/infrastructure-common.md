### 简介

本文档概述了在 `infrastructure/common` 目录中使用共享模块的规则和最佳实践。遵守这些准则可确保一致性、稳定性和适当的资源管理。

### 1. 缓存模块 (`common/cache`)

缓存模块提供缓存、速率限制和概率数据结构的服务。

**核心服务:**

- `[CacheService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/cache/service/CacheService.java)`:
  通用键值缓存操作。
-
`[RateLimiterService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/cache/service/RateLimiterService.java)`:
用于分布式速率限制。
-
`[BloomFilterService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/cache/service/BloomFilterService.java)`:
用于高效检查元素是否存在，允许少量误差。

**使用规则:**

- **强制使用Key构建器**: 所有缓存键 **必须** 使用
  `[CacheKeyBuilder.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/cache/util/CacheKeyBuilder.java)`
  构建。这确保了整个应用中键名的一致性 (`SYSTEM_NAME:module:key:…`)。
- **初始化**: 对于像 `BloomFilterService` 这样的服务，请确保在适配器的初始化阶段 (`@PostConstruct`) 调用 `tryInit`
  来设置过滤器。
- **示例 - 验证码与速率限制**: 参考
  `[UserVerificationAdapter.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/user/UserVerificationAdapter.java)`
  ，了解如何使用 `CacheService` 存储验证码并实现复杂的速率限制逻辑（冷却、窗口计数器、封禁）。
- **示例 - 存在性检查**: 参考
  `[UserPresenceCheckerAdapter.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/user/UserPresenceCheckerAdapter.java)`
  ，了解如何使用 `BloomFilterService` 作为访问数据库前的第一道防线，显著降低数据库负载。

### 2. AI 模块 (`common/ai`)

AI 模块提供对各种AI模型和工具的统一访问。

**核心服务:**

- `[AiCoreService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/ai/service/AiCoreService.java)`:
  获取语言模型 (`LanguageModel`, `StreamingLanguageModel`) 和嵌入模型 (`EmbeddingModel`) 实例的核心服务。它处理模型提供商的选择和配置。
-
`[StructuredOutputTool.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/ai/service/StructuredOutputTool.java)`:
用于引导语言模型以特定的Java类格式 (`.class`) 返回响应。
- `[VectorizationTool.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/ai/service/VectorizationTool.java)`:
  为在向量存储中创建、存储和搜索文本嵌入提供了抽象。

**使用规则:**

- **模型获取**: 始终使用 `AiCoreService` 获取模型实例，不要直接实例化模型。
- **结构化输出**: 当需要AI返回符合特定`POJO`的数据时，使用 `StructuredOutputTool`。提供系统提示、用户提示和所需输出类的示例实例。详情请参阅
  `[ResourceAiGenerateTaskExecutor.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/resource/impl/ResourceAiGenerateTaskExecutor.java)`
  中生成计划和内容的示例。
- **向量化**:
    - 使用 `VectorPort` (由 `VectorizationTool` 实现的应用层端口) 进行所有与嵌入相关的操作。
    - 要向量化并存储文本块，首先从 `AiCoreService` 获取一个 `EmbeddingModel`，然后将其传递给 `VectorPort` 的 `add` 或
      `batchAdd` 方法。
    - 标准实现请参考
      `[ResourceChunkVectorizeTaskExecutor.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/resource/impl/ResourceChunkVectorizeTaskExecutor.java)`。

### 3. 文件模块 (`common/file`)

此模块处理所有文件存储和检索操作。

**核心服务:**

-
`[FileStorageService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/file/service/FileStorageService.java)`:
文件操作的唯一入口，它抽象了底层存储系统（如本地磁盘、S3）。

**使用规则:**

- **适配器模式**: 不要将 `FileStorageService` 直接注入到业务逻辑中。应使用实现了
  `[FilePort.java](mdc:src/main/java/cn/lin037/nexus/application/common/port/FilePort.java)` 的
  `[FileAdapter.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/common/FileAdapter.java)`。
- **所有权与访问权限**: 上传文件时，必须指定 `ownerIdentifier` (如用户ID) 和 `AccessLevel` (`PUBLIC`, `PRIVATE`,
  `SHARED`)。所有后续操作（如下载或删除）都需要提供正确的标识符以进行权限检查。
- **物理路径**: 如需获取存储文件的物理路径以进行处理（如解析），请使用 `getAbsolutePath(storagePath)` 方法。示例请参考
  `[ResourceParseTaskExecutor.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/resource/impl/ResourceParseTaskExecutor.java)`。

### 4. ID 模块 (`common/id`)

提供全局唯一ID生成。

**核心服务:**

-
`[HutoolSnowflakeIdGenerator.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/id/HutoolSnowflakeIdGenerator.java)`:
雪花算法ID生成器。

**使用规则:**

- **静态访问**: 该生成器是在应用启动时初始化的单例。直接使用静态方法 `HutoolSnowflakeIdGenerator.generateId()` (返回
  `String` 类型) 或 `HutoolSnowflakeIdGenerator.generateLongId()` (返回 `long` 类型) 即可，无需依赖注入。

### 5. 通知模块 (`common/notification`)

管理发送通知（如电子邮件）。

**核心服务:**

-
`[NotificationService.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/notification/service/NotificationService.java)`:
发送通知的接口。

**使用规则:**

- **类型安全的注入**: 系统可以有多个 `NotificationService` 的实现（例如，用于电子邮件、短信）。要使用特定服务，请注入
  `List<NotificationService>` 并按所需的 `NotificationType` 进行过滤。
- **示例**: 参考
  `[UserNotificationAdapter.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/user/UserNotificationAdapter.java)`
  。其构造函数从所有可用的通知服务列表中查找 `EMAIL` 服务。
- **模板**: 优先使用基于模板的发送方法 (`send(to, subject, templateId, variables)`) 以确保一致性和可维护性。

### 6. 任务模块 (`common/task`)

任务模块为管理和执行后台作业提供了一个健壮的框架。

**使用规则:**

**A. 创建任务执行器:**

1. **定义参数**: 为任务参数创建一个新的POJO类。该类必须可序列化为JSON。例如: `ResourceParseTaskParameters.java`。
2. **实现 `TaskExecutor`**: 创建一个实现
   `[TaskExecutor<Parameters, Result>](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/task/executor/TaskExecutor.java)`
   的类。
3. **实现方法**:
    - `getTaskType()`: 为此任务类型返回一个唯一的 `String` 常量。例如: `ResourceTaskConstant.TASK_TYPE_RESOURCE_PARSE`。
    - `getParametersType()`: 返回参数POJO的 `.class`。
    - `execute(params, context)`: 在此实现核心任务逻辑。
        - 使用 `[TaskContext](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/task/executor/TaskContext.java)`
          检查取消请求 (`context.isCancellationRequested()`)。
    - `onCompletion(params, result)` (可选): 成功执行后运行的逻辑。
    - `onFailure(params, exception)` (可选): 失败时运行的逻辑。

**B. 处理事务:**

- `execute` 方法本身 **不是** 事务性的。
- 对于执行器内的数据库操作，请使用 **编程式事务** (`TransactionTemplate`) 或注入一个Spring服务/仓库并调用其
  `@Transactional` 方法。这确保了状态变更（例如，将资源设置为 `PARSING`）即使在主任务逻辑稍后失败时也能立即提交。
-
`[ResourceParseTaskExecutor.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/adapter/resource/impl/ResourceParseTaskExecutor.java)`
中有清晰的示例，展示了如何使用 `TransactionTemplate` 独立于主处理逻辑来管理状态更新。

**C. 配置长时间运行的任务:**

- 默认情况下，任务被视为“短时运行”，并受信号量限制。
- 要将任务指定为“长时间运行”（例如，用于I/O密集型或耗时的AI生成），它将使用一个独立的、限制较少的线程池。你必须在
  `application.yml` 中进行配置。
- 将任务的唯一类型字符串添加到 `nexus.task.mapping` 下的 `long-running` 列表中。
- 要了解正确的配置路径，请查看
  `[TaskScheduler.java](mdc:src/main/java/cn/lin037/nexus/infrastructure/common/task/scheduler/TaskScheduler.java)` 中的
  `isLongRunning` 方法。

**`application.yml` 配置示例:**

```yaml
nexus:
  task:
    mapping:
      long-running:
        - "resource.ai.generate" # 来自 ResourceAiGenerateTaskExecutor
        - "resource.parse"       # 来自 ResourceParseTaskExecutor
```
