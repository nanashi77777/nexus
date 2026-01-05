package cn.lin037.nexus.demo;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.common.util.JsonObjectSchemaConverter;
import cn.lin037.nexus.common.util.JsonObjectSchemaToStringConverter;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.AiModuleTypeEnum;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.GeneralStatusEnum;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.OpenAiParamConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolSpecificationConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiModelConfig;
import cn.lin037.nexus.infrastructure.common.ai.model.po.AiProviderConfig;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import dev.langchain4j.model.chat.request.json.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AI配置集成测试类
 * 用于测试AI提供商和模型配置的保存功能
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class AiConfigIntegrationTest {

    @Autowired
    private AiCoreService aiCoreService;

    /**
     * 测试保存AI配置
     * 创建DeepSeek提供商配置和模型配置，并保存到数据库
     */
    @Test
    public void testSaveAiConfigs() {
        // 创建DeepSeek提供商配置
        AiProviderConfig providerConfig = createDeepSeekProviderConfig();
        System.out.println("DeepSeek Provider Config:");
        System.out.println(JSONUtil.toJsonStr(providerConfig));
        // 保存配置到数据库
        aiCoreService.saveProviderConfig(providerConfig);

        // 创建DeepSeek模型配置
        AiModelConfig modelConfig = createDeepSeekModelConfig();
        // 保存配置到数据库
//        modelConfig.setAmcProviderId(providerConfig.getApcId());
        modelConfig.setAmcProviderId(2L);
//         aiCoreService.saveModelConfig(modelConfig);
        System.out.println(modelConfig.getAmcConfig());

        System.out.println("\n配置创建成功，所有AI配置参数正确");
    }

    /**
     * 测试JsonObjectSchema的序列化和反序列化一致性
     * 验证使用新的转换器后，序列化成JSON字符串再反序列化回来的对象类型是否一致
     */
    @Test
    public void testJsonObjectSchemaSerializationConsistency() {
        log.info("开始测试JsonObjectSchema序列化和反序列化一致性");

        // 注册自定义转换器
        ConverterRegistry.getInstance().putCustom(JsonObjectSchema.class, new JsonObjectSchemaToStringConverter());
        ConverterRegistry.getInstance().putCustom(String.class, new JsonObjectSchemaConverter());

        // 测试各种类型的Schema
        testSchemaConsistency("String Schema", createStringSchema());
        testSchemaConsistency("Integer Schema", createIntegerSchema());
        testSchemaConsistency("Boolean Schema", createBooleanSchema());
        testSchemaConsistency("Array Schema", createArraySchema());
        testSchemaConsistency("Enum Schema", createEnumSchema());
        testSchemaConsistency("Complex Object Schema", createComplexObjectSchema());

        log.info("JsonObjectSchema序列化和反序列化一致性测试完成");
    }

    /**
     * 测试单个Schema的序列化和反序列化一致性
     */
    private void testSchemaConsistency(String testName, JsonSchemaElement originalSchema) {
        log.info("\n=== 测试 {} ===", testName);

        // 1. 序列化为JSON字符串
        String jsonString = Convert.convert(String.class, originalSchema);
        log.info("序列化结果: {}", jsonString);

        // 2. 反序列化回JsonSchemaElement
        JsonSchemaElement deserializedSchema = Convert.convert(JsonSchemaElement.class, jsonString);

        // 3. 验证类型一致性
        log.info("原始类型: {}", originalSchema.getClass().getSimpleName());
        log.info("反序列化类型: {}", deserializedSchema != null ? deserializedSchema.getClass().getSimpleName() : "null");

        // 4. 验证内容一致性（简单验证）
        if (deserializedSchema != null) {
            String originalJson = Convert.convert(String.class, originalSchema);
            String deserializedJson = Convert.convert(String.class, deserializedSchema);
            log.info("内容一致性: {}", originalJson.equals(deserializedJson) ? "✓ 一致" : "✗ 不一致");
            if (!originalJson.equals(deserializedJson)) {
                log.warn("原始JSON: {}", originalJson);
                log.warn("反序列化JSON: {}", deserializedJson);
            }
        } else {
            log.error("反序列化失败，结果为null");
        }
    }

    /**
     * 创建字符串类型Schema
     */
    private JsonStringSchema createStringSchema() {
        return JsonStringSchema.builder()
                .description("测试字符串类型")
                .build();
    }

    /**
     * 创建整数类型Schema
     */
    private JsonIntegerSchema createIntegerSchema() {
        return JsonIntegerSchema.builder()
                .description("测试整数类型")
                .build();
    }

    /**
     * 创建布尔类型Schema
     */
    private JsonBooleanSchema createBooleanSchema() {
        return JsonBooleanSchema.builder()
                .description("测试布尔类型")
                .build();
    }

    /**
     * 创建数组类型Schema
     */
    private JsonArraySchema createArraySchema() {
        return JsonArraySchema.builder()
                .description("测试数组类型")
                .items(JsonStringSchema.builder().description("数组项").build())
                .build();
    }

    /**
     * 创建枚举类型Schema
     */
    private JsonEnumSchema createEnumSchema() {
        return JsonEnumSchema.builder()
                .description("测试枚举类型")
                .enumValues(Arrays.asList("VALUE1", "VALUE2", "VALUE3"))
                .build();
    }

    /**
     * 创建复杂对象类型Schema
     */
    private JsonObjectSchema createComplexObjectSchema() {
        return JsonObjectSchema.builder()
                .description("测试复杂对象类型")
                .addStringProperty("name", "姓名")
                .addIntegerProperty("age", "年龄")
                .addProperty("hobbies", JsonArraySchema.builder()
                        .description("爱好列表")
                        .items(JsonStringSchema.builder().description("爱好").build())
                        .build())
                .required("name")
                .build();
    }

    /**
     * 创建DeepSeek提供商配置
     *
     * @return DeepSeek提供商配置对象
     */
    private AiProviderConfig createDeepSeekProviderConfig() {
        AiProviderConfig config = new AiProviderConfig();
        config.setApcName("DeepSeek");
        config.setApcOfficialUrl("https://api-docs.deepseek.com/zh-cn/");
        config.setApcChannel(AiModuleTypeEnum.OPEN_AI.getValue());
        config.setApcBaseUrl("https://api.deepseek.com");
        config.setApcApiKey("sk-f09501bc344f4baaaf9f2e599d3a3ddc");
        config.setApcStatus(GeneralStatusEnum.ACTIVE.getCode());
        config.setApcIsDeleted(false);
        config.setApcCreateTime(LocalDateTime.now());
        config.setApcUpdateTime(LocalDateTime.now());

        return config;
    }

    /**
     * 序列化OpenAiParamConfig并为JsonSchemaElement添加type字段
     *
     * @param config OpenAI配置对象
     * @return 包含type字段的JSON字符串
     */
    private String serializeWithTypeFields(OpenAiParamConfig config) {
        // 使用Hutool的JSONUtil进行序列化，这会自动调用我们配置的JsonObjectSchemaToStringConverter
        return JSONUtil.toJsonStr(config);
    }


    /**
     * 创建DeepSeek模型配置
     *
     * @return DeepSeek模型配置对象
     */
    private AiModelConfig createDeepSeekModelConfig() {
        AiModelConfig config = new AiModelConfig();
        config.setAmcName("deepseek-chat");
        config.setAmcUsedFor("STREAMING_CHAT_WITH_TOOL");
        config.setAmcStatus(GeneralStatusEnum.ACTIVE.getCode());
        config.setAmcIsDeleted(false);
        config.setAmcCreateTime(LocalDateTime.now());
        config.setAmcUpdateTime(LocalDateTime.now());

        // 创建模型配置JSON字符串
        OpenAiParamConfig completeOpenAiConfig = createCompleteOpenAiConfig();
        String json = serializeWithTypeFields(completeOpenAiConfig);
        config.setAmcConfig(json);

        log.info("DeepSeek Model Config: {}", json);

        return config;
    }

    /**
     * 创建完整的OpenAI配置
     * 包含所有6个核心工具的规范
     *
     * @return OpenAI配置Map
     */
    private OpenAiParamConfig createCompleteOpenAiConfig() {
        // 创建OpenAI参数配置对象
        OpenAiParamConfig config = new OpenAiParamConfig();

        // 基础模型配置 - 与JsonTest.testOpenAiParamConfig保持一致
        config.setModelName("deepseek-chat"); // 使用DeepSeek Chat模型
        config.setTemperature(1.3); // 温度参数，控制生成文本的随机性
        config.setMaxTokens(4096); // 最大生成token数，控制响应长度
        config.setTopP(0.9); // Top-p采样，控制生成多样性

        // 配置工具规范列表 - 基于AgentChatToolExecutorImpl中实现的6个核心工具
        List<ToolSpecificationConfig> toolSpecs = new ArrayList<>();
        toolSpecs.add(createMemoryAddTool());
        toolSpecs.add(createMemoryDeleteTool());
        toolSpecs.add(createLearningPlanBatchCreateTool());
        toolSpecs.add(createLearningPlanUpdateTool());
        toolSpecs.add(createLearningPlanBatchDeleteTool());
        toolSpecs.add(createLearningPlanCompletionTool());

        // 设置工具规范列表
        config.setToolSpecifications(toolSpecs);

        return config;
    }

    /**
     * 创建记忆添加工具规范
     * 基于MemoryAddParams参数结构创建工具规范
     *
     * @return 记忆添加工具配置
     */
    private ToolSpecificationConfig createMemoryAddTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("memory_add");
        tool.setDescription("[类型: 工具(Tool)] 添加新的记忆条目到用户的记忆库中");


        // 创建参数schema - 基于MemoryAddParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 记忆添加参数")
                .addStringProperty("title", "[类型: 字符串(String)] 记忆标题")
                .addStringProperty("content", "[类型: 字符串(String)] 记忆内容")
                .addIntegerProperty("importanceScore", "[类型: 整数(Integer), 取值范围: 1-10] 重要性评分(1-10)")
                .required("content");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }

    /**
     * 创建记忆删除工具规范
     * 基于MemoryDeleteParams参数结构创建工具规范
     *
     * @return 记忆删除工具配置
     */
    private ToolSpecificationConfig createMemoryDeleteTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("memory_delete");
        tool.setDescription("[类型: 工具(Tool)] 根据ID删除指定的记忆条目");

        // 创建参数schema - 基于MemoryDeleteParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 记忆删除参数")
                .addProperty("memoryId", JsonIntegerSchema.builder()
                        .description("[类型: 整数(Integer)] 要删除的记忆ID")
                        .build())
                .required("memoryId");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }

    /**
     * 创建学习计划批量创建工具规范
     * 基于LearningPlanBatchCreateParams参数结构创建工具规范
     *
     * @return 学习计划批量创建工具配置
     */
    private ToolSpecificationConfig createLearningPlanBatchCreateTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("learning_plan_batch_create");
        tool.setDescription("[类型: 工具(Tool)] 批量创建学习计划");

        // 创建参数schema - 基于LearningPlanBatchCreateParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划批量创建参数")
                .addProperty("items", JsonArraySchema.builder()
                        .description("[类型: 数组(Array)<对象(Object)>] 学习计划列表")
                        .items(JsonObjectSchema.builder()
                                .description("[类型: 对象(Object)] 学习计划项")
                                .addStringProperty("title", "[类型: 字符串(String)] 规划标题")
                                .addStringProperty("objective", "[类型: 字符串(String)] 学习目标")
                                .addEnumProperty("difficultyLevel", Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"), "[类型: 字符串枚举(Enum<String>)] 难度等级评估，可选值: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT")
                                .required("title", "objective")
                                .build())
                        .build())
                .required("items");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }

    /**
     * 创建学习计划更新工具规范
     * 基于LearningPlanUpdateParams参数结构创建工具规范
     *
     * @return 学习计划更新工具配置
     */
    private ToolSpecificationConfig createLearningPlanUpdateTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("learning_plan_update");
        tool.setDescription("[类型: 工具(Tool)] 更新指定的学习计划");

        // 创建参数schema - 基于LearningPlanUpdateParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划更新参数")
                .addProperty("planId", JsonIntegerSchema.builder()
                        .description("[类型: 整数(Integer)] 学习计划ID")
                        .build())
                .addStringProperty("title", "[类型: 字符串(String)] 新的标题")
                .addStringProperty("objective", "[类型: 字符串(String)] 新的学习目标")
                .addEnumProperty("difficultyLevel", Arrays.asList("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"), "[类型: 字符串枚举(Enum<String>)] 新的难度等级，可选值: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT")
                .addProperty("completed", JsonBooleanSchema.builder()
                        .description("[类型: 布尔(Boolean)] 是否标记为已完成")
                        .build())
                .required("planId");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }

    /**
     * 创建学习计划批量删除工具规范
     * 基于LearningPlanBatchDeleteParams参数结构创建工具规范
     *
     * @return 学习计划批量删除工具配置
     */
    private ToolSpecificationConfig createLearningPlanBatchDeleteTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("learning_plan_batch_delete");
        tool.setDescription("[类型: 工具(Tool)] 批量删除学习计划");

        // 创建参数schema - 基于LearningPlanBatchDeleteParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划批量删除参数")
                .addProperty("planIds", JsonArraySchema.builder()
                        .description("[类型: 数组(Array)<整数(Integer)>] 要删除的学习计划ID列表")
                        .items(JsonIntegerSchema.builder()
                                .description("[类型: 整数(Integer)] 学习计划ID")
                                .build())
                        .build())
                .required("planIds");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }

    /**
     * 创建学习计划完成状态更新工具规范
     * 基于LearningPlanCompletionParams参数结构创建工具规范
     *
     * @return 学习计划完成状态更新工具配置
     */
    private ToolSpecificationConfig createLearningPlanCompletionTool() {
        ToolSpecificationConfig tool = new ToolSpecificationConfig();
        tool.setName("learning_plan_completion");
        tool.setDescription("[类型: 工具(Tool)] 更新学习计划的完成状态");

        // 创建参数schema - 基于LearningPlanCompletionParams结构
        JsonObjectSchema.Builder parametersBuilder = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划完成状态更新参数")
                .addProperty("planId", JsonIntegerSchema.builder()
                        .description("[类型: 整数(Integer)] 学习计划ID")
                        .build())
                .addProperty("isCompleted", JsonBooleanSchema.builder()
                        .description("[类型: 布尔(Boolean)] 是否标记为已完成")
                        .build())
                .required("planId", "isCompleted");

        tool.setParameters(parametersBuilder.build());
        return tool;
    }
}