package cn.lin037.nexus.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Converter;
import cn.hutool.json.JSONObject;
import cn.lin037.nexus.common.enums.JsonSchemaTypeEnum;
import dev.langchain4j.model.chat.request.json.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObjectSchemaConverter implements Converter<JsonObjectSchema> {
    @Override
    public JsonObjectSchema convert(Object value, JsonObjectSchema defaultValue) {
        if (value instanceof JSONObject json) {
            JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

            // 转换 description
            builder.description(json.getStr("description"));

            // 转换 properties，递归处理每个 JsonSchemaElement
            @SuppressWarnings("unchecked")
            Map<String, Object> propertiesMap = (Map<String, Object>) json.get("properties", Map.class);
            if (propertiesMap != null) {
                Map<String, JsonSchemaElement> properties = new HashMap<>();
                for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                    properties.put(entry.getKey(), convertJsonToSchemaElement(entry.getValue()));
                }
                builder.addProperties(properties);
            }

            // 转换 required
            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) json.get("required", List.class);
            if (required != null) {
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
                    definitions.put(entry.getKey(), convertJsonToSchemaElement(entry.getValue()));
                }
                builder.definitions(definitions);
            }

            return builder.build();
        }

        // 如果是 Map 类型，使用 Hutool 转换
        if (value instanceof Map) {
            return BeanUtil.toBean(value, JsonObjectSchema.class);
        }

        return null;
    }

    private JsonSchemaElement convertJsonToSchemaElement(Object value) {
        switch (value) {
            case null -> {
                return null;
            }
            case JSONObject json -> {
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
                        case OBJECT -> convert(json, null); // 递归处理对象类型
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
                    // 作为 JsonObjectSchema 处理，使用递归转换
                    return convert(json, null);
                }

                // 如果有 description 但没有其他标识，默认为 JsonStringSchema
                if (json.containsKey("description")) {
                    return JsonStringSchema.builder()
                            .description(json.getStr("description"))
                            .build();
                }

                // 默认作为 JsonObjectSchema 处理
                return convert(json, null);
            }
            case Map<?, ?> map -> {
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
                            case OBJECT -> BeanUtil.toBean(map, JsonObjectSchema.class);
                            case STRING -> BeanUtil.toBean(map, JsonStringSchema.class);
                            case INTEGER -> BeanUtil.toBean(map, JsonIntegerSchema.class);
                            case NUMBER -> BeanUtil.toBean(map, JsonNumberSchema.class);
                            case BOOLEAN -> BeanUtil.toBean(map, JsonBooleanSchema.class);
                            case ARRAY -> BeanUtil.toBean(map, JsonArraySchema.class);
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
                    // 作为 JsonObjectSchema 处理，使用递归转换
                    return BeanUtil.toBean(map, JsonObjectSchema.class);
                }

                // 如果没有 "type" 字段，尝试猜测类型
                if (map.containsKey("description")) {
                    // 创建 JsonStringSchema
                    return JsonStringSchema.builder()
                            .description((String) map.get("description"))
                            .build();
                }

                // 默认作为 JsonObjectSchema 处理
                return BeanUtil.toBean(map, JsonObjectSchema.class);
            }
            default -> {
            }
        }

        return null;
    }
}