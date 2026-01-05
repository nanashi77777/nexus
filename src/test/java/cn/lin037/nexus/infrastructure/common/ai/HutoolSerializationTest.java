package cn.lin037.nexus.infrastructure.common.ai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Converter;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 本测试演示了使用Hutool JSON对采用构建器模式且没有公共默认构造函数的对象进行序列化/反序列化的方法
 */
public class HutoolSerializationTest {

    // --- 模拟环境的辅助类 ---

    /**
     * 注册自定义转换器
     */
    @BeforeAll
    public static void setupCustomConverter() {
        ConverterRegistry.getInstance().putCustom(JsonObjectSchema.class, JsonObjectSchemaConverter.class);
        ConverterRegistry.getInstance().putCustom(ToolSpecificationConfig.class, ToolSpecificationConfigConverter.class);
    }

    @Test
    void testSerializationWithDtoApproach() {
        System.out.println("--- 运行方案1：DTO方法 ---");

        // 1. 创建原始复杂对象
        Map<String, Object> props = new HashMap<>();
        props.put("location", new HashMap<String, String>() {{
            put("type", "string");
            put("description", "城市和州，例如：旧金山, CA");
        }});
        props.put("unit", "摄氏度");

        JsonObjectSchema originalSchema = JsonObjectSchema.builder()
                .description("获取指定位置的当前天气")
                .properties(props)
                .required(Collections.singletonList("location"))
                .build();
        ToolSpecificationConfig originalConfig = new ToolSpecificationConfig("get_weather", originalSchema);
        System.out.println("原始对象: " + originalConfig);

        // 2. 将原始对象转换为其DTO表示形式
        ToolSpecificationConfigDTO configDTO = new ToolSpecificationConfigDTO();
        configDTO.setName(originalConfig.getName());
        // 这里我们使用Hutool的BeanUtil进行快速转换，也可以手动完成
        configDTO.setParameters(BeanUtil.copyProperties(originalConfig.getParameters(), JsonObjectSchemaDTO.class));

        // 3. 将DTO序列化为JSON字符串。这将完美工作。
        String jsonString = JSONUtil.toJsonStr(configDTO);
        System.out.println("序列化后的JSON: " + jsonString);

        // 4. 将JSON字符串反序列化回DTO。这也有效。
        ToolSpecificationConfigDTO deserializedDTO = JSONUtil.toBean(jsonString, ToolSpecificationConfigDTO.class);

        // 5. 使用构建器将反序列化的DTO转换回原始对象
        JsonObjectSchema finalSchema = JsonObjectSchema.builder()
                .description(deserializedDTO.getParameters().getDescription())
                .properties(deserializedDTO.getParameters().getProperties())
                .required(deserializedDTO.getParameters().getRequired())
                .build();
        ToolSpecificationConfig finalConfig = new ToolSpecificationConfig(deserializedDTO.getName(), finalSchema);
        System.out.println("最终重建的对象: " + finalConfig);

        // 6. 验证重建的对象是否与原始对象相同
        assertEquals(originalConfig, finalConfig);
        System.out.println("DTO方法成功：原始和重建的对象相等。");
    }


    // --- 方案1：DTO（数据传输对象）方法 ---

    @Test
    void testSerializationWithCustomConverter() {
        System.out.println(" --- 运行方案2：自定义转换器方法 ---");

        // 1. 创建原始复杂对象（与之前相同）
        Map<String, Object> props = new HashMap<>();
        props.put("location", new HashMap<String, String>() {{
            put("type", "string");
            put("description", "城市和州，例如：旧金山, CA");
        }});
        props.put("unit", "摄氏度");

        JsonObjectSchema originalSchema = JsonObjectSchema.builder()
                .description("获取指定位置的当前天气")
                .properties(props)
                .required(Collections.singletonList("location"))
                .build();
        ToolSpecificationConfig originalConfig = new ToolSpecificationConfig("get_weather", originalSchema);
        System.out.println("原始对象: " + originalConfig);

        // 2. 直接序列化对象。Hutool会将其转换为类似Map的结构
        String jsonString = JSONUtil.toJsonStr(originalConfig);
        System.out.println("序列化后的JSON: " + jsonString);

        // 3. 直接反序列化为目标类
        // 因为我们注册了JsonObjectSchema的转换器，Hutool现在知道如何构建它
        ToolSpecificationConfig finalConfig = JSONUtil.toBean(jsonString, ToolSpecificationConfig.class);
        System.out.println("最终重建的对象: " + finalConfig);

        // 4. 验证重建的对象是否与原始对象相同
        assertEquals(originalConfig, finalConfig);
        System.out.println("自定义转换器方法成功：原始和重建的对象相等。");
    }

    /**
     * 主配置类
     * 示例中包含一个名称和预处理的JsonObjectSchema类型'parameters'对象
     */
    @Data
    public static class ToolSpecificationConfig {
        private final String name;
        private final JsonObjectSchema parameters;

        public ToolSpecificationConfig(String name, JsonObjectSchema parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return "ToolSpecificationConfig{" +
                    "name='" + name + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }

    /**
     * 模拟langchain4j的JsonObjectSchema
     * 使用构建器模式且没有公共构造函数，这是问题的核心
     * 注意：内部Map为了演示简化为Map<String, Object>
     */
    @Getter
    public static class JsonObjectSchema {
        // Getter方法
        private final String description;
        private final Map<String, Object> properties;
        private final List<String> required;

        private JsonObjectSchema(Builder builder) {
            this.description = builder.description;
            this.properties = builder.properties;
            this.required = builder.required;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonObjectSchema that = (JsonObjectSchema) o;
            return Objects.equals(description, that.description) && Objects.equals(properties, that.properties) && Objects.equals(required, that.required);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description, properties, required);
        }

        @Override
        public String toString() {
            return "JsonObjectSchema{" +
                    "description='" + description + '\'' +
                    ", properties=" + properties +
                    ", required=" + required +
                    '}';
        }

        public static class Builder {
            private String description;
            private Map<String, Object> properties;
            private List<String> required;

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder properties(Map<String, Object> properties) {
                this.properties = properties;
                return this;
            }

            public Builder required(List<String> required) {
                this.required = required;
                return this;
            }

            public JsonObjectSchema build() {
                return new JsonObjectSchema(this);
            }
        }
    }


    // --- 方案2：自定义转换器方法 ---

    /**
     * 简单的JsonObjectSchema POJO
     * 包含默认构造函数和公共Getter/Setter方法，便于Hutool处理
     */
    @Data
    public static class JsonObjectSchemaDTO {
        // Getter和Setter方法
        private String description;
        private Map<String, Object> properties;
        private List<String> required;

        public JsonObjectSchemaDTO() {
        } // Hutool需要这个默认构造函数

    }

    /**
     * 主配置对象的DTO
     */
    @Data
    public static class ToolSpecificationConfigDTO {
        // Getter和Setter方法
        private String name;
        private JsonObjectSchemaDTO parameters;

        public ToolSpecificationConfigDTO() {
        } // Hutool需要这个默认构造函数

    }

    /**
     * 自定义转换器，教Hutool如何将JSONObject转换为我们的JsonObjectSchema
     */
    public static class JsonObjectSchemaConverter implements Converter<JsonObjectSchema> {
        @Override
        public JsonObjectSchema convert(Object value, JsonObjectSchema defaultValue) {
            if (value instanceof JSONObject json) {
                JsonObjectSchema.Builder builder = JsonObjectSchema.builder();

                builder.description(json.getStr("description"));
                // 注意：对于复杂的内部对象，可能需要递归转换
                // 此处假设可以直接转换
                @SuppressWarnings("unchecked")
                Map<String, Object> properties = (Map<String, Object>) json.get("properties", Map.class);
                builder.properties(properties);
                @SuppressWarnings("unchecked")
                List<String> required = (List<String>) json.get("required", List.class);
                builder.required(required);

                return builder.build();
            }
            // 也可以从Map转换
            if (value instanceof Map) {
                return BeanUtil.toBean(value, JsonObjectSchema.class);
            }
            return null;
        }
    }

    public static class ToolSpecificationConfigConverter implements Converter<ToolSpecificationConfig> {
        @Override
        public ToolSpecificationConfig convert(Object value, ToolSpecificationConfig defaultValue) {
            if (value instanceof JSONObject json) {
                String name = json.getStr("name");
                JsonObjectSchema parameters = json.get("parameters", JsonObjectSchema.class);
                return new ToolSpecificationConfig(name, parameters);
            }
            return null;
        }
    }
}
