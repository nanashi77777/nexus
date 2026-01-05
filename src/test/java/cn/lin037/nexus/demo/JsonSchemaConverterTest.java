package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.common.util.JsonObjectSchemaToStringConverter;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * JsonSchema转换器测试
 * 验证JsonObjectSchemaToStringConverter是否正确工作
 *
 * @author Lin037
 */
@SpringBootTest
public class JsonSchemaConverterTest {

    @Test
    void testDirectConverterUsage() {
        System.out.println("=== 测试直接使用JsonObjectSchemaToStringConverter ===");

        // 创建一个包含基本类型和枚举类型的JsonObjectSchema
        JsonObjectSchema schema = JsonObjectSchema.builder()
                .addIntegerProperty("planId", "[类型: 整数(Integer)] 学习计划ID")
                .addEnumProperty("difficultyLevel", List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"), "[类型: 字符串枚举(Enum<String>)] 难度等级")
                .addStringProperty("title", "[类型: 字符串(String)] 计划标题")
                .addBooleanProperty("isActive", "[类型: 布尔(Boolean)] 是否激活")
                .build();

        // 直接使用转换器
        JsonObjectSchemaToStringConverter converter = new JsonObjectSchemaToStringConverter();
        String result = converter.convert(schema, "{}");

        System.out.println("直接转换器结果:");
        System.out.println(result);

        // 验证结果是否包含type字段
        boolean hasIntegerType = result.contains("\"type\":\"integer\"");
        boolean hasEnumType = result.contains("\"type\":\"enum\"");
        boolean hasStringType = result.contains("\"type\":\"string\"");
        boolean hasBooleanType = result.contains("\"type\":\"boolean\"");

        System.out.println("包含integer类型: " + hasIntegerType);
        System.out.println("包含enum类型: " + hasEnumType);
        System.out.println("包含string类型: " + hasStringType);
        System.out.println("包含boolean类型: " + hasBooleanType);
    }

    @Test
    void testHutoolJSONUtilSerialization() {
        System.out.println("\n=== 测试Hutool JSONUtil序列化 ===");

        // 创建相同的schema
        JsonObjectSchema schema = JsonObjectSchema.builder()
                .addIntegerProperty("planId", "[类型: 整数(Integer)] 学习计划ID")
                .addEnumProperty("difficultyLevel", List.of("BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"), "[类型: 字符串枚举(Enum<String>)] 难度等级")
                .addStringProperty("title", "[类型: 字符串(String)] 计划标题")
                .addBooleanProperty("isActive", "[类型: 布尔(Boolean)] 是否激活")
                .build();

        // 使用Hutool的JSONUtil
        String result = JSONUtil.toJsonStr(schema);

        System.out.println("Hutool JSONUtil结果:");
        System.out.println(result);

        // 验证结果是否包含type字段
        boolean hasIntegerType = result.contains("\"type\":\"integer\"");
        boolean hasEnumType = result.contains("\"type\":\"enum\"");
        boolean hasStringType = result.contains("\"type\":\"string\"");
        boolean hasBooleanType = result.contains("\"type\":\"boolean\"");

        System.out.println("包含integer类型: " + hasIntegerType);
        System.out.println("包含enum类型: " + hasEnumType);
        System.out.println("包含string类型: " + hasStringType);
        System.out.println("包含boolean类型: " + hasBooleanType);
    }
}