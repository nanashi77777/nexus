package cn.lin037.nexus.common.util;

import cn.hutool.core.convert.Converter;
import cn.hutool.json.JSONObject;
import cn.lin037.nexus.common.enums.JsonSchemaTypeEnum;
import dev.langchain4j.model.chat.request.json.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JsonSchemaElement转换器
 * 实现Hutool的Converter接口，将JSON对象转换为JsonSchemaElement
 * 支持所有JsonSchemaElement的子类型转换
 *
 * @author Lin037
 */
public class JsonSchemaElementConverter implements Converter<JsonSchemaElement> {

    @Override
    public JsonSchemaElement convert(Object value, JsonSchemaElement defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        return convertJsonToSchemaElement(value);
    }

    /**
     * 将JSON对象转换为JsonSchemaElement
     *
     * @param value JSON对象或Map
     * @return JsonSchemaElement实例
     */
    private JsonSchemaElement convertJsonToSchemaElement(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case JSONObject json -> {
                return convertFromJsonObject(json);
            }
            case Map<?, ?> map -> {
                return convertFromMap(map);
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * 从JSONObject转换为JsonSchemaElement
     *
     * @param json JSONObject对象
     * @return JsonSchemaElement实例
     */
    private JsonSchemaElement convertFromJsonObject(JSONObject json) {
        // 使用枚举类型进行类型判断，提高类型安全性
        String typeStr = json.getStr("type");
        JsonSchemaTypeEnum schemaType = JsonSchemaTypeEnum.fromValue(typeStr);

        // 优先处理 Enum 类型
        if (json.containsKey("enumValues") || JsonSchemaTypeEnum.ENUM.equals(schemaType)) {
            JsonEnumSchema.Builder builder = JsonEnumSchema.builder();
            String description = json.getStr("description");
            if (description != null) {
                builder.description(description);
            }
            List<String> enumValues = json.getJSONArray("enumValues").toList(String.class);
            builder.enumValues(enumValues);
            return builder.build();
        }

        // 根据枚举类型进行精确匹配
        if (schemaType != null) {
            return switch (schemaType) {
                case OBJECT -> convertToObjectSchema(json);
                case STRING -> {
                    JsonStringSchema.Builder builder = JsonStringSchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    yield builder.build();
                }
                case INTEGER -> {
                    JsonIntegerSchema.Builder builder = JsonIntegerSchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    yield builder.build();
                }
                case NUMBER -> {
                    JsonNumberSchema.Builder builder = JsonNumberSchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    yield builder.build();
                }
                case BOOLEAN -> {
                    JsonBooleanSchema.Builder builder = JsonBooleanSchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    yield builder.build();
                }
                case ARRAY -> {
                    JsonArraySchema.Builder builder = JsonArraySchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    // 处理数组项类型
                    Object itemsObj = json.get("items");
                    if (itemsObj != null) {
                        JsonSchemaElement items = convertJsonToSchemaElement(itemsObj);
                        if (items != null) {
                            builder.items(items);
                        }
                    }
                    yield builder.build();
                }
                case NULL -> new JsonNullSchema();
                case ENUM -> {
                    // 已在上面处理
                    JsonEnumSchema.Builder builder = JsonEnumSchema.builder();
                    String description = json.getStr("description");
                    if (description != null) {
                        builder.description(description);
                    }
                    List<String> enumValues = json.getJSONArray("enumValues").toList(String.class);
                    builder.enumValues(enumValues);
                    yield builder.build();
                }
            };
        }

        // 如果没有type字段，尝试根据结构特征推断类型
        if (json.containsKey("properties") || json.containsKey("required") ||
                json.containsKey("additionalProperties") || json.containsKey("definitions")) {
            // 作为 JsonObjectSchema 处理
            return convertToObjectSchema(json);
        }

        // 如果有 description 但没有其他标识，默认为 JsonStringSchema
        if (json.containsKey("description")) {
            return JsonStringSchema.builder()
                    .description(json.getStr("description"))
                    .build();
        }

        // 默认作为 JsonObjectSchema 处理
        return convertToObjectSchema(json);
    }

    /**
     * 从Map转换为JsonSchemaElement
     *
     * @param map Map对象
     * @return JsonSchemaElement实例
     */
    private JsonSchemaElement convertFromMap(Map<?, ?> map) {
        // 优先处理 Enum 类型
        if (map.containsKey("enumValues")) {
            JsonEnumSchema.Builder builder = JsonEnumSchema.builder();
            String description = (String) map.get("description");
            if (description != null) {
                builder.description(description);
            }
            @SuppressWarnings("unchecked")
            List<String> enumValues = (List<String>) map.get("enumValues");
            builder.enumValues(enumValues);
            return builder.build();
        }

        // 使用枚举类型进行类型判断
        if (map.containsKey("type")) {
            String typeStr = (String) map.get("type");
            JsonSchemaTypeEnum schemaType = JsonSchemaTypeEnum.fromValue(typeStr);

            if (schemaType != null) {
                return switch (schemaType) {
                    case OBJECT -> convertToObjectSchemaFromMap(map);
                    case STRING -> {
                        JsonStringSchema.Builder builder = JsonStringSchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        yield builder.build();
                    }
                    case INTEGER -> {
                        JsonIntegerSchema.Builder builder = JsonIntegerSchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        yield builder.build();
                    }
                    case NUMBER -> {
                        JsonNumberSchema.Builder builder = JsonNumberSchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        yield builder.build();
                    }
                    case BOOLEAN -> {
                        JsonBooleanSchema.Builder builder = JsonBooleanSchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        yield builder.build();
                    }
                    case ARRAY -> {
                        JsonArraySchema.Builder builder = JsonArraySchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        // 处理数组项类型
                        Object itemsObj = map.get("items");
                        if (itemsObj != null) {
                            JsonSchemaElement items = convertJsonToSchemaElement(itemsObj);
                            if (items != null) {
                                builder.items(items);
                            }
                        }
                        yield builder.build();
                    }
                    case NULL -> new JsonNullSchema();
                    case ENUM -> {
                        JsonEnumSchema.Builder builder = JsonEnumSchema.builder();
                        String description = (String) map.get("description");
                        if (description != null) {
                            builder.description(description);
                        }
                        @SuppressWarnings("unchecked")
                        List<String> enumValues = (List<String>) map.get("enumValues");
                        if (enumValues != null) {
                            builder.enumValues(enumValues);
                        }
                        yield builder.build();
                    }
                };
            }
        }

        // 检查是否是对象类型
        if (map.containsKey("properties") || map.containsKey("required") ||
                map.containsKey("additionalProperties") || map.containsKey("definitions")) {
            // 作为 JsonObjectSchema 处理
            return convertToObjectSchemaFromMap(map);
        }

        // 如果没有 "type" 字段，尝试猜测类型
        if (map.containsKey("description")) {
            // 创建 JsonStringSchema
            return JsonStringSchema.builder()
                    .description((String) map.get("description"))
                    .build();
        }

        // 默认作为 JsonObjectSchema 处理
        return convertToObjectSchemaFromMap(map);
    }

    /**
     * 将JSONObject转换为JsonObjectSchema
     *
     * @param json JSONObject对象
     * @return JsonObjectSchema实例
     */
    private JsonObjectSchema convertToObjectSchema(JSONObject json) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        // 转换 description
        String description = json.getStr("description");
        if (description != null) {
            builder.description(description);
        }

        // 转换 properties，递归处理每个 JsonSchemaElement
        @SuppressWarnings("unchecked")
        Map<String, Object> propertiesMap = (Map<String, Object>) json.get("properties", Map.class);
        if (propertiesMap != null) {
            Map<String, JsonSchemaElement> properties = new HashMap<>();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                JsonSchemaElement element = convertJsonToSchemaElement(entry.getValue());
                if (element != null) {
                    properties.put(entry.getKey(), element);
                }
            }
            if (!properties.isEmpty()) {
                builder.addProperties(properties);
            }
        }

        // 转换 required
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) json.get("required", List.class);
        if (required != null && !required.isEmpty()) {
            builder.required(required.toArray(new String[0]));
        }

        // 转换 additionalProperties
        Boolean additionalProperties = json.getBool("additionalProperties");
        if (additionalProperties != null) {
            builder.additionalProperties(additionalProperties);
        }

        // 转换 definitions，递归处理每个 JsonSchemaElement
        @SuppressWarnings("unchecked")
        Map<String, Object> definitionsMap = (Map<String, Object>) json.get("definitions", Map.class);
        if (definitionsMap != null) {
            Map<String, JsonSchemaElement> definitions = new HashMap<>();
            for (Map.Entry<String, Object> entry : definitionsMap.entrySet()) {
                JsonSchemaElement element = convertJsonToSchemaElement(entry.getValue());
                if (element != null) {
                    definitions.put(entry.getKey(), element);
                }
            }
            if (!definitions.isEmpty()) {
                builder.definitions(definitions);
            }
        }

        return builder.build();
    }

    /**
     * 将Map转换为JsonObjectSchema
     *
     * @param map Map对象
     * @return JsonObjectSchema实例
     */
    private JsonObjectSchema convertToObjectSchemaFromMap(Map<?, ?> map) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

        // 转换 description
        String description = (String) map.get("description");
        if (description != null) {
            builder.description(description);
        }

        // 转换 properties，递归处理每个 JsonSchemaElement
        @SuppressWarnings("unchecked")
        Map<String, Object> propertiesMap = (Map<String, Object>) map.get("properties");
        if (propertiesMap != null) {
            Map<String, JsonSchemaElement> properties = new HashMap<>();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                JsonSchemaElement element = convertJsonToSchemaElement(entry.getValue());
                if (element != null) {
                    properties.put(entry.getKey(), element);
                }
            }
            if (!properties.isEmpty()) {
                builder.addProperties(properties);
            }
        }

        // 转换 required
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) map.get("required");
        if (required != null && !required.isEmpty()) {
            builder.required(required.toArray(new String[0]));
        }

        // 转换 additionalProperties
        Boolean additionalProperties = (Boolean) map.get("additionalProperties");
        if (additionalProperties != null) {
            builder.additionalProperties(additionalProperties);
        }

        // 转换 definitions，递归处理每个 JsonSchemaElement
        @SuppressWarnings("unchecked")
        Map<String, Object> definitionsMap = (Map<String, Object>) map.get("definitions");
        if (definitionsMap != null) {
            Map<String, JsonSchemaElement> definitions = new HashMap<>();
            for (Map.Entry<String, Object> entry : definitionsMap.entrySet()) {
                JsonSchemaElement element = convertJsonToSchemaElement(entry.getValue());
                if (element != null) {
                    definitions.put(entry.getKey(), element);
                }
            }
            if (!definitions.isEmpty()) {
                builder.definitions(definitions);
            }
        }

        return builder.build();
    }
}