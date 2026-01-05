package cn.lin037.nexus.infrastructure.common.ai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Converter;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.common.util.JsonObjectSchemaConverter;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolSpecificationConfig;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class AiRepositoryTest {

    /**
     * 注册自定义转换器
     */
    @BeforeAll
    public static void setupCustomConverter() {
        ConverterRegistry.getInstance().putCustom(JsonObjectSchema.class, JsonObjectSchemaConverter.class);
//        ConverterRegistry.getInstance().putCustom(ToolSpecificationConfig.class, ToolSpecificationConfigConverter.class);
    }

    @Test
    void testJsonObjectSchemaTransform() {
        System.out.println(" --- 运行优化后的自定义转换器方法 ---");

        // 1. 构建原始对象
        Map<String, JsonSchemaElement> properties = new HashMap<>();
        JsonObjectSchema nestedSchema = JsonObjectSchema.builder()
                .addStringProperty("type")
                .addStringProperty("description")
                .build();
        properties.put("location", nestedSchema);
        JsonStringSchema unitSchema = JsonStringSchema.builder()
                .description("摄氏度")
                .build();
        properties.put("unit", unitSchema);

        JsonObjectSchema originalSchema = JsonObjectSchema.builder()
                .description("获取指定位置的当前天气")
                .addProperties(properties)
                .required("location")
                .additionalProperties(false)
                .definitions(Collections.singletonMap("nested", nestedSchema))
                .build();
        System.out.println("原始对象: " + originalSchema);

        // 2. 序列化对象
        String jsonString = Json.toJson(originalSchema);
//        String jsonString = JSONUtil.toJsonStr(originalSchema);
        System.out.println("序列化后的JSON: " + jsonString);

        // 3. 反序列化回原始对象
//        JsonObjectSchema finalSchema = JSONUtil.toBean(jsonString, JsonObjectSchema.class);
        String finalJsonStr = Json.toJson(originalSchema);
        System.out.println("最终重建的对象: " + Json.toJson(originalSchema));

        // 4. 验证重建的对象是否与原始对象相同
        assertEquals(jsonString, finalJsonStr);
        System.out.println("优化后的自定义转换器方法成功：原始和重建的对象相等。");
    }

    @Test
    void testJsonObjectSchemaTransform2() {
        JsonObjectSchema nestedObject = JsonObjectSchema.builder()
                .addStringProperty("street", "街道地址")
                .addIntegerProperty("zipCode", "邮政编码")
                .build();

        JsonObjectSchema schema = JsonObjectSchema.builder()
                .description("获取指定城市的天气信息")
                .addStringProperty("city", "应返回天气预报的城市")
                .addEnumProperty("temperatureUnit", Arrays.asList("CELSIUS", "FAHRENHEIT"), "温度单位")
                .addIntegerProperty("days", "需要预测的天数")
                .addNumberProperty("latitude", "纬度")
                .addBooleanProperty("includePrecipitation", "是否包含降水信息")
                .addProperty("address", nestedObject)
                .required(Arrays.asList("city", "temperatureUnit", "days"))
                .additionalProperties(false)
                .definitions(Collections.emptyMap())
                .build();

        String firstSchema = Json.toJson(schema);
        log.info("JsonObjectSchemaTransform-finalParametersJsonStr: {}", firstSchema);
        JsonObjectSchema bean = JSONUtil.toBean(firstSchema, JsonObjectSchema.class);
        String finalSchema = Json.toJson(bean);
        log.info("JsonObjectSchemaTransform-finalParametersJsonStr: {}", finalSchema);

        Map<?, ?> map1 = Json.fromJson(finalSchema, Map.class);
        log.info("JsonObjectSchemaTransform-finalParametersMap: {}", map1);
        Map<?, ?> map2 = Json.fromJson(finalSchema, Map.class);
        log.info("JsonObjectSchemaTransform-finalParametersMap: {}", map2);
        assertEquals(map1, map2);
    }

    @Test
    void testToolSpecificationConfigJson() {
        String jsonStr = """
                {
                    "name": "天气查询",\s
                    "description": "获取指定城市的天气信息的工具",\s
                    "parameters": {
                        "required": [
                            "city",\s
                            "temperatureUnit",\s
                            "days"
                        ],\s
                        "properties": {
                            "city": {
                                "description": "应返回天气预报的城市"
                            },\s
                            "days": {
                                "description": "需要预测的天数"
                            },\s
                            "address": {
                                "required": [ ],\s
                                "properties": {
                                    "street": {
                                        "description": "街道地址"
                                    },\s
                                    "zipCode": {
                                        "description": "邮政编码"
                                    }
                                },\s
                                "definitions": { },\s
                                "description": null,\s
                                "additionalProperties": null
                            },\s
                            "latitude": {
                                "description": "纬度"
                            },\s
                            "temperatureUnit": {
                                "enumValues": [
                                    "CELSIUS",\s
                                    "FAHRENHEIT"
                                ],\s
                                "description": "温度单位"
                            },\s
                            "includePrecipitation": {
                                "description": "是否包含降水信息"
                            }
                        },\s
                        "definitions": { },\s
                        "description": "获取指定城市的天气信息",\s
                        "additionalProperties": false
                    }
                }
                """;

        ToolSpecificationConfig toolConfig = JSONUtil.toBean(jsonStr, ToolSpecificationConfig.class);
        log.info("testToolSpecificationConfigJson-toolConfig: {}", toolConfig);
        log.info("testToolSpecificationConfigJson-toolConfig: {}", Json.toJson(toolConfig));
        log.info("testToolSpecificationConfigJson-parameters: {}", toolConfig.getParameters().toString());
        log.info("testToolSpecificationConfigJson-parameters: {}", Json.toJson(toolConfig.getParameters()));
    }

    public static class ToolSpecificationConfigConverter implements Converter<ToolSpecificationConfig> {
        @Override
        public ToolSpecificationConfig convert(Object value, ToolSpecificationConfig defaultValue) {
            if (value instanceof JSONObject json) {
                String name = json.getStr("name");
                String description = json.getStr("description");
//                JsonObjectSchema parameters = json.get("parameters", JsonObjectSchema.class);
                JsonObjectSchema parameters = BeanUtil.toBean(json.getJSONObject("parameters"), JsonObjectSchema.class);
                ToolSpecificationConfig toolSpecificationConfig = new ToolSpecificationConfig();
                toolSpecificationConfig.setName(name);
                toolSpecificationConfig.setDescription(description);
                toolSpecificationConfig.setParameters(parameters);
                return toolSpecificationConfig;
            }
            return null;
        }
    }
}