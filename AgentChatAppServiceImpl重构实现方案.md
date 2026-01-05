# AgentChatAppServiceImpl重构实现方案

## 1. 重构概述

### 1.1 重构目标

基于用户需求和现有的 `StreamingChatToolServiceImpl` 和 `StreamingChatListener` 接口，重构 `AgentChatAppServiceImpl` 以实现：

1. **正确的流式响应处理** - 使用 `Flux.create` 创建响应式流
2. **取消机制集成** - 通过 `AgentCachePort` 实现取消检查
3. **工具批次管理** - 完整的工具请求生命周期管理
4. **授权流程支持** - 支持需要用户授权的工具执行
5. **异常处理和数据一致性** - 确保各种异常情况下的数据完整性

### 1.2 核心架构变更

```
原有架构：
AgentChatAppServiceImpl -> agentChatPort.chatStream() -> 直接返回结果

新架构：
AgentChatAppServiceImpl -> Flux.create() -> AgentStreamingChatListener -> 
  -> StreamingChatToolServiceImpl.chat() -> 异步回调处理
```

## 2. 类结构重构设计

### 2.1 依赖注入重构

```java
package cn.lin037.nexus.application.agent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.lin037.nexus.application.agent.port.AgentCachePort;
import cn.lin037.nexus.application.agent.port.AgentChatPort;
import cn.lin037.nexus.application.agent.service.AgentChatAppService;
import cn.lin037.nexus.application.agent.util.ToolMessageSerializer;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatToolService;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatSessionEntity;
import cn.lin037.nexus.infrastructure.common.persistent.repository.agent.AgentChatMessageRepository;
import cn.lin037.nexus.infrastructure.common.persistent.repository.agent.AgentChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent聊天应用服务实现
 * 重构后支持完整的流式响应、取消机制和工具管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentChatAppServiceImpl implements AgentChatAppService {
    
    // 核心依赖服务
    private final AgentCachePort agentCachePort;
    private final AgentChatPort agentChatPort;
    private final StreamingChatToolService streamingChatToolService;
    private final ToolMessageSerializer toolMessageSerializer;
    
    // 数据访问层
    private final AgentChatSessionRepository sessionRepository;
    private final AgentChatMessageRepository messageRepository;
    
    // 活跃会话管理（用于取消操作）
    private final Map<Long, FluxSink<ResultVO<StreamingChatVO>>> activeSessions = new ConcurrentHashMap<>();
    
    // 其他现有方法保持不变...
}
```

### 2.2 streamingChat方法完整重构

```java
@Override
public Flux<ResultVO<StreamingChatVO>> streamingChat(AgentChatStreamingReq req) {
    return Flux.create(sink -> {
        try {
            // 1. 参数验证和预处理
            validateStreamingRequest(req);
            
            // 2. 获取用户信息和会话
            Long userId = StpUtil.getLoginIdAsLong();
            AgentChatSessionEntity session = getOrCreateSession(req.getSessionId(), userId);
            
            // 3. 构建缓存键和注册活跃会话
            String sessionCacheKey = buildSessionCacheKey(session.getAcsId(), userId);
            activeSessions.put(session.getAcsId(), sink);
            
            // 4. 设置会话清理逻辑
            sink.onDispose(() -> {
                activeSessions.remove(session.getAcsId());
                cleanupSessionCache(sessionCacheKey);
                log.info("会话流式响应已清理，会话ID: {}", session.getAcsId());
            });
            
            // 5. 保存用户消息
            saveUserMessage(req, session, userId);
            
            // 6. 异步执行流式聊天
            executeStreamingChatAsync(req, session, userId, sessionCacheKey, sink);
            
        } catch (Exception e) {
            log.error("启动流式聊天失败，请求: {}", req, e);
            sink.error(new RuntimeException("启动流式聊天失败", e));
        }
    })
    .doOnError(error -> {
        log.error("流式聊天过程中发生错误", error);
    })
    .onErrorResume(error -> {
        // 错误恢复：返回错误消息给前端
        StreamingChatVO errorVO = StreamingChatVO.builder()
            .eventType(StreamingChatVO.EventType.ERROR)
            .content(Map.of(
                "message", "聊天服务暂时不可用，请稍后重试",
                "error", error.getMessage(),
                "timestamp", LocalDateTime.now()
            ))
            .timestamp(LocalDateTime.now())
            .build();
        
        return Flux.just(ResultVO.success(errorVO));
    });
}
```

### 2.3 异步执行方法实现

```java
/**
 * 异步执行流式聊天
 */
private void executeStreamingChatAsync(
        AgentChatStreamingReq req, 
        AgentChatSessionEntity session, 
        Long userId,
        String sessionCacheKey,
        FluxSink<ResultVO<StreamingChatVO>> sink) {
    
    try {
        // 1. 创建流式监听器
        AgentStreamingChatListener listener = createStreamingListener(
            session, userId, sessionCacheKey, sink);
        
        // 2. 构建聊天请求
        StreamingChatRequest chatRequest = buildChatRequest(req, session);
        
        // 3. 发送开始响应通知
        sendStreamingStartNotification(sink);
        
        // 4. 调用流式聊天服务（异步执行）
        CompletableFuture.runAsync(() -> {
            try {
                streamingChatToolService.chat(chatRequest, listener);
            } catch (Exception e) {
                log.error("流式聊天执行失败，会话: {}", session.getAcsId(), e);
                listener.onError(e);
            }
        }).exceptionally(throwable -> {
            log.error("流式聊天异步执行异常，会话: {}", session.getAcsId(), throwable);
            listener.onError(throwable);
            return null;
        });
        
    } catch (Exception e) {
        log.error("异步执行流式聊天失败，会话: {}", session.getAcsId(), e);
        sink.error(e);
    }
}

/**
 * 创建流式监听器
 */
private AgentStreamingChatListener createStreamingListener(
        AgentChatSessionEntity session, 
        Long userId,
        String sessionCacheKey,
        FluxSink<ResultVO<StreamingChatVO>> sink) {
    
    return new AgentStreamingChatListener(
        agentCachePort,
        toolMessageSerializer,
        messageRepository,
        sink,
        session,
        userId,
        sessionCacheKey
    );
}

/**
 * 构建聊天请求
 */
private StreamingChatRequest buildChatRequest(AgentChatStreamingReq req, AgentChatSessionEntity session) {
    // 获取历史消息
    List<AgentChatMessageEntity> historyMessages = getHistoryMessages(session.getAcsId(), req.getHistoryLimit());
    
    // 转换为聊天消息格式
    List<ChatMessage> chatMessages = convertToChatMessages(historyMessages);
    
    // 添加当前用户消息
    chatMessages.add(UserMessage.from(req.getContent()));
    
    return StreamingChatRequest.builder()
        .messages(chatMessages)
        .modelName(session.getAcsModelName())
        .temperature(session.getAcsTemperature())
        .maxTokens(session.getAcsMaxTokens())
        .tools(getAvailableTools(session))
        .build();
}
```

## 3. 取消机制实现

### 3.1 cancelStreaming方法重构

```java
@Override
public ResultVO<Void> cancelStreaming(AgentChatCancelStreamingReq req) {
    try {
        // 1. 参数验证
        if (req.getSessionId() == null) {
            return ResultVO.fail(AgentChatErrorCodeEnum.INVALID_PARAMETER, "会话ID不能为空");
        }
        
        // 2. 权限检查
        Long userId = StpUtil.getLoginIdAsLong();
        AgentChatSessionEntity session = sessionRepository.selectById(req.getSessionId());
        
        if (session == null) {
            return ResultVO.fail(AgentChatErrorCodeEnum.SESSION_NOT_FOUND, "会话不存在");
        }
        
        if (!userId.equals(session.getAcsUserId())) {
            return ResultVO.fail(AgentChatErrorCodeEnum.NO_PERMISSION, "无权限操作此会话");
        }
        
        // 3. 设置取消标识到缓存
        String sessionCacheKey = buildSessionCacheKey(session.getAcsId(), userId);
        agentCachePort.set(sessionCacheKey, true, Duration.ofMinutes(5));
        
        // 4. 如果会话正在活跃，直接完成流式响应
        FluxSink<ResultVO<StreamingChatVO>> activeSink = activeSessions.get(session.getAcsId());
        if (activeSink != null && !activeSink.isCancelled()) {
            // 发送取消通知
            sendCancellationNotification(activeSink, "用户主动取消");
            
            // 完成流式响应
            activeSink.complete();
            
            log.info("已取消活跃会话的流式响应，会话ID: {}", session.getAcsId());
        }
        
        log.info("流式响应取消请求已处理，会话ID: {}, 用户ID: {}", session.getAcsId(), userId);
        
        return ResultVO.success();
        
    } catch (Exception e) {
        log.error("取消流式响应失败，请求: {}", req, e);
        return ResultVO.fail(AgentChatErrorCodeEnum.CANCEL_FAILED, "取消失败: " + e.getMessage());
    }
}

/**
 * 发送取消通知
 */
private void sendCancellationNotification(FluxSink<ResultVO<StreamingChatVO>> sink, String reason) {
    try {
        StreamingChatVO cancelVO = StreamingChatVO.builder()
            .eventType(StreamingChatVO.EventType.CANCELLED)
            .content(Map.of(
                "reason", reason,
                "timestamp", LocalDateTime.now()
            ))
            .timestamp(LocalDateTime.now())
            .build();
        
        sink.next(ResultVO.success(cancelVO));
        
    } catch (Exception e) {
        log.error("发送取消通知失败", e);
    }
}
```

### 3.2 缓存键管理

```java
/**
 * 构建会话缓存键
 */
private String buildSessionCacheKey(Long sessionId, Long userId) {
    return String.format("agent:chat:session:%d:user:%d:streaming", sessionId, userId);
}

/**
 * 清理会话缓存
 */
private void cleanupSessionCache(String sessionCacheKey) {
    try {
        agentCachePort.delete(sessionCacheKey);
        log.debug("会话缓存已清理: {}", sessionCacheKey);
    } catch (Exception e) {
        log.warn("清理会话缓存失败: {}", sessionCacheKey, e);
    }
}
```

## 4. 工具授权处理

### 4.1 工具授权接口实现

```java
/**
 * 处理工具授权请求
 */
@Override
public ResultVO<Void> authorizeTools(ToolAuthResponseVO authResponse) {
    try {
        // 1. 参数验证
        if (StrUtil.isBlank(authResponse.getBatchId()) || 
            CollUtil.isEmpty(authResponse.getDecisions())) {
            return ResultVO.fail(AgentChatErrorCodeEnum.INVALID_PARAMETER, "授权参数不完整");
        }
        
        // 2. 权限检查
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 3. 查询工具批次
        AgentChatMessageEntity batchEntity = messageRepository
            .findToolBatchByBatchId(authResponse.getBatchId())
            .orElse(null);
        
        if (batchEntity == null) {
            return ResultVO.fail(AgentChatErrorCodeEnum.TOOL_BATCH_NOT_FOUND, "工具批次不存在");
        }
        
        // 4. 权限验证
        if (!userId.equals(batchEntity.getAcmUserId())) {
            return ResultVO.fail(AgentChatErrorCodeEnum.NO_PERMISSION, "无权限操作此工具批次");
        }
        
        // 5. 反序列化工具批次
        ToolBatchMessage toolBatch = toolMessageSerializer.deserialize(batchEntity.getAcmContent());
        
        // 6. 应用授权决策
        applyAuthorizationDecisions(toolBatch, authResponse.getDecisions(), userId);
        
        // 7. 更新工具批次状态
        toolBatch.setStatus(ToolBatchStatus.PROCESSING);
        toolBatch.setUpdatedAt(LocalDateTime.now());
        
        // 8. 保存更新
        batchEntity.setAcmContent(toolMessageSerializer.serialize(toolBatch));
        messageRepository.updateById(batchEntity);
        
        // 9. 继续执行已授权的工具
        continueToolExecution(toolBatch);
        
        log.info("工具授权处理完成，批次ID: {}, 决策数量: {}", 
            authResponse.getBatchId(), authResponse.getDecisions().size());
        
        return ResultVO.success();
        
    } catch (Exception e) {
        log.error("处理工具授权失败，批次ID: {}", authResponse.getBatchId(), e);
        return ResultVO.fail(AgentChatErrorCodeEnum.AUTHORIZATION_FAILED, "授权处理失败: " + e.getMessage());
    }
}

/**
 * 应用授权决策
 */
private void applyAuthorizationDecisions(
        ToolBatchMessage toolBatch, 
        List<ToolAuthDecisionVO> decisions,
        Long userId) {
    
    Map<String, ToolAuthDecisionVO> decisionMap = decisions.stream()
        .collect(Collectors.toMap(ToolAuthDecisionVO::getRequestId, Function.identity()));
    
    for (ToolRequestItem tool : toolBatch.getToolRequests()) {
        ToolAuthDecisionVO decision = decisionMap.get(tool.getRequestId());
        if (decision != null) {
            // 更新工具状态
            switch (decision.getDecision().toUpperCase()) {
                case "APPROVE":
                    tool.setStatus(ToolRequestStatus.PENDING); // 等待执行
                    break;
                case "REJECT":
                    tool.setStatus(ToolRequestStatus.REJECTED);
                    break;
                case "SKIP":
                    tool.setStatus(ToolRequestStatus.SKIPPED);
                    break;
                default:
                    log.warn("未知的授权决策: {}", decision.getDecision());
                    continue;
            }
            
            // 记录授权信息
            tool.setUserDecision(decision.getDecision());
            tool.setAuthorizedAt(LocalDateTime.now());
            tool.setAuthorizedByUserId(userId);
        }
    }
}

/**
 * 继续执行已授权的工具
 */
private void continueToolExecution(ToolBatchMessage toolBatch) {
    // 获取需要执行的工具
    List<ToolRequestItem> approvedTools = toolBatch.getToolRequests().stream()
        .filter(tool -> tool.getStatus() == ToolRequestStatus.PENDING)
        .collect(Collectors.toList());
    
    if (approvedTools.isEmpty()) {
        log.info("没有需要执行的工具，批次ID: {}", toolBatch.getBatchId());
        return;
    }
    
    // 异步执行工具
    CompletableFuture.runAsync(() -> {
        try {
            executeApprovedTools(toolBatch, approvedTools);
        } catch (Exception e) {
            log.error("执行已授权工具失败，批次ID: {}", toolBatch.getBatchId(), e);
        }
    });
}
```

## 5. 消息保存和历史管理

### 5.1 用户消息保存

```java
/**
 * 保存用户消息
 */
private void saveUserMessage(AgentChatStreamingReq req, AgentChatSessionEntity session, Long userId) {
    try {
        AgentChatMessageEntity userMessage = AgentChatMessageEntity.builder()
            .acmSessionId(session.getAcsId())
            .acmUserId(userId)
            .acmLearningSpaceId(session.getAcsLearningSpaceId())
            .acmType(AgentChatMessageTypeEnum.USER_MESSAGE.getCode())
            .acmRole(AgentChatMessageRoleEnum.USER.getRole())
            .acmContent(req.getContent())
            .acmCreatedAt(LocalDateTime.now())
            .build();
        
        messageRepository.save(userMessage);
        
        log.debug("用户消息已保存，会话: {}, 内容长度: {}", session.getAcsId(), req.getContent().length());
        
    } catch (Exception e) {
        log.error("保存用户消息失败，会话: {}", session.getAcsId(), e);
        throw new RuntimeException("保存用户消息失败", e);
    }
}
```

### 5.2 历史消息查询和转换

```java
/**
 * 获取历史消息
 */
private List<AgentChatMessageEntity> getHistoryMessages(Long sessionId, Integer limit) {
    try {
        // 设置默认限制
        int messageLimit = (limit != null && limit > 0) ? Math.min(limit, 50) : 20;
        
        // 查询最近的消息（排除工具批次消息，避免重复）
        return messageRepository.findRecentMessagesBySession(
            sessionId, 
            messageLimit,
            List.of(AgentChatMessageTypeEnum.USER_MESSAGE.getCode(), 
                   AgentChatMessageTypeEnum.AI_MESSAGE.getCode())
        );
        
    } catch (Exception e) {
        log.error("获取历史消息失败，会话: {}", sessionId, e);
        return Collections.emptyList();
    }
}

/**
 * 转换为聊天消息格式
 */
private List<ChatMessage> convertToChatMessages(List<AgentChatMessageEntity> messageEntities) {
    return messageEntities.stream()
        .map(this::convertToChatMessage)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}

/**
 * 转换单个消息
 */
private ChatMessage convertToChatMessage(AgentChatMessageEntity entity) {
    try {
        String role = entity.getAcmRole();
        String content = entity.getAcmContent();
        
        if (StrUtil.isBlank(content)) {
            return null;
        }
        
        switch (role.toLowerCase()) {
            case "user":
                return UserMessage.from(content);
            case "assistant":
                return AiMessage.from(content);
            case "system":
                return SystemMessage.from(content);
            default:
                log.warn("未知的消息角色: {}", role);
                return null;
        }
        
    } catch (Exception e) {
        log.error("转换聊天消息失败，消息ID: {}", entity.getAcmId(), e);
        return null;
    }
}
```

## 6. 工具管理和配置

### 6.1 可用工具获取

```java
/**
 * 获取可用工具列表
 */
private List<ToolSpecification> getAvailableTools(AgentChatSessionEntity session) {
    try {
        // 根据会话配置和用户权限获取可用工具
        List<String> enabledToolNames = parseEnabledTools(session.getAcsEnabledTools());
        
        return toolRegistry.getToolsByNames(enabledToolNames).stream()
            .filter(tool -> hasToolPermission(session.getAcsUserId(), tool.name()))
            .collect(Collectors.toList());
            
    } catch (Exception e) {
        log.error("获取可用工具失败，会话: {}", session.getAcsId(), e);
        return Collections.emptyList();
    }
}

/**
 * 解析启用的工具名称
 */
private List<String> parseEnabledTools(String enabledToolsJson) {
    if (StrUtil.isBlank(enabledToolsJson)) {
        return getDefaultTools();
    }
    
    try {
        return JsonUtil.parseArray(enabledToolsJson, String.class);
    } catch (Exception e) {
        log.warn("解析启用工具配置失败，使用默认配置: {}", enabledToolsJson, e);
        return getDefaultTools();
    }
}

/**
 * 检查工具权限
 */
private boolean hasToolPermission(Long userId, String toolName) {
    // 实现工具权限检查逻辑
    // 可以基于用户角色、工具风险级别等进行判断
    return true; // 简化实现
}
```

## 7. 异常处理和监控

### 7.1 全局异常处理

```java
/**
 * 参数验证
 */
private void validateStreamingRequest(AgentChatStreamingReq req) {
    if (req == null) {
        throw new IllegalArgumentException("请求参数不能为空");
    }
    
    if (StrUtil.isBlank(req.getContent())) {
        throw new IllegalArgumentException("消息内容不能为空");
    }
    
    if (req.getContent().length() > 10000) {
        throw new IllegalArgumentException("消息内容过长，最大支持10000字符");
    }
}

/**
 * 获取或创建会话
 */
private AgentChatSessionEntity getOrCreateSession(Long sessionId, Long userId) {
    if (sessionId != null) {
        AgentChatSessionEntity session = sessionRepository.selectById(sessionId);
        if (session != null) {
            // 验证会话所有权
            if (!userId.equals(session.getAcsUserId())) {
                throw new RuntimeException("无权限访问此会话");
            }
            return session;
        }
    }
    
    // 创建新会话
    return createNewSession(userId);
}

/**
 * 创建新会话
 */
private AgentChatSessionEntity createNewSession(Long userId) {
    // 实现新会话创建逻辑
    // ...
}
```

### 7.2 性能监控和日志

```java
/**
 * 发送流式开始通知
 */
private void sendStreamingStartNotification(FluxSink<ResultVO<StreamingChatVO>> sink) {
    try {
        StreamingChatVO startVO = StreamingChatVO.builder()
            .eventType(StreamingChatVO.EventType.STREAMING_START)
            .content(Map.of(
                "message", "开始生成响应",
                "timestamp", LocalDateTime.now()
            ))
            .timestamp(LocalDateTime.now())
            .build();
        
        sink.next(ResultVO.success(startVO));
        
    } catch (Exception e) {
        log.error("发送流式开始通知失败", e);
    }
}
```

## 8. 配置和部署考虑

### 8.1 线程池配置

```java
/**
 * 异步执行器配置
 */
@Configuration
public class AgentChatConfig {
    
    @Bean("agentChatExecutor")
    public Executor agentChatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("agent-chat-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

### 8.2 缓存配置

```java
/**
 * 缓存相关配置
 */
public class AgentCacheConfig {
    
    // 会话取消标识缓存时间
    public static final Duration CANCELLATION_CACHE_TTL = Duration.ofMinutes(5);
    
    // 活跃会话清理间隔
    public static final Duration ACTIVE_SESSION_CLEANUP_INTERVAL = Duration.ofMinutes(10);
}
```

---

*此文档详细说明了AgentChatAppServiceImpl的完整重构方案，确保与现有的StreamingChatToolServiceImpl和StreamingChatListener接口完全兼容，并实现了用户要求的所有功能*