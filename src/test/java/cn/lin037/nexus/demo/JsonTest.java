package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.agent.dto.ToolListItem;
import cn.lin037.nexus.application.agent.enums.ToolItemStatus;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.LearningPlanBatchCreateParams;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.ToolExecutionResponse;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.AiModuleTypeEnum;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.OpenAiParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolSpecificationConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class JsonTest {

    @Test
    void testJson() {
        ToolExecutionResult toolExecutionResult = new ToolExecutionResult();
        toolExecutionResult.setRequest(ToolExecutionRequest.builder()
                .id("1")
                .name("Hello")
                .arguments("{\"name\":\"hello\"}")
                .build());
        toolExecutionResult.setResultText("hello world");
        toolExecutionResult.setStatus(ToolExecutionResult.ToolExecutionStatus.SUCCESS);
        toolExecutionResult.setErrorMessage("error message");
        toolExecutionResult.setExecutionTimeMs(1000L);
        String jsonStr = toolExecutionResult.toJsonStr();

        System.out.println(jsonStr);

        ToolExecutionResult toolExecutionResult1 = Json.fromJson(jsonStr, ToolExecutionResult.class);
        System.out.println(toolExecutionResult1);
    }

    /**
     * 测试OpenAI参数配置，生成完整的工具配置JSON
     * 该方法配置了AgentChat工具执行器中支持的所有工具规范
     * 可直接将输出的JSON配置到数据库中用于AI模型的工具调用
     */
    @Test
    void testOpenAiParamConfig() {
        // 创建OpenAI参数配置对象
        OpenAiParamConfig config = new OpenAiParamConfig();

        // 基础模型配置
        config.setModelName("deepseek-chat"); // 使用GPT-4o-mini模型，性价比较高
        config.setTemperature(1.3); // 温度参数，控制生成文本的随机性，0.7适合对话场景
        config.setMaxTokens(4096); // 最大生成token数，控制响应长度
        config.setTopP(0.9); // Top-p采样，控制生成多样性

        // 配置工具规范列表 - 基于AgentChatToolExecutorImpl中实现的6个核心工具
        List<ToolSpecificationConfig> toolSpecs = new ArrayList<>();

        // 1. 记忆添加工具 - 用于保存用户的重要信息和学习内容
        ToolSpecificationConfig memoryAddTool = new ToolSpecificationConfig();
        memoryAddTool.setName("memory_add");
        memoryAddTool.setDescription("添加新的记忆信息，用于保存用户的重要学习内容、知识点或个人信息");

        // 记忆添加工具的参数结构 - 对应MemoryAddParams
        JsonObjectSchema memoryAddParams = JsonObjectSchema.builder()
                .addStringProperty("title", "记忆标题（可选）")
                .addStringProperty("content", "记忆内容（必填）")
                .addIntegerProperty("importanceScore", "重要性评分（1-10），默认5")
                .required("content")
                .build();
        memoryAddTool.setParameters(memoryAddParams);
        toolSpecs.add(memoryAddTool);

        // 2. 记忆删除工具 - 用于删除不再需要的记忆信息
        ToolSpecificationConfig memoryDeleteTool = new ToolSpecificationConfig();
        memoryDeleteTool.setName("memory_delete");
        memoryDeleteTool.setDescription("删除指定的记忆信息，用于清理过时或错误的记忆内容");

        JsonObjectSchema memoryDeleteParams = JsonObjectSchema.builder()
                .addIntegerProperty("memoryId", "要删除的记忆ID，必须是有效的记忆标识符")
                .required("memoryId")
                .build();
        memoryDeleteTool.setParameters(memoryDeleteParams);
        toolSpecs.add(memoryDeleteTool);

        // 3. 学习计划批量创建工具 - 用于创建多个学习任务
        ToolSpecificationConfig planBatchCreateTool = new ToolSpecificationConfig();
        planBatchCreateTool.setName("learning_plan_batch_create");
        planBatchCreateTool.setDescription("批量创建学习计划，用于为用户制定系统性的学习任务列表");

        // 学习计划项的结构定义 - 对应LearningPlanBatchCreateParams.CreateItem
        JsonObjectSchema planItemSchema = JsonObjectSchema.builder()
                .addStringProperty("title", "规划标题（必填）")
                .addStringProperty("objective", "学习目标（必填）")
                .addStringProperty("difficultyLevel", "难度评估（可选，默认INTERMEDIATE）")
                .required("title", "objective")
                .build();

        JsonObjectSchema planBatchCreateParams = JsonObjectSchema.builder()
                .addProperty("items", JsonArraySchema.builder()
                        .items(planItemSchema)
                        .description("学习计划项列表，包含多个学习任务")
                        .build())
                .required("items")
                .build();
        planBatchCreateTool.setParameters(planBatchCreateParams);
        toolSpecs.add(planBatchCreateTool);

        // 4. 学习计划更新工具 - 用于修改现有学习计划
        ToolSpecificationConfig planUpdateTool = new ToolSpecificationConfig();
        planUpdateTool.setName("learning_plan_update");
        planUpdateTool.setDescription("更新指定的学习计划，可修改标题、目标、难度等级或完成状态");

        // 学习计划更新工具的参数结构 - 对应LearningPlanUpdateParams
        JsonObjectSchema planUpdateParams = JsonObjectSchema.builder()
                .addIntegerProperty("planId", "学习计划ID（必填）")
                .addStringProperty("title", "新的标题（可选）")
                .addStringProperty("objective", "新的学习目标（可选）")
                .addStringProperty("difficultyLevel", "新的难度等级（可选）")
                .addBooleanProperty("completed", "是否标记为已完成（可选）")
                .required("planId")
                .build();
        planUpdateTool.setParameters(planUpdateParams);
        toolSpecs.add(planUpdateTool);

        // 5. 学习计划批量删除工具 - 用于删除多个学习计划
        ToolSpecificationConfig planBatchDeleteTool = new ToolSpecificationConfig();
        planBatchDeleteTool.setName("learning_plan_batch_delete");
        planBatchDeleteTool.setDescription("批量删除学习计划，用于清理不再需要的学习任务");

        JsonObjectSchema planBatchDeleteParams = JsonObjectSchema.builder()
                .addProperty("planIds", JsonArraySchema.builder()
                        .items(JsonIntegerSchema.builder()
                                .description("学习计划ID")
                                .build())
                        .description("要删除的学习计划ID列表")
                        .build())
                .required("planIds")
                .build();
        planBatchDeleteTool.setParameters(planBatchDeleteParams);
        toolSpecs.add(planBatchDeleteTool);

        // 6. 学习计划完成状态更新工具 - 对应LearningPlanCompletionParams
        ToolSpecificationConfig planCompletionTool = new ToolSpecificationConfig();
        planCompletionTool.setName("learning_plan_completion");
        planCompletionTool.setDescription("更新学习计划的完成状态，用于标记任务完成或取消完成");

        JsonObjectSchema planCompletionParams = JsonObjectSchema.builder()
                .addIntegerProperty("planId", "学习计划ID（必填）")
                .addBooleanProperty("completed", "是否已完成（必填）")
                .required("planId", "completed")
                .build();
        planCompletionTool.setParameters(planCompletionParams);
        toolSpecs.add(planCompletionTool);

        // 设置工具规范列表
        config.setToolSpecifications(toolSpecs);

        // 转换为JSON并打印（跳过Duration字段避免序列化问题）
        String configJson = JSONUtil.toJsonStr(config);
        System.out.println("=== OpenAI参数配置JSON（可直接存储到数据库） ===");
        System.out.println(configJson);

        // 格式化输出，便于阅读
        System.out.println("\n=== 格式化的配置信息 ===");
        System.out.println("模型名称: " + config.getModelName());
        System.out.println("温度参数: " + config.getTemperature());
        System.out.println("最大Token数: " + config.getMaxTokens());
        System.out.println("配置的工具数量: " + config.getToolSpecifications().size());

        config.getToolSpecifications().forEach(tool -> {
            System.out.println("- 工具: " + tool.getName() + " | 描述: " + tool.getDescription());
        });
    }

    /**
     * 测试AI服务商配置，基于DeepSeek API文档创建配置
     * 该方法配置了DeepSeek AI服务商的相关参数
     * 可直接将输出的JSON配置存储到数据库中
     */
    @Test
    void testAiProviderConfig() {
        // 创建DeepSeek AI服务商配置对象
        AiProviderConfig config = new AiProviderConfig();

        // 基础配置信息 - 来源于DeepSeek API文档
        config.setApcName("DeepSeek"); // 供应商名称
        config.setApcOfficialUrl("https://api-docs.deepseek.com/zh-cn/"); // 供应商官网地址
        config.setApcChannel(AiModuleTypeEnum.OPEN_AI.name()); // 渠道类型，DeepSeek使用OpenAI兼容格式
        config.setApcBaseUrl("https://api.deepseek.com"); // 基础URL，支持OpenAI兼容的API格式
        config.setApcApiKey("sk-f09501bc344f4baaaf9f2e599d3a3ddc"); // API密钥，需要从DeepSeek官网申请
        config.setApcStatus(GeneralStatusEnum.ACTIVE.getCode()); // 状态：启用
        config.setApcIsDeleted(false); // 逻辑删除标记：未删除
        config.setApcCreateTime(LocalDateTime.now()); // 创建时间
        config.setApcUpdateTime(LocalDateTime.now()); // 更新时间

        // 转换为JSON并打印
        String configJson = JSONUtil.toJsonStr(config);
        System.out.println("=== DeepSeek AI服务商配置JSON（可直接存储到数据库） ===");
        System.out.println(configJson);

        // 格式化输出，便于阅读
        System.out.println("\n=== 格式化的配置信息 ===");
        System.out.println("供应商名称: " + config.getApcName());
        System.out.println("官网地址: " + config.getApcOfficialUrl());
        System.out.println("渠道类型: " + config.getApcChannel());
        System.out.println("基础URL: " + config.getApcBaseUrl());
        System.out.println("API密钥: " + (config.getApcApiKey().length() > 10 ?
                config.getApcApiKey().substring(0, 10) + "..." : config.getApcApiKey()));
        System.out.println("状态: " + (Objects.equals(config.getApcStatus(), GeneralStatusEnum.ACTIVE.getCode()) ? "启用" : "禁用"));

        System.out.println("\n=== DeepSeek API特性说明 ===");
        System.out.println("- 兼容OpenAI API格式，可使用OpenAI SDK访问");
        System.out.println("- 支持deepseek-chat和deepseek-reasoner模型");
        System.out.println("- deepseek-chat对应DeepSeek-V3.1非思考模式");
        System.out.println("- deepseek-reasoner对应DeepSeek-V3.1思考模式");
        System.out.println("- 支持流式和非流式输出");
        System.out.println("- 可选择使用https://api.deepseek.com或https://api.deepseek.com/v1作为base_url");
    }


    @Test
    void testJsonBean() {

        String json = """
                """;
        LearningPlanBatchCreateParams params = JSONUtil.toBean(json, LearningPlanBatchCreateParams.class);
        log.info(params.toString());
    }

    @Test
    void name() {
        ToolExecutionResponse response = ToolExecutionResponse.success("学习计划批量创建成功", List.of(1L));
        log.info(JSONUtil.toJsonStr(response));
    }
}
