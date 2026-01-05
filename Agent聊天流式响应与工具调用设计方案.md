# Agent聊天流式响应与工具调用设计方案

## 1. 需求分析与注意点

### 1.1 核心需求梳理

根据用户需求，需要解决以下核心问题：

1. **流式响应取消机制**：在StreamingChatListener的各个回调中，通过AgentCachePort检查会话是否被取消
2. **工具信息存储设计**：设计合适的数据结构存储工具请求和执行结果
3. **授权机制处理**：当会话未开启自动执行时，需要保存数据并结束响应
4. **工具请求与结果对应**：确保工具请求和执行结果能够一一对应
5. **消息实体设计**：通过序列化JSON到AgentChatMessageEntity.acmContent实现数据持久化

### 1.2 当前架构分析

**现有组件关系：**

- `AgentChatAppServiceImpl.streamChat()` → 调用 `agentChatPort.chatStream()`
- `agentChatPort.chatStream()` → 内部调用 `StreamingChatToolServiceImpl.chat()`
- `StreamingChatToolServiceImpl` → 通过回调触发 `StreamingChatListener` 的各个方法

**StreamingChatListener回调方法：**

- `onThinkingToken()` - 思考token片段
- `onThinkingComplete()` - 思考完成
- `onContentToken()` - 内容token片段
- `onCompleteMessage()` - 完整消息
- `onReceiveCompleteToolList()` - 接收完整工具列表
- `onToolExecutionStart()` - 单个工具执行开始
- `onToolExecutionFinish()` - 单个工具执行完成
- `onAllToolsComplete()` - 所有工具执行完成
- `onStreamingComplete()` - 流式响应完成
- `onError()` - 错误处理

## 2. 关键疑问点分析

### 2.1 取消检查时机问题

**疑问：** 在哪些回调方法中检查取消状态？

**分析：**

- **高频检查点**：`onContentToken()` - 每个内容片段都检查，响应最及时
- **关键决策点**：`onCompleteMessage()`, `onReceiveCompleteToolList()`, `onToolExecutionStart()` - 在重要决策前检查
- **避免检查点**：`onError()`, `onStreamingComplete()` - 已经是结束流程，无需检查

**建议策略：** 在内容生成和工具执行的关键节点进行检查

### 2.2 AiMessage保存时机与内容

**疑问：** 何时保存AiMessage？保存什么内容？

**当前问题：**

- `onCompleteMessage()` 可能返回工具响应，与工具相关存储冲突
- 需要区分普通AI响应和工具调用响应

**解决思路：**

1. **普通AI响应**：在 `onCompleteMessage()` 中，当 `finishReason != TOOL_EXECUTION` 时保存
2. **工具相关响应**：通过专门的工具消息对象处理，不在 `onCompleteMessage()` 中保存

### 2.3 工具信息存储设计

**核心挑战：**

1. **一对多关系**：一次AI响应可能包含多个工具请求
2. **状态管理**：工具请求的执行状态（待执行/执行中/已完成/已跳过/已拒绝）
3. **结果关联**：工具请求与执行结果的一一对应
4. **授权流程**：需要人工授权时的数据保存和恢复

## 3. 数据结构设计方案

### 3.1 工具批次消息对象

```java
/**
 * 工具批次消息 - 存储一次AI响应中的所有工具请求
 */
public class ToolBatchMessage {
    private String batchId;                    // 批次ID
    private Long sessionId;                   // 会话ID
    private String aiMessageContent;          // 原始AI消息内容
    private TokenUsage tokenUsage;            // Token使用情况
    private List<ToolRequestItem> toolRequests; // 工具请求列表
    private ToolBatchStatus status;           // 批次状态
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3.2 工具请求项对象

```java
/**
 * 单个工具请求项
 */
public class ToolRequestItem {
    private String requestId;                 // 请求ID（对应ToolExecutionRequest.id()）
    private String toolName;                  // 工具名称
    private String arguments;                 // 工具参数（JSON字符串）
    private ToolRequestStatus status;         // 请求状态
    private String executionResult;           // 执行结果（JSON字符串）
    private String errorMessage;              // 错误信息
    private Long executionTimeMs;             // 执行耗时
    private LocalDateTime requestedAt;        // 请求时间
    private LocalDateTime executedAt;         // 执行时间
}
```

### 3.3 状态枚举定义

```java
/**
 * 工具批次状态
 */
public enum ToolBatchStatus {
    PENDING,           // 待处理
    PROCESSING,        // 处理中
    WAITING_AUTH,      // 等待授权
    COMPLETED,         // 已完成
    CANCELLED,         // 已取消
    FAILED            // 失败
}

/**
 * 工具请求状态
 */
public enum ToolRequestStatus {
    PENDING,          // 待执行
    EXECUTING,        // 执行中
    COMPLETED,        // 已完成
    SKIPPED,          // 已跳过
    REJECTED,         // 已拒绝
    FAILED           // 执行失败
}
```

## 4. 流程设计方案

### 4.1 正常流程（自动执行）

```
1. AgentChatAppServiceImpl.streamChat()
   ↓
2. agentChatPort.chatStream() → StreamingChatToolServiceImpl.chat()
   ↓
3. StreamingChatListener回调序列：
   
   3.1 onContentToken() 
       → 检查取消状态 → 如果取消则保存当前内容并complete()
   
   3.2 onCompleteMessage()
       → 如果finishReason != TOOL_EXECUTION，保存普通AI消息
   
   3.3 onReceiveCompleteToolList()
       → 创建ToolBatchMessage对象
       → 检查取消状态
       → 检查自动执行权限
       → 如果需要授权：保存批次消息(状态=WAITING_AUTH) → 返回DEFER_AND_CLOSE
       → 如果自动执行：返回PROCEED
   
   3.4 onToolExecutionStart() (循环每个工具)
       → 更新ToolRequestItem状态为EXECUTING
       → 检查取消状态
       → 返回执行决策
   
   3.5 onToolExecutionFinish() (循环每个工具)
       → 更新ToolRequestItem状态和结果
       → 检查取消状态
   
   3.6 onAllToolsComplete()
       → 更新ToolBatchMessage状态为COMPLETED
       → 保存到AgentChatMessageEntity
   
   3.7 onStreamingComplete()
       → 清理会话状态
```

### 4.2 授权流程（需要人工确认）

```
1. 在onReceiveCompleteToolList()中检测到需要授权
   ↓
2. 创建ToolBatchMessage，状态设为WAITING_AUTH
   ↓
3. 序列化为JSON保存到AgentChatMessageEntity.acmContent
   ↓
4. 调用sink.complete()结束当前响应
   ↓
5. 前端显示工具授权界面
   ↓
6. 用户确认后，通过新的API继续执行：
   - 从数据库加载ToolBatchMessage
   - 根据用户选择更新各个ToolRequestItem状态
   - 继续执行被授权的工具
   - 构造新的消息列表继续对话
```

### 4.3 取消流程

```
1. 用户调用cancelStreaming()
   ↓
2. AgentCachePort.setSessionCancelled()设置取消标志
   ↓
3. 在各个回调中检查到取消状态：
   - 保存当前已生成的内容
   - 如果有未完成的工具批次，更新状态为CANCELLED
   - 调用sink.complete()结束响应
```

## 5. 实现细节设计

### 5.1 消息类型扩展

需要在AgentChatMessageTypeEnum中添加新类型：

```java
public enum AgentChatMessageTypeEnum {
    NORMAL(0, "普通消息"),
    TOOL_BATCH(3, "工具批次消息"),    // 新增
    // ... 其他类型
}
```

### 5.2 JSON序列化策略

- 使用Jackson进行ToolBatchMessage的序列化/反序列化
- 在AgentChatMessageEntity.acmContent中存储JSON字符串
- 提供工具类进行对象与JSON的转换

### 5.3 并发安全考虑

- 使用AtomicReference保存当前的ToolBatchMessage
- 在更新工具状态时使用乐观锁机制
- 取消检查使用volatile变量或缓存

## 6. 技术实现细节

### 6.1 StreamingChatListener实现策略

在AgentChatAppServiceImpl中实现StreamingChatListener时的关键考虑：

#### 6.1.1 取消检查的实现

```java
private boolean checkCancellation(AgentChatExecutionContext context, FluxSink<ResultVO<StreamingChatVO>> sink) {
    boolean cancelled = chatCachePort.isSessionCancelled(
        context.getSessionId(), 
        context.getUserId(), 
        context.getLearningSpaceId()
    );
    
    if (cancelled) {
        // 保存当前状态并结束响应
        handleCancellation(context, sink);
        return true;
    }
    return false;
}
```

#### 6.1.2 工具批次消息的管理

使用ThreadLocal或实例变量保存当前处理的工具批次：

```java
private volatile ToolBatchMessage currentToolBatch;
private final StringBuilder contentBuffer = new StringBuilder();
```

### 6.2 数据持久化策略

#### 6.2.1 保存时机详细分析

1. **内容消息保存**：
    - 时机：onCompleteMessage() 且 finishReason != TOOL_EXECUTION
    - 内容：contentBuffer.toString()
    - 类型：AgentChatMessageTypeEnum.NORMAL

2. **工具批次保存**：
    - 时机1：onReceiveCompleteToolList() 需要授权时立即保存
    - 时机2：onAllToolsComplete() 自动执行完成后保存
    - 内容：ToolBatchMessage序列化的JSON
    - 类型：AgentChatMessageTypeEnum.TOOL_BATCH

3. **取消时保存**：
    - 保存当前contentBuffer内容（如果有）
    - 更新未完成的工具批次状态为CANCELLED

#### 6.2.2 JSON序列化工具类设计

```java
@Component
public class ToolMessageSerializer {
    private final ObjectMapper objectMapper;
    
    public String serialize(ToolBatchMessage message) {
        // 序列化逻辑
    }
    
    public ToolBatchMessage deserialize(String json) {
        // 反序列化逻辑
    }
}
```

### 6.3 授权机制实现

#### 6.3.1 授权检查逻辑

在onReceiveCompleteToolList()中的详细判断：

```java
@Override
public ToolBatchDecision onReceiveCompleteToolList(
    List<ToolExecutionRequest> requests, 
    TokenUsage tokenUsage, 
    AgentChatExecutionContext context) {
    
    // 1. 检查取消状态
    if (checkCancellation(context, sink)) {
        return ToolBatchDecision.DEFER_AND_CLOSE;
    }
    
    // 2. 创建工具批次对象
    currentToolBatch = createToolBatch(requests, tokenUsage, context);
    
    // 3. 检查自动执行权限
    AcsAutoCallToolPermissionEnum permission = 
        AcsAutoCallToolPermissionEnum.fromCode(currentSession.getAcsIsAutoCallTool());
    
    // 4. 逐个检查工具权限
    boolean needsAuth = false;
    for (ToolExecutionRequest request : requests) {
        if (!canAutoExecute(request, permission)) {
            needsAuth = true;
            break;
        }
    }
    
    // 5. 根据权限决策
    if (needsAuth) {
        // 保存批次消息并等待授权
        currentToolBatch.setStatus(ToolBatchStatus.WAITING_AUTH);
        saveToolBatchMessage(currentToolBatch);
        sink.next(StreamingChatVO.toolAuthRequired(currentToolBatch.getBatchId()));
        sink.complete();
        return ToolBatchDecision.DEFER_AND_CLOSE;
    } else {
        // 自动执行
        currentToolBatch.setStatus(ToolBatchStatus.PROCESSING);
        return ToolBatchDecision.PROCEED;
    }
}
```

#### 6.3.2 工具权限判断细节

```java
private boolean canAutoExecute(ToolExecutionRequest request, AcsAutoCallToolPermissionEnum permission) {
    if (permission == null || permission == AcsAutoCallToolPermissionEnum.CLOSED) {
        return false;
    }
    
    AcsToolEnum toolEnum = AcsToolEnum.getByName(request.name());
    if (toolEnum == null) {
        // 未知工具需要授权
        return false;
    }
    
    return switch (permission) {
        case READ_ONLY -> toolEnum.isReadOperation() && !toolEnum.isWriteOperation();
        case WRITE_ONLY -> toolEnum.isWriteOperation();
        case read_WRITE -> toolEnum.isReadOperation() || toolEnum.isWriteOperation();
        default -> false;
    };
}
```

### 6.4 状态同步与一致性

#### 6.4.1 工具执行状态更新

```java
@Override
public ToolDecision onToolExecutionStart(ToolExecutionRequest request, AgentChatExecutionContext context) {
    // 检查取消
    if (checkCancellation(context, sink)) {
        return ToolDecision.DEFER_AND_CLOSE;
    }
    
    // 更新工具状态
    updateToolRequestStatus(request.id(), ToolRequestStatus.EXECUTING);
    
    // 发送工具开始事件
    sink.next(StreamingChatVO.toolExecutionStart(request.name(), request.id()));
    
    return ToolDecision.EXECUTE;
}

@Override
public ToolContinueDecision onToolExecutionFinish(ToolExecutionResult result, AgentChatExecutionContext context) {
    // 检查取消
    if (checkCancellation(context, sink)) {
        return ToolContinueDecision.TERMINATE;
    }
    
    // 更新工具状态和结果
    updateToolRequestResult(result.getRequest().id(), result);
    
    // 发送工具完成事件
    sink.next(StreamingChatVO.toolExecutionFinish(result.getRequest().name(), result.getResultText()));
    
    return ToolContinueDecision.CONTINUE;
}
```

#### 6.4.2 批次完成处理

```java
@Override
public LoopDecision onAllToolsComplete(AgentChatExecutionContext context) {
    // 检查取消
    if (checkCancellation(context, sink)) {
        return LoopDecision.END_RESPONSE;
    }
    
    // 更新批次状态
    currentToolBatch.setStatus(ToolBatchStatus.COMPLETED);
    currentToolBatch.setUpdatedAt(LocalDateTime.now());
    
    // 保存完整的工具批次消息
    saveToolBatchMessage(currentToolBatch);
    
    // 发送批次完成事件
    sink.next(StreamingChatVO.toolBatchComplete(currentToolBatch.getBatchId()));
    
    return LoopDecision.CONTINUE_LOOP;
}
```

## 7. 异常处理与边界情况

### 7.1 取消时的数据一致性

当检测到取消时，需要确保：

1. 当前生成的内容被保存
2. 未完成的工具批次状态正确更新
3. 会话状态恢复正常
4. 缓存标志被清理

### 7.2 并发访问控制

- 使用ConcurrentHashMap管理活跃会话
- 工具批次更新使用乐观锁
- 取消检查使用原子操作

### 7.3 内存管理

- 及时清理完成的工具批次对象
- 限制contentBuffer大小
- 使用弱引用管理长期持有的对象

## 8. 测试策略

### 8.1 单元测试覆盖

1. **工具权限判断测试**
2. **取消检查逻辑测试**
3. **JSON序列化/反序列化测试**
4. **状态转换测试**

### 8.2 集成测试场景

1. **正常流程测试**：自动执行工具的完整流程
2. **授权流程测试**：需要人工确认的工具执行
3. **取消流程测试**：各个阶段的取消处理
4. **异常恢复测试**：网络中断、系统重启等场景

## 9. 性能优化考虑

### 9.1 缓存策略

- 工具权限配置缓存
- 会话状态缓存
- 序列化结果缓存

### 9.2 异步处理

- 工具执行结果的异步保存
- 状态更新的批量处理
- 缓存操作的异步化

## 10. 下一步实施计划

### 10.1 第一阶段：核心数据结构

1. 创建ToolBatchMessage、ToolRequestItem类
2. 扩展AgentChatMessageTypeEnum
3. 实现JSON序列化工具类

### 10.2 第二阶段：StreamingChatListener实现

1. 在AgentChatAppServiceImpl中实现所有回调方法
2. 添加取消检查逻辑
3. 实现工具权限判断

### 10.3 第三阶段：授权机制

1. 创建工具授权API
2. 实现授权后的继续执行逻辑
3. 添加前端授权界面支持

### 10.4 第四阶段：测试与优化

1. 编写全面的单元测试
2. 进行集成测试
3. 性能优化和内存管理

---

*本设计方案基于当前的StreamingChatListener接口和StreamingChatToolServiceImpl实现，确保与现有架构完全兼容*