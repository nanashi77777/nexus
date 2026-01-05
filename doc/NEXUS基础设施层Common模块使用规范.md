# NEXUS基础设施层Common模块使用规范

本文档详细说明了NEXUS项目infrastructure/common模块下各个子模块的功能、使用方法和最佳实践。

---

## 1. 模块总览

`infrastructure/common` 模块提供了项目的核心基础设施服务，包含以下子模块：

- **AI模块**: AI服务的统一接口和模型管理
- **缓存模块**: Redis缓存操作的统一抽象
- **文件存储模块**: 文件上传、下载、权限管理
- **ID生成模块**: 雪花算法ID生成服务
- **通知模块**: 邮件、短信等通知服务
- **任务模块**: 异步任务调度和生命周期管理
- **持久化模块**: 数据库实体和映射器定义
- **异常模块**: 基础设施层异常定义

---

## 2. AI模块（ai）

### 2.1. 模块概述

AI模块提供了与第三方AI服务集成的统一接口，支持多种AI模型和提供商。

### 2.2. 核心接口

```java
// 主要服务接口
AiCoreService aiCoreService;

// 获取语言模型
LanguageModel model = aiCoreService.getLanguageModel("gpt-4-turbo", "chat");

// 获取流式语言模型  
StreamingLanguageModel streamingModel = aiCoreService.getStreamingLanguageModel("gpt-4-turbo", "chat");

// 获取嵌入模型
EmbeddingModel embeddingModel = aiCoreService.getEmbeddingModel("text-embedding-ada-002");
```

### 2.3. 配置规范

```yaml
# application-ai-model.yml
ai:
  providers:
    - name: "openai"
      type: "OPEN_AI"
      status: "ACTIVE"
      config:
        apiKey: "${OPENAI_API_KEY}"
        baseUrl: "https://api.openai.com"
```

### 2.4. 异常处理

- 使用 `AIInfraExceptionEnum` 定义的异常码
- 模型实例化失败会自动将模型状态设为 INACTIVE
- 支持故障隔离和人工干预恢复

### 2.5. 使用示例

```java
@Service
@RequiredArgsConstructor
public class ContentGenerationService {
    private final AiCoreService aiCoreService;
    
    public String generateContent(String prompt) {
        try {
            LanguageModel model = aiCoreService.getLanguageModel("gpt-4-turbo", "content-generation");
            return model.generate(prompt);
        } catch (InfrastructureException e) {
            log.error("AI内容生成失败", e);
            throw e;
        }
    }
}
```

---

## 3. 缓存模块（cache）

### 3.1. 模块概述

提供Redis缓存操作的统一抽象，支持多种数据结构和高级功能。

### 3.2. 核心接口

```java
// 通用缓存服务
@Autowired
private CacheService cacheService;

// 基本操作
cacheService.set("key", value);
cacheService.set("key", value, 30, TimeUnit.MINUTES);
String value = cacheService.get("key");
cacheService.delete("key");

// Hash操作
cacheService.hashSet("user:123", "name", "张三");
String name = cacheService.hashGet("user:123", "name");

// List操作
cacheService.listRightPush("queue", item);
Object item = cacheService.listLeftPop("queue");

// Set操作
cacheService.setAdd("tags", Arrays.asList("java", "spring"));
Set<String> tags = cacheService.setMembers("tags");
```

### 3.3. Key构建工具

```java
// 使用CacheKeyBuilder构建标准化的Redis Key
String userKey = CacheKeyBuilder.buildKey("user", "profile", userId);
// 结果: NEXUS:user:profile:123456

String sessionKey = CacheKeyBuilder.buildKey("session", sessionId);
// 结果: NEXUS:session:abc123
```

### 3.4. 配置规范

```yaml
nexus:
  cache:
    bloomFilter:
      expectedInsertions: 10000
      falseProbability: 0.01
    rateLimiter:
      mode: OVERALL
      rate: 100
      rateInterval: PT1M
```

### 3.5. 异常处理

- 使用 `CacheExceptionCodeEnum` 定义的异常码
- 布隆过滤器和令牌桶初始化异常
- Lua脚本执行失败异常

---

## 4. 文件存储模块（file）

### 4.1. 模块概述

提供完整的文件管理解决方案，包括上传、下载、权限控制和配额管理。

### 4.2. 核心接口

```java
@Autowired
private FileStorageService fileStorageService;

// 文件上传
InfraFileMetadata metadata = fileStorageService.upload(
    multipartFile, 
    ownerIdentifier, 
    AccessLevel.PRIVATE
);

// 文件下载
boolean success = fileStorageService.download(
    fileId, 
    accessorIdentifier, 
    outputStream
);

// 权限管理
fileStorageService.grantAccess(fileId, ownerIdentifier, granteeIdentifier);
fileStorageService.revokeAccess(fileId, ownerIdentifier, granteeIdentifier);

// 文件删除
fileStorageService.delete(fileId, ownerIdentifier);
```

### 4.3. 访问级别

- **PUBLIC**: 公开访问，任何人都可以通过文件ID访问
- **PRIVATE**: 私有访问，只有所有者和被授权用户可以访问

### 4.4. 配置规范

```yaml
nexus:
  file:
    storage:
      basePath: "/data/files"
      maxFileSize: 100MB
      allowedTypes: ["image/*", "application/pdf", "text/*"]
    quota:
      weeklyLimit: 50
      enabled: true
```

### 4.5. 异常处理

使用 `FileExceptionCodeEnum` 定义的异常码，包括：

- 文件未找到、访问被拒绝
- 存储失败、下载失败、删除失败
- 配额超出、路径无效
- 文件所有者不匹配、模板文件冲突

---

## 5. ID生成模块（id）

### 5.1. 模块概述

基于雪花算法的分布式ID生成服务，支持高并发和集群部署。

### 5.2. 核心接口

```java
// 静态方法调用（推荐）
String id = HutoolSnowflakeIdGenerator.generateId();
long longId = HutoolSnowflakeIdGenerator.generateLongId();
```

### 5.3. 配置规范

```yaml
snowflake:
  datacenter-id: 0  # 数据中心ID (0-31)
  worker-id: 0      # 工作机器ID (0-31)
```

### 5.4. 特性

- **时钟回拨处理**: 支持最多2秒的时钟回拨
- **自定义起始时间**: 使用2025-01-05作为起始时间
- **随机序列**: 提高ID的随机性
- **单例模式**: 全局唯一的Snowflake实例

### 5.5. 异常处理

使用 `IdGeneratorResultCodeEnum` 定义的异常码：

- 时钟回拨错误、序列号溢出
- 无效的工作机器ID、数据中心ID
- 雪花算法初始化错误

---

## 6. 通知模块（notification）

### 6.1. 模块概述

统一的通知发送服务，支持邮件、短信等多种通知方式。

### 6.2. 核心接口

```java
@Autowired
private NotificationService emailNotificationService;

// 发送简单文本通知
emailNotificationService.send(
    "user@example.com", 
    "欢迎注册", 
    "欢迎您注册我们的平台！"
);

// 使用模板发送通知
Map<String, Object> variables = Map.of(
    "username", "张三",
    "verificationCode", "123456"
);
emailNotificationService.send(
    "user@example.com",
    "验证码",
    "verification",  // 模板ID
    variables
);
```

### 6.3. 模板系统

- **模板存储**: `/resources/templates/notifications/`
- **支持格式**: `.html`, `.text`, `.md`
- **模板引擎**: 支持变量替换的简单模板引擎
- **缓存机制**: 模板内容自动缓存，支持清理

### 6.4. 频率控制

- **全局限流**: 基于Redisson的令牌桶算法
- **等待队列**: 支持有限队列等待
- **动态配置**: 支持运行时调整限流参数

### 6.5. 配置规范

```yaml
nexus:
  notification:
    email:
      from: "noreply@nexus.com"
    rateLimiter:
      enabled: true
      permitsPerMinute: 60
      maxWaiters: 100
```

### 6.6. 异常处理

使用 `NotificationExceptionCodeEnum` 定义的异常码：

- 发送频率超出限制、等待队列已满
- 模板未找到、模板渲染失败
- 发送失败、配置无效

---

## 7. 任务模块（task）

### 7.1. 模块概述

异步任务调度和生命周期管理系统，支持任务持久化、自动恢复和分布式执行。

### 7.2. 核心接口

```java
// 任务管理器
@Autowired
private AsyncTaskManager asyncTaskManager;

// 提交任务
Map<String, Object> params = Map.of(
    "inputText", "要处理的文本",
    "options", Map.of("format", "pdf")
);
Long taskId = asyncTaskManager.submit(
    "DOCUMENT_GENERATION",  // 任务类型
    params,                 // 任务参数
    userId                  // 任务所有者
);

// 查询任务状态
Optional<AsyncTask> task = asyncTaskManager.getTask(taskId);

// 取消任务
asyncTaskManager.cancel(taskId, operatorId);
```

### 7.3. 任务执行器开发

```java
@Component
public class DocumentGenerationTaskExecutor implements TaskExecutor<DocumentParams, DocumentResult> {
    
    @Override
    public String getTaskType() {
        return "DOCUMENT_GENERATION";
    }
    
    @Override
    public DocumentResult execute(DocumentParams parameters, TaskContext context) throws Exception {
        // 检查取消状态
        if (context.isCancellationRequested()) {
            throw new InfrastructureException(TaskExceptionCodeEnum.TASK_CANCELLED);
        }
        
        // 执行业务逻辑
        return processDocument(parameters);
    }
    
    @Override
    public Class<DocumentParams> getParametersType() {
        return DocumentParams.class;
    }
    
    @Override
    public void onCompletion(DocumentParams parameters, DocumentResult result) {
        // 成功回调
        log.info("文档生成完成: {}", result.getFileUrl());
    }
    
    @Override
    public void onFailure(DocumentParams parameters, Exception exception) {
        // 失败回调
        log.error("文档生成失败", exception);
    }
}
```

### 7.4. 任务状态

- **WAITING**: 等待调度
- **RUNNING**: 正在执行
- **COMPLETED**: 执行完成
- **CANCELLED**: 已取消
- **FAILED**: 执行失败

### 7.5. 配置规范

```yaml
nexus:
  task:
    enabled: true
    polling:
      interval: PT5S
      batchSize: 10
    shortRunning:
      maxPoolSize: 100
    longRunning:
      corePoolSize: 5
      maxPoolSize: 20
      queueCapacity: 100
    mapping:
      long-running:
        - "DOCUMENT_GENERATION"
        - "VIDEO_PROCESSING"
        - "DATA_MIGRATION"
```

### 7.6. 异常处理

使用 `TaskExceptionCodeEnum` 定义的异常码：

- 任务不存在、任务中断、执行器未找到
- 任务已运行、无法取消、序列化失败
- 配置错误、未知状态

---

## 8. 持久化模块（persistent）

### 8.1. 模块概述

定义了项目核心数据实体和映射器接口。

### 8.2. 核心实体

- **UserAccountEntity**: 用户账户基础信息
- **LearningSpaceEntity**: 学习空间，实现多租户隔离
- **ResourceEntity**: 资源元数据
- **ResourceChunkEntity**: 资源分片数据

### 8.3. 映射器接口

所有映射器都继承 `MybatisMapper<T>` 基础接口：

```java
@Mapper
public interface UserAccountMapper extends MybatisMapper<UserAccountEntity> {
    // 继承基础CRUD操作
}
```

### 8.4. 实体特性

- **雪花ID**: 使用 `@TableId(value = IdAutoType.NONE)` 手动设置ID
- **逻辑删除**: 使用 `@LogicDelete` 注解
- **类型处理**: 使用 `@TypeHandler` 处理JSON等复杂类型
- **序列化**: 实现 `Serializable` 接口

---

## 9. 异常模块（exception）

### 9.1. 模块概述

定义基础设施层的异常类型和异常码接口。

### 9.2. 核心组件

- **InfrastructureException**: 基础设施异常基类
- **InfraExceptionCode**: 异常码接口
- **CommonInfraExceptionEnum**: 通用基础设施异常码

### 9.3. 使用规范

```java
// 抛出基础设施异常
throw new InfrastructureException(AIInfraExceptionEnum.MODEL_INSTANTIATION_FAILED);

// 带原因的异常
throw new InfrastructureException(FileExceptionCodeEnum.STORAGE_ERROR, cause);

// 使用静态工厂方法
throw InfrastructureException.of(TaskExceptionCodeEnum.INFRA_TASK_NOT_FOUND, "详细信息");
```

---

## 10. 模块集成最佳实践

### 10.1. 依赖注入规范

```java
@Service
@RequiredArgsConstructor  // 使用构造函数注入
public class BusinessService {
    
    private final CacheService cacheService;
    private final FileStorageService fileStorageService;
    private final AsyncTaskManager taskManager;
    
    // 业务方法
}
```

### 10.2. 异常处理策略

```java
@Service
public class IntegratedService {
    
    public void complexOperation() {
        try {
            // 调用多个基础设施服务
            cacheService.set("key", "value");
            fileStorageService.upload(file, owner, AccessLevel.PRIVATE);
            taskManager.submit("TASK_TYPE", params, owner);
            
        } catch (InfrastructureException e) {
            // 基础设施异常直接重抛
            log.error("基础设施操作失败", e);
            throw e;
        } catch (Exception e) {
            // 未知异常包装为基础设施异常
            log.error("意外异常", e);
            throw new InfrastructureException(
                CommonInfraExceptionEnum.INFRA_COMMON_UNKNOWN_ERROR, e);
        }
    }
}
```

### 10.3. 配置管理

```yaml
# application.yml
nexus:
  cache:
    # 缓存配置
  file:
    # 文件存储配置
  notification:
    # 通知配置
  task:
    # 任务配置

# 外部化敏感配置
ai:
  providers:
    - config:
        apiKey: "${AI_API_KEY:default_key}"
```

### 10.4. 监控和日志

```java
@Slf4j
public class MonitoredService {
    
    @Autowired
    private CacheService cacheService;
    
    public void businessMethod() {
        String cacheKey = CacheKeyBuilder.buildKey("business", "operation", id);
        
        try {
            // 记录操作开始
            log.info("开始执行业务操作: id={}", id);
            
            // 执行操作
            Object result = cacheService.get(cacheKey);
            if (result == null) {
                result = computeResult();
                cacheService.set(cacheKey, result, 30, TimeUnit.MINUTES);
            }
            
            // 记录操作成功
            log.info("业务操作执行成功: id={}", id);
            
        } catch (Exception e) {
            // 记录操作失败
            log.error("业务操作执行失败: id={}", id, e);
            throw e;
        }
    }
}
```

---

## 11. 总结

Infrastructure/Common模块提供了NEXUS项目的核心基础设施能力：

1. **AI服务**: 统一的AI模型访问接口，支持多提供商
2. **缓存服务**: 完整的Redis操作抽象，支持多种数据结构
3. **文件存储**: 安全的文件管理，支持权限控制和配额管理
4. **ID生成**: 高性能的分布式ID生成服务
5. **通知服务**: 统一的通知发送，支持模板和频率控制
6. **任务调度**: 可靠的异步任务管理，支持持久化和恢复
7. **数据持久化**: 标准化的实体定义和映射器接口
8. **异常处理**: 完整的基础设施异常体系

使用这些模块时，请遵循：

- **接口优先**: 通过接口而非实现类进行依赖
- **异常规范**: 使用标准的异常码和处理策略
- **配置外化**: 重要配置参数支持外部化配置
- **日志完整**: 关键操作必须有完整的日志记录
- **资源管理**: 合理使用缓存、连接池等资源 