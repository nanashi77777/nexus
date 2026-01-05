# 异步任务基础设施：开发者使用文档

本文档为开发者提供了在 Nexus 项目中使用异步任务基础设施的详细指南，涵盖了任务提交、自定义逻辑实现和结果解读等内容。

---

## 1. 核心概念

该系统由以下几个核心组件构成：

- **`AsyncTaskManager`**: 应用层的统一入口。您通过它来提交新任务和获取任务状态。
- **`TaskExecutor<P, R>`**: 您必须实现的接口，用于定义特定任务类型的执行逻辑。
    - `P`: **P**arameters，您的参数对象类型。
    - `R`: **R**esult，您的结果对象类型（即审计负载）。
- **`TaskResult<R>`**: 一个封装对象，您的 `execute` 方法必须返回此对象。它同时包含了对用户友好的消息和结构化的详细审计负载。
- **`AsyncTask`**: 代表任务状态、参数和结果的数据库实体。当您查询任务状态时，会与此对象交互。
- **`ErrorDetails`**: 一个标准化的结构，用于在任务失败时，将异常信息序列化为 JSON 对象并存入数据库。

---

## 2. 如何创建和使用新任务

请遵循以下步骤来实现和运行一个新的后台任务。

### 第一步：提交任务

要启动一个任务，请注入 `AsyncTaskManager` 并调用其 `submit` 方法。

- **`taskType`**: 任务的唯一字符串标识符（例如 `"DOCUMENT_GENERATION"`）。此字符串必须与您的 `TaskExecutor` 中返回的标识符完全匹配。
- **`parameters`**: 一个可以被序列化为 JSON 的参数对象。强烈建议使用专门的 DTO 以确保类型安全。
- **`ownerIdentifier`**: 标识任务所有者或发起者的字符串（例如，用户ID）。

**示例代码：**

```java
import org.springframework.stereotype.Service;
import cn.lin037.nexus.infrastructure.common.task.api.AsyncTaskManager;

@Service
public class DocumentService {

    private final AsyncTaskManager asyncTaskManager;

    public DocumentService(AsyncTaskManager asyncTaskManager) {
        this.asyncTaskManager = asyncTaskManager;
    }

    public Long startDocumentGeneration(String templateId, String content) {
        // 1. 创建一个类型安全的参数对象
        DocumentGenerationParams params = new DocumentGenerationParams(templateId, content);

        // 2. 提交任务并获取其ID
        Long taskId = asyncTaskManager.submit(
                "DOCUMENT_GENERATION", // 必须与 TaskExecutor 的类型匹配
                params,
                "user-123" // 发起任务的用户
        );

        return taskId;
    }
}
```

### 第二步：实现任务执行器

创建一个新类来实现 `TaskExecutor<P, R>` 接口。这个类将包含您的核心业务逻辑。

**实现要点：**

1. **`@Component`**: 该类必须是一个 Spring Bean。
2. **泛型**: 指定您的参数（`P`）和结果（`R`）类型。
3. **`getTaskType()`**: 返回唯一的字符串标识符，它将此执行器与 `submit` 调用关联起来。
4. **`getParametersType()` / `getResultType()`**: 返回泛型类型的 `.class` 对象，以辅助反序列化。
5. **`execute()`**: 您的业务逻辑在此实现。
    - 该方法必须返回一个 `TaskResult<R>` 对象。
    - 使用 `TaskResult.success(message, payload)` 来表示任务成功。
    - 您**不需要**为通用异常将逻辑包装在 `try-catch` 块中。调度器会自动处理。

**完整示例 (`DocumentGenerationTaskExecutor.java`):**

```java
package cn.lin037.nexus.application.resource.service;

import cn.lin037.nexus.infrastructure.common.task.executor.TaskContext;
import cn.lin037.nexus.infrastructure.common.task.executor.TaskExecutor;
import cn.lin037.nexus.infrastructure.common.task.model.TaskResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

// 为确保类型安全，定义参数和结果的 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
class DocumentGenerationParams {
    private String templateId;
    private String content;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DocumentGenerationResult {
    private String documentId;
    private String storagePath;
    private int wordCount;
}

@Component
public class DocumentGenerationTaskExecutor implements TaskExecutor<DocumentGenerationParams, DocumentGenerationResult> {

    @Override
    public String getTaskType() {
        return "DOCUMENT_GENERATION";
    }

    @Override
    public Class<DocumentGenerationParams> getParametersType() {
        return DocumentGenerationParams.class;
    }

    @Override
    public Class<DocumentGenerationResult> getResultType() {
        return DocumentGenerationResult.class;
    }

    @Override
    public TaskResult<DocumentGenerationResult> execute(DocumentGenerationParams params, TaskContext context) throws Exception {
        log.info("开始为模板生成文档: {}", params.getTemplateId());

        // --- 您的业务逻辑 ---
        // 1. 模拟一些工作，例如调用外部服务
        Thread.sleep(5000); // 任务耗时5秒

        // 2. 检查是否已请求取消
        if (context.isCancellationRequested()) {
            log.warn("文档生成任务已被请求取消。");
            // 如果需要，可在此处执行清理操作
            throw new InterruptedException("任务被用户取消。");
        }

        // 3. 生成结果负载
        String newDocumentId = UUID.randomUUID().toString();
        DocumentGenerationResult resultPayload = new DocumentGenerationResult(
                newDocumentId,
                "/storage/" + newDocumentId + ".pdf",
                params.getContent().split("\\s+").length
        );
        // --- 业务逻辑结束 ---

        log.info("文档生成成功。文档 ID: {}", newDocumentId);

        // 4. 返回一个标准化的 TaskResult 对象
        return TaskResult.success(
                "文档创建成功，共 " + resultPayload.getWordCount() + " 个单词。",
                resultPayload
        );
    }
}
```

### 第三步：检查任务状态

使用 `submit` 调用返回的 `taskId` 来查询任务的状态。

```java
import java.util.Optional;

// ... 在您的服务中

public void checkTaskStatus(Long taskId) {
    Optional<AsyncTask> taskOptional = asyncTaskManager.getTask(taskId);

    if (taskOptional.isPresent()) {
        AsyncTask task = taskOptional.get();

        System.out.println("任务 ID: " + task.getAtId());
        System.out.println("状态: " + TaskStatusEnum.fromCode(task.getAtStatus()));
        System.out.println("用户消息: " + task.getAtUserFriendlyMessage());

        // 用于审计或调试，检查详细的 JSON
        System.out.println("审计详情 (JSON): " + task.getAtAuditDetails());

        if (task.getAtStatus().equals(TaskStatusEnum.COMPLETED.getCode())) {
            // 您可以将审计详情反序列化回您的结果对象
            DocumentGenerationResult result = JSONUtil.toBean(task.getAtAuditDetails(), DocumentGenerationResult.class);
            System.out.println("生成的文档 ID: " + result.getDocumentId());
        }
    } else {
        System.out.println("未找到 ID 为 " + taskId + " 的任务。");
    }
}
```

---

## 3. 异常处理

系统提供了强大且自动化的异常处理机制。

- **如果您抛出 `InfrastructureException`**: 异常的消息将被用作 `atUserFriendlyMessage`。
- **如果您抛出任何其他 `Exception` (例如 `IOException`, `NullPointerException`)**:
    - `atUserFriendlyMessage` 将被设置为通用的“任务执行期间发生未知错误。”
    - `atAuditDetails` 将包含一个 JSON 对象，其中含有完整的异常类名、消息和堆栈跟踪，非常适合调试。

只有当您需要在任务失败前执行自定义的清理逻辑时，才需要在 `execute` 方法内部使用 `try-catch`。

---

## 4. 高级配置

您可以在 `application-task.yml` 中配置任务的行为。

### 长时间运行的任务

如果一个任务预计需要很长时间（例如，数分钟或数小时），请将其分配给专用的长时任务线程池，以避免阻塞短时任务。

```yaml
nexus:
  task:
    mapping:
      long-running:
        - "VIDEO_PROCESSING"
        - "DATA_REPORT_GENERATION"
        - "DOCUMENT_GENERATION" # 在此处添加您的新任务类型
```

### 任务超时

系统会自动检测并使运行时间过长的任务失败。默认超时时间为30分钟。

```yaml
nexus:
  task:
    timeout:
      enabled: true
      defaultDuration: 30 # 数值
      defaultUnit: MINUTES # 单位 (例如, SECONDS, MINUTES, HOURS)
      checkIntervalSeconds: 60 # 看门狗检查超时的频率（秒）
```