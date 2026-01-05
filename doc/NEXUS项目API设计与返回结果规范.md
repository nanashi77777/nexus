# NEXUS 项目 API 设计与返回结果规范

本文档旨在为NEXUS项目的所有RESTful API提供一套统一的设计与实现标准。遵循此规范有助于提高API的一致性、可预测性和易用性，从而提升前后端开发协作的效率。

## 1. API 设计原则 (RESTful)

所有API都应遵循RESTful设计原则，使用标准的HTTP方法来表达操作意图。

- **`GET`**: 用于**查询**资源。请求应是幂等的，不应对服务器状态产生副作用。
- **`POST`**: 用于**创建**新资源。
- **`PUT` / `PATCH`**: 用于**更新**现有资源。`PUT` 通常用于完整替换，`PATCH` 用于部分更新。
- **`DELETE`**: 用于**删除**现有资源。

## 2. URL 命名规范

- **使用名词复数**: URL路径应使用名词复数来表示资源集合，例如 `/v1/users`, `/v1/resources`。
- **路径小写**: 所有路径均使用小写字母，并用连字符 `-` 分隔单词（kebab-case），例如 `/v1/learning-spaces`。
- **版本控制**: 所有API路径都应包含版本号，例如 `/v1`。这为未来的API升级提供了清晰的路径。
- **唯一资源标识**: 使用路径变量来标识单个资源，例如 `/v1/users/{userId}`。

## 3. 请求规范

- **GET 请求**: 参数应通过URL查询字符串（Query Parameters）传递，例如 `/v1/users?page=1&size=10`。
- **POST/PUT/PATCH 请求**: 数据应通过请求体（Request Body）以 `application/json` 格式传递。
- **参数校验**: 所有API的请求参数（包括路径变量、查询参数和请求体）都**必须**进行校验。推荐使用
  `spring-boot-starter-validation` 提供的注解（如 `@NotNull`, `@Size`）在Controller层或Request DTO中进行。
- **请求DTO**: 所有复杂的请求体都应封装成独立的DTO（Data Transfer Object），并放置在 `web/rest/v1/{domain}/req` 包下。
    - 参考代码: `@/src/main/java/cn/lin037/nexus/web/rest/v1/resource/req/CreateAiSearchResourceReq.java`

## 4. 响应规范

### 4.1. 统一响应结构 `ResultVO`

**所有** API端点，无论成功或失败，都**必须**返回统一的响应结构 `ResultVO<T>`。这为前端提供了一个可预测的、一致的数据处理模型。

- **参考代码**: `@/src/main/java/cn/lin037/nexus/common/model/vo/ResultVO.java`

`ResultVO` 包含以下核心字段：

- `code`: 业务状态码。`200000` 代表成功，非 `200000` 代表具体的业务错误。
- `message`: 对业务状态码的描述。
- `data`: 泛型 `T`，包含实际的业务数据。

### 4.2. 成功响应

- **HTTP状态码**: 统一使用 `200 OK`。
- **响应体**: `ResultVO` 中的`code` 为 `"200000"`，`message` 为 `"请求成功"`，`data` 字段包含具体的响应数据。
    - 查询单个资源: `data` 为单个JSON对象。
    - 查询资源列表: `data` 为JSON数组，或是一个包含分页信息的 `PageResult` 对象。
    - 创建/更新/删除操作: `data` 可以为 `null`，或者返回操作成功的资源的ID或布尔值。

### 4.3. 失败响应

- **HTTP状态码**: 统一使用 `200 OK`。业务层面的失败不应通过 `4xx` 或 `5xx` HTTP状态码来传达，这些应保留给网关、网络或容器层面的错误。
- **响应体**: `ResultVO` 中的`code` 为非零的业务错误码，`message` 为对应的错误描述。`data` 字段通常为 `null`
  ，或在特定场景下可包含辅助排查问题的非敏感信息。
- **异常处理**: 所有业务异常和系统异常都会被
  `@/src/main/java/cn/lin037/nexus/common/exception/GlobalExceptionHandler.java` 统一捕获并转换为标准的 `ResultVO` 失败响应。

### 4.4. 响应DTO (VO)

与请求类似，所有复杂的响应数据都应封装成独立的VO（View Object），并放置在 `web/rest/v1/{domain}/vo`
包下。这有助于将API的视图模型与内部的领域模型解耦。

- 参考代码: `@/src/main/java/cn/lin037/nexus/web/rest/v1/resource/vo/ResourceCreatedVO.java`

---

## 5. 请求处理规范

### 5.1. 数据传输对象（Req）

- **职责**: 用于封装从客户端传递到`Controller`的请求数据。
- **命名**: 以 `Req` 结尾，如 `CreateUserReq`。
- **验证**: 必须在`Req`对象的字段上使用JSR 303（`jakarta.validation.constraints.*`）注解进行输入验证。

### 5.2. 控制器方法签名

- **请求体**: 使用 `@RequestBody` 注解接收POST/PUT请求的数据。
- **强制验证**: 必须在 `@RequestBody` 注解后添加 `@Valid` 注解，以触发自动验证。
- **用户身份**: 控制器层面**禁止**直接接收 `userId` 等身份标识。所有用户身份信息必须通过 `StpUtil.getLoginIdAsLong()` 在
  `AppService`层获取。

### 5.3. 示例

```java
// Req DTO
@Data
public class CreateResourceReq {
    @NotBlank(message = "资源标题不能为空")
    @Length(min = 1, max = 100, message = "标题长度必须在1-100之间")
    private String title;
    
    @NotNull(message = "来源类型不能为空")
    private ResourceSourceTypeEnum sourceType;
}

// Controller方法
@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    
    private final ResourceAppService resourceAppService;
    
    // ...
    
    @PostMapping
    public ResultVO<Long> createResource(@Valid @RequestBody CreateResourceReq request) {
        // userId 在 resourceAppService 内部通过 StpUtil 获取
        Long resourceId = resourceAppService.createResource(request);
        return ResultVO.success(resourceId);
    }
}

// AppService实现
@Service
public class ResourceAppServiceImpl implements ResourceAppService {
    
    public Long createResource(CreateResourceReq request) {
        long userId = StpUtil.getLoginIdAsLong(); // 安全地获取用户ID
        // ...业务逻辑...
    }
}
```

---

## 6. 总结

本规范旨在确保NEXUS项目的API设计具有一致性、可预测性和安全性。所有开发者应严格遵循：

1. **RESTful原则**：使用正确的HTTP方法和URL结构。
2. **统一响应**: 所有接口必须返回 `ResultVO` 格式。
3. **清晰的错误码**: 使用 `CommonResultCodeEnum` 和业务模块的 `ErrorCodeEnum`。
4. **强制验证**: 对所有输入请求使用 `@Valid` 进行验证。
5. **安全的用户身份获取**: 通过 `StpUtil` 在业务层获取用户ID。 