package cn.lin037.nexus.common.util;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.request.json.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonSchemaElement转换器测试类
 * 测试JsonSchemaElement的序列化和反序列化功能
 *
 * @author Lin037
 */
@SpringBootTest
class JsonSchemaElementConverterTest {

    /**
     * 使用自定义序列化方法，确保包含type字段
     *
     * @param element JsonSchemaElement对象
     * @return 包含type字段的JSON字符串
     */
    private String serializeWithTypeFields(JsonSchemaElement element) {
        // 直接使用JsonObjectSchemaToStringConverter进行序列化
        JsonObjectSchemaToStringConverter converter = new JsonObjectSchemaToStringConverter();
        return converter.convert(element, "{}");
    }


    /**
     * 测试JsonObjectSchema的序列化和反序列化
     */
    @Test
    void testJsonObjectSchemaConversion() {
        // 创建一个复杂的JsonObjectSchema
        JsonObjectSchema originalSchema = JsonObjectSchema.builder()
                .description("测试对象Schema")
                .addProperty("name", JsonStringSchema.builder().description("姓名").build())
                .addProperty("age", JsonIntegerSchema.builder().description("年龄").build())
                .addProperty("isActive", JsonBooleanSchema.builder().description("是否激活").build())
                .addProperty("tags", JsonArraySchema.builder()
                        .description("标签列表")
                        .items(JsonStringSchema.builder().description("标签").build())
                        .build())
                .addProperty("status", JsonEnumSchema.builder()
                        .description("状态")
                        .enumValues(List.of("ACTIVE", "INACTIVE", "PENDING"))
                        .build())
                .required("name", "age")
                .additionalProperties(false)
                .build();

        // 序列化为JSON字符串
        String jsonStr = serializeWithTypeFields(originalSchema);
        System.out.println("序列化结果: " + jsonStr);

        // 验证JSON字符串包含type字段
        assertTrue(jsonStr.contains("\"type\":\"object\""), "序列化结果应包含type字段");
        assertTrue(jsonStr.contains("\"type\":\"string\""), "嵌套的string类型应包含type字段");
        assertTrue(jsonStr.contains("\"type\":\"integer\""), "嵌套的integer类型应包含type字段");
        assertTrue(jsonStr.contains("\"type\":\"boolean\""), "嵌套的boolean类型应包含type字段");
        assertTrue(jsonStr.contains("\"type\":\"array\""), "嵌套的array类型应包含type字段");

        // 反序列化为JsonObjectSchema
        JsonObjectSchema deserializedSchema = JSONUtil.toBean(jsonStr, JsonObjectSchema.class);

        // 验证反序列化结果
        assertNotNull(deserializedSchema, "反序列化结果不应为null");
        assertEquals(originalSchema.description(), deserializedSchema.description(), "description应该相等");
        assertEquals(originalSchema.required(), deserializedSchema.required(), "required字段应该相等");
        assertEquals(originalSchema.additionalProperties(), deserializedSchema.additionalProperties(), "additionalProperties应该相等");

        // 验证properties字段
        assertNotNull(deserializedSchema.properties(), "properties不应为null");
        assertEquals(originalSchema.properties().size(), deserializedSchema.properties().size(), "properties数量应该相等");

        // 验证具体的property类型
        assertTrue(deserializedSchema.properties().get("name") instanceof JsonStringSchema, "name应该是JsonStringSchema类型");
        assertTrue(deserializedSchema.properties().get("age") instanceof JsonIntegerSchema, "age应该是JsonIntegerSchema类型");
        assertTrue(deserializedSchema.properties().get("isActive") instanceof JsonBooleanSchema, "isActive应该是JsonBooleanSchema类型");
        assertTrue(deserializedSchema.properties().get("tags") instanceof JsonArraySchema, "tags应该是JsonArraySchema类型");
        assertTrue(deserializedSchema.properties().get("status") instanceof JsonEnumSchema, "status应该是JsonEnumSchema类型");

        // 验证数组项类型
        JsonArraySchema tagsSchema = (JsonArraySchema) deserializedSchema.properties().get("tags");
        assertTrue(tagsSchema.items() instanceof JsonStringSchema, "数组项应该是JsonStringSchema类型");

        // 验证枚举值
        JsonEnumSchema statusSchema = (JsonEnumSchema) deserializedSchema.properties().get("status");
        assertEquals(3, statusSchema.enumValues().size(), "枚举值数量应该为3");
        assertTrue(statusSchema.enumValues().contains("ACTIVE"), "应包含ACTIVE枚举值");
    }

    /**
     * 测试JsonArraySchema的序列化和反序列化
     */
    @Test
    void testJsonArraySchemaConversion() {
        // 创建JsonArraySchema
        JsonArraySchema originalSchema = JsonArraySchema.builder()
                .description("字符串数组")
                .items(JsonStringSchema.builder().description("数组项").build())
                .build();

        // 序列化
        String jsonStr = serializeWithTypeFields(originalSchema);
        System.out.println("数组Schema序列化结果: " + jsonStr);

        // 验证包含type字段
        assertTrue(jsonStr.contains("\"type\":\"array\""), "应包含array类型");
        assertTrue(jsonStr.contains("\"type\":\"string\""), "items应包含string类型");

        // 反序列化
        JsonSchemaElement deserializedElement = JSONUtil.toBean(jsonStr, JsonSchemaElement.class);

        // 验证类型
        assertTrue(deserializedElement instanceof JsonArraySchema, "应该是JsonArraySchema类型");
        JsonArraySchema deserializedSchema = (JsonArraySchema) deserializedElement;

        // 验证属性
        assertEquals(originalSchema.description(), deserializedSchema.description(), "description应该相等");
        assertTrue(deserializedSchema.items() instanceof JsonStringSchema, "items应该是JsonStringSchema类型");
    }

    /**
     * 测试JsonEnumSchema的序列化和反序列化
     */
    @Test
    void testJsonEnumSchemaConversion() {
        // 创建JsonEnumSchema
        JsonEnumSchema originalSchema = JsonEnumSchema.builder()
                .description("状态枚举")
                .enumValues(List.of("DRAFT", "PUBLISHED", "ARCHIVED"))
                .build();

        // 序列化
        String jsonStr = serializeWithTypeFields(originalSchema);
        System.out.println("枚举Schema序列化结果: " + jsonStr);

        // 验证包含type字段和enumValues
        assertTrue(jsonStr.contains("\"type\":\"enum\""), "枚举应该是enum类型");
        assertTrue(jsonStr.contains("\"enumValues\""), "应包含enumValues字段");

        // 反序列化
        JsonSchemaElement deserializedElement = JSONUtil.toBean(jsonStr, JsonSchemaElement.class);

        // 验证类型
        assertTrue(deserializedElement instanceof JsonEnumSchema, "应该是JsonEnumSchema类型");
        JsonEnumSchema deserializedSchema = (JsonEnumSchema) deserializedElement;

        // 验证属性
        assertEquals(originalSchema.description(), deserializedSchema.description(), "description应该相等");
        assertEquals(originalSchema.enumValues().size(), deserializedSchema.enumValues().size(), "enumValues数量应该相等");
        assertTrue(deserializedSchema.enumValues().contains("DRAFT"), "应包含DRAFT枚举值");
    }

    /**
     * 测试基本类型的序列化和反序列化
     */
    @Test
    void testBasicTypesConversion() {
        // 测试JsonStringSchema
        JsonStringSchema stringSchema = JsonStringSchema.builder().description("字符串类型").build();
        String stringJson = serializeWithTypeFields(stringSchema);
        assertTrue(stringJson.contains("\"type\":\"string\""), "应包含string类型");
        JsonSchemaElement deserializedString = JSONUtil.toBean(stringJson, JsonSchemaElement.class);
        assertTrue(deserializedString instanceof JsonStringSchema, "应该是JsonStringSchema类型");

        // 测试JsonIntegerSchema
        JsonIntegerSchema integerSchema = JsonIntegerSchema.builder().description("整数类型").build();
        String integerJson = serializeWithTypeFields(integerSchema);
        assertTrue(integerJson.contains("\"type\":\"integer\""), "应包含integer类型");
        JsonSchemaElement deserializedInteger = JSONUtil.toBean(integerJson, JsonSchemaElement.class);
        assertTrue(deserializedInteger instanceof JsonIntegerSchema, "应该是JsonIntegerSchema类型");

        // 测试JsonBooleanSchema
        JsonBooleanSchema booleanSchema = JsonBooleanSchema.builder().description("布尔类型").build();
        String booleanJson = serializeWithTypeFields(booleanSchema);
        assertTrue(booleanJson.contains("\"type\":\"boolean\""), "应包含boolean类型");
        JsonSchemaElement deserializedBoolean = JSONUtil.toBean(booleanJson, JsonSchemaElement.class);
        assertTrue(deserializedBoolean instanceof JsonBooleanSchema, "应该是JsonBooleanSchema类型");

        // 测试JsonNumberSchema
        JsonNumberSchema numberSchema = JsonNumberSchema.builder().description("数字类型").build();
        String numberJson = serializeWithTypeFields(numberSchema);
        assertTrue(numberJson.contains("\"type\":\"number\""), "应包含number类型");
        JsonSchemaElement deserializedNumber = JSONUtil.toBean(numberJson, JsonSchemaElement.class);
        assertTrue(deserializedNumber instanceof JsonNumberSchema, "应该是JsonNumberSchema类型");
    }
}