package cn.lin037.nexus.config;

import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.serialize.JSONDeserializer;
import cn.hutool.json.serialize.JSONObjectSerializer;
import cn.lin037.nexus.common.util.*;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HutoolUtilsConfig {

    @PostConstruct
    public void init() {
        // 注册JsonObjectSchema相关转换器
        ConverterRegistry.getInstance().putCustom(JsonObjectSchema.class, JsonObjectSchemaConverter.class);

        // 注册JsonSchemaElement转换器（用于反序列化）
        ConverterRegistry.getInstance().putCustom(JsonSchemaElement.class, JsonSchemaElementConverter.class);

        // 注册枚举转换器 - 使用您创建的优秀转换器类
        // 注册AgentLearningDifficultyEnum的专用转换器
        ConverterRegistry.getInstance().putCustom(AgentLearningDifficultyEnum.class,
                new EnumConverter<>(AgentLearningDifficultyEnum.class));

        // 注册通用的枚举到字符串转换器
        ConverterRegistry.getInstance().putCustom(String.class, new EnumToStringConverter());

        // 为JSON序列化注册枚举序列化器 - 直接序列化为枚举的序列化值
        JSONUtil.putSerializer(AgentLearningDifficultyEnum.class, new JSONObjectSerializer<AgentLearningDifficultyEnum>() {
            @Override
            public void serialize(cn.hutool.json.JSONObject json, AgentLearningDifficultyEnum enumValue) {
                // 直接将枚举的序列化值写入JSON，而不是包装在对象中
                json.set("value", enumValue.getSerializationValue());
            }
        });

        // 为ToolExecutionStatus枚举注册序列化器
        JSONUtil.putSerializer(cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus.class,
                new JSONObjectSerializer<cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus>() {
                    @Override
                    public void serialize(cn.hutool.json.JSONObject json, cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus enumValue) {
                        json.set("name", enumValue.name());
                        json.set("desc", enumValue.getDesc());
                    }
                });

        // 注册JSON反序列化器
        JSONUtil.putDeserializer(AgentLearningDifficultyEnum.class, new JSONDeserializer<AgentLearningDifficultyEnum>() {
            @Override
            public AgentLearningDifficultyEnum deserialize(cn.hutool.json.JSON json) {
                if (json == null) return null;
                Object value = json.getByPath("value");
                if (value == null) return null;
                return AgentLearningDifficultyEnum.fromSerializationValue(value.toString());
            }
        });

        // 为ToolExecutionStatus枚举注册反序列化器
        JSONUtil.putDeserializer(cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus.class,
                new JSONDeserializer<cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus>() {
                    @Override
                    public cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus deserialize(cn.hutool.json.JSON json) {
                        Object name = json.getByPath("name");
                        if (name != null) {
                            return cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult.ToolExecutionStatus.valueOf(name.toString());
                        }
                        return null;
                    }
                });

        // 注册JsonObjectSchema的JSON序列化器
        JSONUtil.putSerializer(JsonObjectSchema.class, new JSONObjectSerializer<JsonObjectSchema>() {
            @Override
            public void serialize(cn.hutool.json.JSONObject json, JsonObjectSchema schema) {
                JsonObjectSchemaToStringConverter converter = new JsonObjectSchemaToStringConverter();
                String jsonStr = converter.convert(schema, null);
                cn.hutool.json.JSONObject schemaJson = JSONUtil.parseObj(jsonStr);
                json.putAll(schemaJson);
            }
        });
    }
}
