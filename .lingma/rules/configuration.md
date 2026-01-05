# 配置文件规范

## 文件组织

- **主配置文件**: `application.yml`，存放通用配置。
- **模块配置文件**: `application-{module}.yml`，例如 `application-ai-model.yml`, `application-task.yml`。
- **环境配置文件**: `application-{profile}.yml`，例如 `application-dev.yml`, `application-prod.yml`。

## 编写风格

- **层级**: 使用 `nexus.*` 作为项目自定义配置的根命名空间。
- **缩进**: 使用2个空格进行缩进。
- **敏感信息**: **严禁**将密码、API Key等敏感信息硬编码在配置文件中。必须使用环境变量或外部配置中心。
    - **推荐方式**: 使用 `${ENV_VARIABLE:defaultValue}` 的形式。

## 示例

```yaml
# application.yml
spring:
  profiles:
    active: dev
    include:
      - ai-model
      - sa-token
      - snowflake
      - task

# application-task.yml
nexus:
  task:
    enabled: ${TASK_ENABLED:true}
    polling:
      interval: PT5S # 5秒
      batchSize: 10
    mapping:
      long-running: ${TASK_LONG_RUNNING_TYPES:DOCUMENT_GENERATION,VIDEO_PROCESSING}

# application-prod.yml
# 生产环境配置
ai:
  providers:
    - name: "openai"
      type: "OPEN_AI"
      config:
        apiKey: "${OPENAI_API_KEY}" # 从环境变量获取
```

description:
globs:
alwaysApply: false
---
