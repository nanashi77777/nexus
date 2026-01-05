# StreamingChatListener实现策略详细说明

## 1. 实现概述

### 1.1 核心职责

`StreamingChatListener` 的实现需要承担以下关键职责：

1. **取消检查机制** - 在每个回调中检查用户是否取消了响应
2. **消息保存策略** - 在适当时机保存AI消息和工具批次信息
3. **工具批次管理** - 管理工具请求的生命周期和状态变化
4. **授权流程控制** - 处理需要用户授权的工具执行
5. **异常处理** - 处理各种异常情况并确保数据一致性
6. **流式响应控制** - 通过FluxSink控制响应流的完成

### 1.2 实现类结构

```java
package cn.lin037.nexus.application.agent.service.impl;

import cn.lin037.nexus.application.agent.port.AgentCachePort;
import cn.lin037.nexus.application.agent.model.dto.ToolBatchMessage;
import cn.lin037.nexus.application.agent.util.ToolMessageSerializer;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

/**
 * Agent聊天流式监听器实现
 * 负责处理AI流式响应中的各种事件，包括取消检查、消息保存、工具管理等
 */
@Slf4j
public class AgentStreamingChatListener implements StreamingChatListener {
    
    // 依赖注入的服务
    private final AgentCachePort agentCachePort;
    private final ToolMessageSerializer toolMessageSerializer;
    private final AgentChatMessageRepository messageRepository;
    private final FluxSink<ResultVO<StreamingChatVO>> sink;
    
    // 会话上下文信息
    private final AgentChatSessionEntity session;
    private final Long userId;
    private final String sessionCacheKey;
    
    // 当前工具批次管理
    private ToolBatchMessage currentToolBatch;
    private boolean isToolBatchSaved = false;
    
    // 当前AI消息内容累积
    private StringBuilder currentAiContent = new StringBuilder();
    private boolean isAiMessageSaved = false;
    
    // 构造函数...
}
```

## 2. 取消检查机制详细设计

### 2.1 取消检查时机分析

根据用户需求，需要在"各个回调的恰当位置"进行取消检查。分析各个回调的特点：

| 回调方法                        | 调用频率 | 取消检查必要性 | 检查时机说明       |
|-----------------------------|------|---------|--------------|
| `onThinkingToken`           | 极高频  | **低**   | 思考阶段，用户较少取消  |
| `onThinkingComplete`        | 低频   | **中**   | 思考完成，适合检查    |
| `onContentToken`            | 高频   | **中**   | 内容生成中，需要平衡性能 |
| `onCompleteMessage`         | 低频   | **高**   | 消息完成，关键检查点   |
| `onReceiveCompleteToolList` | 低频   | **高**   | 工具列表接收，关键检查点 |
| `onToolExecutionStart`      | 低频   | **高**   | 工具开始执行，关键检查点 |
| `onToolExecutionFinish`     | 低频   | **高**   | 工具执行完成，关键检查点 |
| `onAllToolsComplete`        | 低频   | **高**   | 所有工具完成，关键检查点 |
| `onStreamingComplete`       | 低频   | **高**   | 流式完成，最终检查点   |
| `onError`                   | 低频   | **高**   | 错误处理，必须检查    |

### 2.2 取消检查实现策略

```java
/**
 * 统一的取消检查方法
 * 
 * @param checkPoint 检查点名称，用于日志记录
 * @return true表示已取消，false表示继续执行
 */
private boolean checkCancellation(String checkPoint) {
    try {
        // 从缓存中获取取消标识
        Boolean isCancelled = agentCachePort.get(sessionCacheKey, Boolean.class);
        
        if (Boolean.TRUE.equals(isCancelled)) {
            log.info("检测到用户取消响应，检查点: {}, 会话: {}", checkPoint, session.getAcsId());
            
            // 保存当前状态并完成流式响应
            handleCancellation(checkPoint);
            return true;
        }
        
        return false;
        
    } catch (Exception e) {
        log.error("取消检查失败，检查点: {}, 会话: {}", checkPoint, session.getAcsId(), e);
        // 检查失败时继续执行，避免因缓存问题中断正常流程
        return false;
    }
}

/**
 * 处理取消操作
 * 保存当前状态并完成流式响应
 */
private void handleCancellation(String checkPoint) {
    try {
        // 1. 保存当前AI消息（如果有内容且未保存）
        saveCurrentAiMessageIfNeeded("用户取消 - " + checkPoint);
        
        // 2. 保存当前工具批次（如果有且未保存）
        saveCurrentToolBatchIfNeeded(ToolBatchStatus.CANCELLED, "用户取消 - " + checkPoint);
        
        // 3. 发送取消通知给前端
        sendCancellationNotice(checkPoint);
        
        // 4. 完成流式响应
        sink.complete();
        
        log.info("用户取消处理完成，检查点: {}, 会话: {}", checkPoint, session.getAcsId());
        
    } catch (Exception e) {
        log.error("处理用户取消时发生错误，检查点: {}, 会话: {}", checkPoint, session.getAcsId(), e);
        // 即使处理失败也要完成流式响应
        sink.error(new RuntimeException("取消处理失败", e));
    }
}
```

### 2.3 各回调中的取消检查实现

```java
@Override
public ThinkingDecision onThinkingToken(String token) {
    // 思考阶段不进行取消检查，避免过度频繁的缓存访问
    
    // 发送思考token给前端
    sendThinkingToken(token);
    
    return ThinkingDecision.CONTINUE;
}

@Override
public ThinkingDecision onThinkingComplete(String fullThinkingContent) {
    // 思考完成时进行取消检查
    if (checkCancellation("onThinkingComplete")) {
        return ThinkingDecision.TERMINATE;
    }
    
    // 发送思考完成通知
    sendThinkingComplete(fullThinkingContent);
    
    return ThinkingDecision.CONTINUE;
}

@Override
public ContentDecision onContentToken(String token) {
    // 内容token生成时，每10个token检查一次取消（平衡性能和响应性）
    static int tokenCount = 0;
    if (++tokenCount % 10 == 0) {
        if (checkCancellation("onContentToken")) {
            return ContentDecision.TERMINATE;
        }
    }
    
    // 累积AI消息内容
    currentAiContent.append(token);
    
    // 发送内容token给前端
    sendContentToken(token);
    
    return ContentDecision.CONTINUE;
}

@Override
public void onCompleteMessage(AiMessage message) {
    // 消息完成时必须检查取消
    if (checkCancellation("onCompleteMessage")) {
        return;
    }
    
    // 保存完整的AI消息
    saveAiMessage(message);
    
    // 发送消息完成通知
    sendMessageComplete(message);
}

@Override
public ToolContinueDecision onReceiveCompleteToolList(List<ToolExecutionRequest> toolExecutionRequests) {
    // 接收工具列表时必须检查取消
    if (checkCancellation("onReceiveCompleteToolList")) {
        return ToolContinueDecision.TERMINATE;
    }
    
    // 创建工具批次
    createToolBatch(toolExecutionRequests);
    
    // 检查是否需要用户授权
    if (needsUserAuthorization()) {
        // 保存工具批次并等待授权
        saveCurrentToolBatchIfNeeded(ToolBatchStatus.WAITING_AUTH, "等待用户授权");
        sendAuthorizationRequest();
        sink.complete(); // 结束当前流式响应，等待用户授权
        return ToolContinueDecision.TERMINATE;
    }
    
    return ToolContinueDecision.CONTINUE;
}

@Override
public void onToolExecutionStart(ToolExecutionRequest request) {
    // 工具开始执行时检查取消
    if (checkCancellation("onToolExecutionStart")) {
        return;
    }
    
    // 更新工具状态为执行中
    updateToolStatus(request.id(), ToolRequestStatus.EXECUTING);
    
    // 发送工具执行开始通知
    sendToolExecutionStart(request);
}

@Override
public void onToolExecutionFinish(ToolExecutionRequest request, String result) {
    // 工具执行完成时检查取消
    if (checkCancellation("onToolExecutionFinish")) {
        return;
    }
    
    // 更新工具状态和结果
    updateToolResult(request.id(), ToolRequestStatus.COMPLETED, result);
    
    // 发送工具执行完成通知
    sendToolExecutionFinish(request, result);
}

@Override
public void onAllToolsComplete() {
    // 所有工具完成时检查取消
    if (checkCancellation("onAllToolsComplete")) {
        return;
    }
    
    // 完成工具批次
    completeToolBatch();
    
    // 发送所有工具完成通知
    sendAllToolsComplete();
}

@Override
public void onStreamingComplete() {
    // 流式完成时进行最终检查
    if (checkCancellation("onStreamingComplete")) {
        return;
    }
    
    // 确保所有数据都已保存
    saveCurrentAiMessageIfNeeded("流式响应完成");
    saveCurrentToolBatchIfNeeded(ToolBatchStatus.COMPLETED, "流式响应完成");
    
    // 发送流式完成通知
    sendStreamingComplete();
    
    // 完成流式响应
    sink.complete();
}

@Override
public void onError(Throwable error) {
    // 错误处理时也要检查取消（可能是因为取消导致的错误）
    boolean wasCancelled = checkCancellation("onError");
    
    if (!wasCancelled) {
        // 不是取消导致的错误，进行错误处理
        handleError(error);
    }
    
    // 无论如何都要结束流式响应
    sink.error(error);
}
```

## 3. 消息保存策略详细设计

### 3.1 AI消息保存时机分析

根据用户提到的"onAiMessage可能返回工具响应"的冲突问题，需要仔细设计消息保存策略：

**问题分析：**

- `onCompleteMessage(AiMessage message)` 会接收到完整的AI消息
- 这个消息可能包含纯文本内容，也可能包含工具调用请求
- 如果包含工具调用，那么这个消息与工具批次消息存在重叠

**解决方案：**

1. **分离内容和工具调用** - 将AI消息的文本内容和工具调用分开处理
2. **避免重复保存** - 工具调用信息只在工具批次中保存，AI消息只保存文本内容
3. **建立关联关系** - 通过批次ID建立AI消息与工具批次的关联

### 3.2 消息保存实现

```java
/**
 * 保存AI消息（仅保存文本内容，不包含工具调用）
 */
private void saveAiMessage(AiMessage message) {
    try {
        // 提取纯文本内容（排除工具调用部分）
        String textContent = extractTextContent(message);
        
        if (StrUtil.isNotBlank(textContent)) {
            AgentChatMessageEntity messageEntity = AgentChatMessageEntity.builder()
                .acmSessionId(session.getAcsId())
                .acmUserId(userId)
                .acmLearningSpaceId(session.getAcsLearningSpaceId())
                .acmType(AgentChatMessageTypeEnum.AI_MESSAGE.getCode())
                .acmRole(AgentChatMessageRoleEnum.ASSISTANT.getRole())
                .acmContent(textContent)
                .acmTokenUsage(extractTokenUsage(message))
                .acmCreatedAt(LocalDateTime.now())
                .build();
            
            // 如果存在工具批次，建立关联
            if (currentToolBatch != null) {
                messageEntity.setAcmRelatedBatchId(currentToolBatch.getBatchId());
            }
            
            messageRepository.save(messageEntity);
            isAiMessageSaved = true;
            
            log.info("AI消息已保存，会话: {}, 内容长度: {}", session.getAcsId(), textContent.length());
        }
        
    } catch (Exception e) {
        log.error("保存AI消息失败，会话: {}", session.getAcsId(), e);
        throw new RuntimeException("保存AI消息失败", e);
    }
}

/**
 * 从AiMessage中提取纯文本内容
 * 排除工具调用相关的内容
 */
private String extractTextContent(AiMessage message) {
    if (message == null) {
        return currentAiContent.toString().trim();
    }
    
    // 如果消息包含工具调用，只返回文本部分
    if (message.hasToolExecutionRequests()) {
        // 从消息中提取文本内容，排除工具调用JSON
        return extractTextFromMixedContent(message.text());
    }
    
    // 纯文本消息直接返回
    return message.text();
}

/**
 * 从混合内容中提取纯文本（排除工具调用JSON）
 */
private String extractTextFromMixedContent(String content) {
    if (StrUtil.isBlank(content)) {
        return "";
    }
    
    // 使用正则表达式或JSON解析来分离文本和工具调用
    // 这里需要根据实际的消息格式来实现
    // 示例实现（需要根据实际格式调整）：
    
    try {
        // 假设工具调用以特定格式包装，如 <tool_call>...</tool_call>
        String textOnly = content.replaceAll("<tool_call>.*?</tool_call>", "").trim();
        return textOnly;
    } catch (Exception e) {
        log.warn("提取纯文本内容失败，返回原始内容，会话: {}", session.getAcsId(), e);
        return content;
    }
}

/**
 * 条件保存当前AI消息
 */
private void saveCurrentAiMessageIfNeeded(String reason) {
    if (!isAiMessageSaved && currentAiContent.length() > 0) {
        try {
            AgentChatMessageEntity messageEntity = AgentChatMessageEntity.builder()
                .acmSessionId(session.getAcsId())
                .acmUserId(userId)
                .acmLearningSpaceId(session.getAcsLearningSpaceId())
                .acmType(AgentChatMessageTypeEnum.AI_MESSAGE.getCode())
                .acmRole(AgentChatMessageRoleEnum.ASSISTANT.getRole())
                .acmContent(currentAiContent.toString().trim())
                .acmCreatedAt(LocalDateTime.now())
                .build();
            
            // 添加保存原因到备注
            messageEntity.setAcmRemark(reason);
            
            // 如果存在工具批次，建立关联
            if (currentToolBatch != null) {
                messageEntity.setAcmRelatedBatchId(currentToolBatch.getBatchId());
            }
            
            messageRepository.save(messageEntity);
            isAiMessageSaved = true;
            
            log.info("条件保存AI消息完成，原因: {}, 会话: {}, 内容长度: {}", 
                reason, session.getAcsId(), currentAiContent.length());
                
        } catch (Exception e) {
            log.error("条件保存AI消息失败，原因: {}, 会话: {}", reason, session.getAcsId(), e);
        }
    }
}
```

## 4. 工具批次管理详细设计

### 4.1 工具批次生命周期管理

```java
/**
 * 创建工具批次
 */
private void createToolBatch(List<ToolExecutionRequest> requests) {
    try {
        String batchId = ToolBatchMessage.generateBatchId();
        
        // 转换工具请求为内部格式
        List<ToolRequestItem> toolItems = requests.stream()
            .map(ToolRequestItem::fromToolExecutionRequest)
            .collect(Collectors.toList());
        
        currentToolBatch = ToolBatchMessage.builder()
            .batchId(batchId)
            .sessionId(session.getAcsId())
            .userId(userId)
            .learningSpaceId(session.getAcsLearningSpaceId())
            .aiMessageContent(currentAiContent.toString().trim())
            .toolRequests(toolItems)
            .status(ToolBatchStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        log.info("工具批次已创建，批次ID: {}, 工具数量: {}, 会话: {}", 
            batchId, requests.size(), session.getAcsId());
            
    } catch (Exception e) {
        log.error("创建工具批次失败，会话: {}", session.getAcsId(), e);
        throw new RuntimeException("创建工具批次失败", e);
    }
}

/**
 * 检查是否需要用户授权
 */
private boolean needsUserAuthorization() {
    // 检查会话设置
    if (session.getAcsAutoExecuteLevel() == null || 
        session.getAcsAutoExecuteLevel() == AgentAutoExecuteLevelEnum.MANUAL) {
        return true;
    }
    
    // 检查工具风险级别
    if (currentToolBatch != null) {
        return currentToolBatch.getToolRequests().stream()
            .anyMatch(this::isHighRiskTool);
    }
    
    return false;
}

/**
 * 判断是否为高风险工具
 */
private boolean isHighRiskTool(ToolRequestItem tool) {
    // 根据工具名称和参数判断风险级别
    String toolName = tool.getToolName();
    
    // 文件操作、系统命令等为高风险
    if (toolName.contains("file") || toolName.contains("system") || 
        toolName.contains("delete") || toolName.contains("execute")) {
        return true;
    }
    
    // 可以根据参数进一步判断
    // ...
    
    return false;
}

/**
 * 更新工具状态
 */
private void updateToolStatus(String requestId, ToolRequestStatus newStatus) {
    if (currentToolBatch == null) {
        log.warn("尝试更新工具状态但工具批次为空，请求ID: {}", requestId);
        return;
    }
    
    ToolRequestItem tool = findToolByRequestId(requestId);
    if (tool != null) {
        tool.setStatus(newStatus);
        tool.setExecutionStartedAt(LocalDateTime.now());
        currentToolBatch.setUpdatedAt(LocalDateTime.now());
        
        // 如果工具批次已保存，更新数据库
        if (isToolBatchSaved) {
            updateToolBatchInDatabase();
        }
    }
}

/**
 * 更新工具执行结果
 */
private void updateToolResult(String requestId, ToolRequestStatus status, String result) {
    if (currentToolBatch == null) {
        return;
    }
    
    ToolRequestItem tool = findToolByRequestId(requestId);
    if (tool != null) {
        tool.setStatus(status);
        tool.setExecutionResult(result);
        tool.setExecutedAt(LocalDateTime.now());
        
        // 计算执行耗时
        if (tool.getExecutionStartedAt() != null) {
            long duration = Duration.between(tool.getExecutionStartedAt(), tool.getExecutedAt()).toMillis();
            tool.setExecutionTimeMs(duration);
        }
        
        currentToolBatch.setUpdatedAt(LocalDateTime.now());
        
        // 如果工具批次已保存，更新数据库
        if (isToolBatchSaved) {
            updateToolBatchInDatabase();
        }
    }
}

/**
 * 完成工具批次
 */
private void completeToolBatch() {
    if (currentToolBatch == null) {
        return;
    }
    
    currentToolBatch.setStatus(ToolBatchStatus.COMPLETED);
    currentToolBatch.setCompletedAt(LocalDateTime.now());
    currentToolBatch.setUpdatedAt(LocalDateTime.now());
    
    // 保存或更新工具批次
    saveCurrentToolBatchIfNeeded(ToolBatchStatus.COMPLETED, "所有工具执行完成");
}

/**
 * 条件保存工具批次
 */
private void saveCurrentToolBatchIfNeeded(ToolBatchStatus status, String reason) {
    if (currentToolBatch == null) {
        return;
    }
    
    try {
        currentToolBatch.setStatus(status);
        currentToolBatch.setUpdatedAt(LocalDateTime.now());
        
        if (!isToolBatchSaved) {
            // 首次保存
            AgentChatMessageEntity messageEntity = AgentChatMessageEntity.builder()
                .acmSessionId(session.getAcsId())
                .acmUserId(userId)
                .acmLearningSpaceId(session.getAcsLearningSpaceId())
                .acmType(AgentChatMessageTypeEnum.TOOL_BATCH.getCode())
                .acmRole(AgentChatMessageRoleEnum.ASSISTANT.getRole())
                .acmContent(toolMessageSerializer.serialize(currentToolBatch))
                .acmCreatedAt(LocalDateTime.now())
                .acmRemark(reason)
                .build();
            
            messageRepository.save(messageEntity);
            isToolBatchSaved = true;
            
            log.info("工具批次已保存，批次ID: {}, 状态: {}, 原因: {}", 
                currentToolBatch.getBatchId(), status, reason);
        } else {
            // 更新已保存的批次
            updateToolBatchInDatabase();
        }
        
    } catch (Exception e) {
        log.error("保存工具批次失败，批次ID: {}, 原因: {}", 
            currentToolBatch.getBatchId(), reason, e);
    }
}

/**
 * 更新数据库中的工具批次
 */
private void updateToolBatchInDatabase() {
    try {
        AgentChatMessageEntity messageEntity = messageRepository
            .findToolBatchByBatchId(currentToolBatch.getBatchId())
            .orElseThrow(() -> new RuntimeException("工具批次不存在: " + currentToolBatch.getBatchId()));
        
        messageEntity.setAcmContent(toolMessageSerializer.serialize(currentToolBatch));
        messageRepository.updateById(messageEntity);
        
        log.debug("工具批次已更新，批次ID: {}", currentToolBatch.getBatchId());
        
    } catch (Exception e) {
        log.error("更新工具批次失败，批次ID: {}", currentToolBatch.getBatchId(), e);
    }
}
```

## 5. 授权流程控制详细设计

### 5.1 授权请求发送

```java
/**
 * 发送授权请求给前端
 */
private void sendAuthorizationRequest() {
    try {
        ToolAuthRequestVO authRequest = buildAuthRequest();
        
        StreamingChatVO authVO = StreamingChatVO.builder()
            .eventType(StreamingChatVO.EventType.TOOL_AUTH_REQUEST)
            .content(authRequest)
            .timestamp(LocalDateTime.now())
            .build();
        
        ResultVO<StreamingChatVO> result = ResultVO.success(authVO);
        sink.next(result);
        
        log.info("授权请求已发送，批次ID: {}, 工具数量: {}", 
            currentToolBatch.getBatchId(), currentToolBatch.getToolRequests().size());
            
    } catch (Exception e) {
        log.error("发送授权请求失败，批次ID: {}", currentToolBatch.getBatchId(), e);
        sink.error(new RuntimeException("发送授权请求失败", e));
    }
}

/**
 * 构建授权请求VO
 */
private ToolAuthRequestVO buildAuthRequest() {
    List<ToolAuthItemVO> toolItems = currentToolBatch.getToolRequests().stream()
        .map(this::buildToolAuthItem)
        .collect(Collectors.toList());
    
    return ToolAuthRequestVO.builder()
        .batchId(currentToolBatch.getBatchId())
        .sessionId(session.getAcsId().toString())
        .aiMessage(currentToolBatch.getAiMessageContent())
        .tools(toolItems)
        .requestTime(LocalDateTime.now())
        .build();
}

/**
 * 构建单个工具授权项
 */
private ToolAuthItemVO buildToolAuthItem(ToolRequestItem tool) {
    return ToolAuthItemVO.builder()
        .requestId(tool.getRequestId())
        .toolName(tool.getToolName())
        .toolDescription(getToolDescription(tool.getToolName()))
        .arguments(parseToolArguments(tool.getArguments()))
        .isReadOperation(isReadOperation(tool))
        .isWriteOperation(isWriteOperation(tool))
        .riskLevel(calculateRiskLevel(tool))
        .build();
}
```

## 6. 前端通信协议设计

### 6.1 StreamingChatVO事件类型扩展

```java
// 在StreamingChatVO.EventType中添加新的事件类型

public enum EventType {
    // 现有类型...
    THINKING_TOKEN("thinking_token"),
    THINKING_COMPLETE("thinking_complete"),
    CONTENT_TOKEN("content_token"),
    MESSAGE_COMPLETE("message_complete"),
    
    // 新增工具相关类型
    TOOL_LIST_RECEIVED("tool_list_received"),
    TOOL_EXECUTION_START("tool_execution_start"),
    TOOL_EXECUTION_FINISH("tool_execution_finish"),
    ALL_TOOLS_COMPLETE("all_tools_complete"),
    
    // 授权相关类型
    TOOL_AUTH_REQUEST("tool_auth_request"),
    
    // 取消和错误类型
    CANCELLED("cancelled"),
    ERROR("error"),
    
    // 完成类型
    STREAMING_COMPLETE("streaming_complete");
}
```

### 6.2 前端通信方法实现

```java
/**
 * 发送思考token
 */
private void sendThinkingToken(String token) {
    StreamingChatVO vo = StreamingChatVO.builder()
        .eventType(StreamingChatVO.EventType.THINKING_TOKEN)
        .content(token)
        .timestamp(LocalDateTime.now())
        .build();
    
    sink.next(ResultVO.success(vo));
}

/**
 * 发送内容token
 */
private void sendContentToken(String token) {
    StreamingChatVO vo = StreamingChatVO.builder()
        .eventType(StreamingChatVO.EventType.CONTENT_TOKEN)
        .content(token)
        .timestamp(LocalDateTime.now())
        .build();
    
    sink.next(ResultVO.success(vo));
}

/**
 * 发送工具执行开始通知
 */
private void sendToolExecutionStart(ToolExecutionRequest request) {
    Map<String, Object> data = Map.of(
        "requestId", request.id(),
        "toolName", request.name(),
        "arguments", request.arguments()
    );
    
    StreamingChatVO vo = StreamingChatVO.builder()
        .eventType(StreamingChatVO.EventType.TOOL_EXECUTION_START)
        .content(data)
        .timestamp(LocalDateTime.now())
        .build();
    
    sink.next(ResultVO.success(vo));
}

/**
 * 发送取消通知
 */
private void sendCancellationNotice(String checkPoint) {
    Map<String, Object> data = Map.of(
        "reason", "用户取消",
        "checkPoint", checkPoint,
        "timestamp", LocalDateTime.now()
    );
    
    StreamingChatVO vo = StreamingChatVO.builder()
        .eventType(StreamingChatVO.EventType.CANCELLED)
        .content(data)
        .timestamp(LocalDateTime.now())
        .build();
    
    sink.next(ResultVO.success(vo));
}
```

## 7. 异常处理和边界情况

### 7.1 异常处理策略

```java
/**
 * 统一异常处理
 */
private void handleError(Throwable error) {
    try {
        // 保存当前状态
        saveCurrentAiMessageIfNeeded("发生错误: " + error.getMessage());
        saveCurrentToolBatchIfNeeded(ToolBatchStatus.FAILED, "发生错误: " + error.getMessage());
        
        // 发送错误通知给前端
        sendErrorNotification(error);
        
        log.error("流式响应处理发生错误，会话: {}", session.getAcsId(), error);
        
    } catch (Exception e) {
        log.error("错误处理过程中发生异常，会话: {}", session.getAcsId(), e);
    }
}

/**
 * 发送错误通知
 */
private void sendErrorNotification(Throwable error) {
    Map<String, Object> errorData = Map.of(
        "message", error.getMessage(),
        "type", error.getClass().getSimpleName(),
        "timestamp", LocalDateTime.now()
    );
    
    StreamingChatVO vo = StreamingChatVO.builder()
        .eventType(StreamingChatVO.EventType.ERROR)
        .content(errorData)
        .timestamp(LocalDateTime.now())
        .build();
    
    sink.next(ResultVO.success(vo));
}
```

### 7.2 边界情况处理

1. **缓存服务不可用**
    - 取消检查失败时继续执行，避免因缓存问题中断正常流程
    - 记录警告日志，但不抛出异常

2. **数据库保存失败**
    - 记录错误日志，但继续流式响应
    - 可以考虑重试机制或降级处理

3. **工具执行超时**
    - 在工具执行过程中定期检查取消状态
    - 设置合理的超时时间

4. **并发访问冲突**
    - 使用乐观锁或悲观锁保护关键数据
    - 处理并发更新冲突

---

*此文档详细说明了StreamingChatListener的完整实现策略，确保在各种情况下都能正确处理取消检查、消息保存和工具管理*