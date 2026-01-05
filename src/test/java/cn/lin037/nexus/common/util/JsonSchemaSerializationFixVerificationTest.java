package cn.lin037.nexus.common.util;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.request.json.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonSchema序列化修复验证测试
 * 专门验证用户反馈的两个问题是否已经解决：
 * 1. 基本类型字段缺少type字段
 * 2. 枚举类型错误标记为string类型
 *
 * @author Lin037
 */
@SpringBootTest
class JsonSchemaSerializationFixVerificationTest {

    /**
     * 使用JsonObjectSchemaToStringConverter进行序列化
     *
     * @param element JsonSchemaElement对象
     * @return 包含type字段的JSON字符串
     */
    private String serialize(JsonSchemaElement element) {
        JsonObjectSchemaToStringConverter converter = new JsonObjectSchemaToStringConverter();
        return converter.convert(element, "{}");
    }

    /**
     * 验证问题1：基本类型字段现在都有type字段
     */
    @Test
    void testBasicTypesHaveTypeField() {
        System.out.println("=== 验证问题1：基本类型字段现在都有type字段 ===");

        // 测试整数类型（对应用户提到的planId字段）
        JsonIntegerSchema planIdSchema = JsonIntegerSchema.builder()
                .description("[类型: 整数(Integer)] 学习计划ID")
                .build();
        String planIdJson = serialize(planIdSchema);
        System.out.println("planId字段序列化结果: " + planIdJson);
        assertTrue(planIdJson.contains("\"type\":\"integer\""), "planId应该包含integer类型");

        // 测试字符串类型
        JsonStringSchema titleSchema = JsonStringSchema.builder()
                .description("[类型: 字符串(String)] 规划标题")
                .build();
        String titleJson = serialize(titleSchema);
        System.out.println("title字段序列化结果: " + titleJson);
        assertTrue(titleJson.contains("\"type\":\"string\""), "title应该包含string类型");

        // 测试布尔类型
        JsonBooleanSchema completedSchema = JsonBooleanSchema.builder()
                .description("[类型: 布尔(Boolean)] 是否标记为已完成")
                .build();
        String completedJson = serialize(completedSchema);
        System.out.println("completed字段序列化结果: " + completedJson);
        assertTrue(completedJson.contains("\"type\":\"boolean\""), "completed应该包含boolean类型");

        System.out.println("✅ 问题1已解决：所有基本类型都正确添加了type字段\n");
    }

    /**
     * 验证问题2：枚举类型现在正确标记为enum而不是string
     */
    @Test
    void testEnumTypeIsCorrectlyMarked() {
        System.out.println("=== 验证问题2：枚举类型现在正确标记为enum而不是string ===");

        // 测试难度等级枚举（对应用户提到的difficultyLevel字段）
        JsonEnumSchema difficultyLevelSchema = JsonEnumSchema.builder()
                .description("[类型: 字符串枚举(Enum<String>)] 难度等级评估，可选值: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT")
                .enumValues(List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"))
                .build();
        String difficultyLevelJson = serialize(difficultyLevelSchema);
        System.out.println("difficultyLevel字段序列化结果: " + difficultyLevelJson);

        // 验证类型正确标记为enum
        assertTrue(difficultyLevelJson.contains("\"type\":\"enum\""), "difficultyLevel应该是enum类型");
        assertFalse(difficultyLevelJson.contains("\"type\":\"string\""), "difficultyLevel不应该是string类型");
        assertTrue(difficultyLevelJson.contains("\"enumValues\""), "应该包含enumValues字段");
        assertTrue(difficultyLevelJson.contains("BEGINNER"), "应该包含BEGINNER枚举值");

        System.out.println("✅ 问题2已解决：枚举类型正确标记为enum类型\n");
    }

    /**
     * 验证完整的工具规范对象序列化
     * 模拟用户提供的learning_plan_batch_create工具规范
     */
    @Test
    void testCompleteToolSpecificationSerialization() {
        System.out.println("=== 验证完整的工具规范对象序列化 ===");

        // 创建学习计划项对象Schema
        JsonObjectSchema learningPlanItemSchema = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划项")
                .addProperty("title", JsonStringSchema.builder()
                        .description("[类型: 字符串(String)] 规划标题")
                        .build())
                .addProperty("objective", JsonStringSchema.builder()
                        .description("[类型: 字符串(String)] 学习目标")
                        .build())
                .addProperty("difficultyLevel", JsonEnumSchema.builder()
                        .description("[类型: 字符串枚举(Enum<String>)] 难度等级评估，可选值: BEGINNER | INTERMEDIATE | ADVANCED | EXPERT")
                        .enumValues(List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"))
                        .build())
                .required("title", "objective")
                .additionalProperties(null)
                .build();

        // 创建完整的参数Schema
        JsonObjectSchema parametersSchema = JsonObjectSchema.builder()
                .description("[类型: 对象(Object)] 学习计划批量创建参数")
                .addProperty("items", JsonArraySchema.builder()
                        .description("[类型: 数组(Array)<对象(Object)>] 学习计划列表")
                        .items(learningPlanItemSchema)
                        .build())
                .required("items")
                .additionalProperties(null)
                .build();

        String parametersJson = serialize(parametersSchema);
        System.out.println("完整工具规范参数序列化结果: " + parametersJson);

        // 验证所有类型都正确
        assertTrue(parametersJson.contains("\"type\":\"object\""), "根对象应该是object类型");
        assertTrue(parametersJson.contains("\"type\":\"array\""), "items应该是array类型");
        assertTrue(parametersJson.contains("\"type\":\"string\""), "title和objective应该是string类型");
        assertTrue(parametersJson.contains("\"type\":\"enum\""), "difficultyLevel应该是enum类型");
        assertTrue(parametersJson.contains("\"enumValues\""), "应该包含enumValues字段");
        assertTrue(parametersJson.contains("BEGINNER"), "应该包含枚举值");

        System.out.println("✅ 完整工具规范序列化正确，所有类型字段都已正确添加\n");
    }

    /**
     * 验证序列化和反序列化的一致性
     */
    @Test
    void testSerializationDeserializationConsistency() {
        System.out.println("=== 验证序列化和反序列化的一致性 ===");

        // 创建一个包含各种类型的复杂Schema
        JsonObjectSchema originalSchema = JsonObjectSchema.builder()
                .description("测试Schema")
                .addProperty("id", JsonIntegerSchema.builder().description("ID").build())
                .addProperty("name", JsonStringSchema.builder().description("名称").build())
                .addProperty("active", JsonBooleanSchema.builder().description("是否激活").build())
                .addProperty("status", JsonEnumSchema.builder()
                        .description("状态")
                        .enumValues(List.of("ACTIVE", "INACTIVE"))
                        .build())
                .required("id", "name")
                .build();

        // 序列化
        String jsonStr = serialize(originalSchema);
        System.out.println("序列化结果: " + jsonStr);

        // 反序列化
        JsonSchemaElement deserializedElement = JSONUtil.toBean(jsonStr, JsonSchemaElement.class);
        assertTrue(deserializedElement instanceof JsonObjectSchema, "反序列化应该得到JsonObjectSchema");

        JsonObjectSchema deserializedSchema = (JsonObjectSchema) deserializedElement;
        assertEquals(originalSchema.description(), deserializedSchema.description(), "description应该一致");
        assertEquals(originalSchema.properties().size(), deserializedSchema.properties().size(), "属性数量应该一致");

        // 验证各个属性的类型
        assertTrue(deserializedSchema.properties().get("id") instanceof JsonIntegerSchema, "id应该是JsonIntegerSchema");
        assertTrue(deserializedSchema.properties().get("name") instanceof JsonStringSchema, "name应该是JsonStringSchema");
        assertTrue(deserializedSchema.properties().get("active") instanceof JsonBooleanSchema, "active应该是JsonBooleanSchema");
        assertTrue(deserializedSchema.properties().get("status") instanceof JsonEnumSchema, "status应该是JsonEnumSchema");

        System.out.println("✅ 序列化和反序列化一致性验证通过\n");
    }
}