package cn.lin037.nexus.common.util;

import cn.hutool.core.convert.Converter;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.request.json.*;

import java.util.Map;

/**
 * JsonObjectSchema到字符串转换器
 * 实现Hutool的Converter接口，将JsonObjectSchema转换为JSON字符串
 * 在序列化过程中会添加type字段以便后续反序列化时进行类型识别
 *
 * @author Lin037
 */
public class JsonObjectSchemaToStringConverter implements Converter<String> {

    @Override
    public String convert(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof JsonSchemaElement) {
            try {
                JSONObject jsonObject = convertSchemaElementToJson((JsonSchemaElement) value);
                return JSONUtil.toJsonStr(jsonObject);
            } catch (Exception e) {
                return defaultValue;
            }
        }

        // 如果不是JsonSchemaElement类型，返回默认值
        return defaultValue;
    }

    /**
     * 将JsonSchemaElement转换为JSONObject，并添加type字段
     *
     * @param element JsonSchemaElement对象
     * @return 包含type字段的JSONObject
     */
    private JSONObject convertSchemaElementToJson(JsonSchemaElement element) {
        JSONObject jsonObject = new JSONObject();

        // 添加type字段
        String type = getSchemaType(element);
        if (type != null) {
            jsonObject.set("type", type);
        }

        // 添加description字段
        if (element.description() != null) {
            jsonObject.set("description", element.description());
        }

        // 根据具体类型添加特定字段
        addSpecificFields(jsonObject, element);

        return jsonObject;
    }

    /**
     * 获取JsonSchemaElement对应的类型字符串
     *
     * @param element JsonSchemaElement对象
     * @return 类型字符串
     */
    private String getSchemaType(JsonSchemaElement element) {
        return switch (element) {
            case JsonObjectSchema ignored -> "object";
            case JsonStringSchema ignored -> "string";
            case JsonIntegerSchema ignored -> "integer";
            case JsonNumberSchema ignored -> "number";
            case JsonBooleanSchema ignored -> "boolean";
            case JsonArraySchema ignored -> "array";
            case JsonEnumSchema ignored -> "enum";
            case JsonNullSchema ignored -> "null";
            default -> null;
        };
    }

    /**
     * 根据JsonSchemaElement的具体类型添加特定字段
     *
     * @param json    JSON对象
     * @param element JsonSchemaElement对象
     */
    private void addSpecificFields(JSONObject json, JsonSchemaElement element) {
        switch (element) {
            case JsonObjectSchema objectSchema -> {
                // 处理properties字段，递归转换每个属性
                if (objectSchema.properties() != null && !objectSchema.properties().isEmpty()) {
                    JSONObject properties = new JSONObject();
                    for (Map.Entry<String, JsonSchemaElement> entry : objectSchema.properties().entrySet()) {
                        JSONObject propertyJson = convertSchemaElementToJson(entry.getValue());
                        properties.set(entry.getKey(), propertyJson);
                    }
                    json.set("properties", properties);
                }

                // 处理required字段
                if (objectSchema.required() != null && objectSchema.required().size() > 0) {
                    json.set("required", objectSchema.required());
                }

                // 处理additionalProperties字段
                if (objectSchema.additionalProperties() != null) {
                    json.set("additionalProperties", objectSchema.additionalProperties());
                }

                // 处理definitions字段
                if (objectSchema.definitions() != null && !objectSchema.definitions().isEmpty()) {
                    JSONObject definitions = new JSONObject();
                    for (Map.Entry<String, JsonSchemaElement> entry : objectSchema.definitions().entrySet()) {
                        JSONObject definitionJson = convertSchemaElementToJson(entry.getValue());
                        definitions.set(entry.getKey(), definitionJson);
                    }
                    json.set("definitions", definitions);
                }
            }
            case JsonArraySchema arraySchema -> {
                // 处理items字段，递归转换数组项类型
                if (arraySchema.items() != null) {
                    JSONObject itemsJson = convertSchemaElementToJson(arraySchema.items());
                    json.set("items", itemsJson);
                }
            }
            case JsonEnumSchema enumSchema -> {
                // 处理enumValues字段
                if (enumSchema.enumValues() != null && !enumSchema.enumValues().isEmpty()) {
                    json.set("enumValues", enumSchema.enumValues());
                }
            }
            default -> {
                // 对于其他类型（String, Integer, Number, Boolean, Null），不需要额外字段
            }
        }
    }
}