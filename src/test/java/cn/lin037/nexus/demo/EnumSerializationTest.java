package cn.lin037.nexus.demo;

import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.LearningPlanBatchCreateParams;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 枚举序列化测试类
 * 测试LearningPlanBatchCreateParams的difficultyLevel字段（AgentLearningDifficultyEnum类型）的序列化和反序列化功能
 *
 * @author Lin037
 */
@Slf4j
@SpringBootTest
public class EnumSerializationTest {

    @Test
    void testLearningPlanBatchCreateParamsSerialization() {
        log.info("=== 开始测试 LearningPlanBatchCreateParams 枚举序列化 ===");

        // 创建测试数据
        LearningPlanBatchCreateParams params = new LearningPlanBatchCreateParams();

        LearningPlanBatchCreateParams.CreateItem item1 = new LearningPlanBatchCreateParams.CreateItem();
        item1.setTitle("Java基础学习");
        item1.setObjective("掌握Java基本语法和面向对象编程");
        item1.setDifficultyLevel(AgentLearningDifficultyEnum.BEGINNER);

        LearningPlanBatchCreateParams.CreateItem item2 = new LearningPlanBatchCreateParams.CreateItem();
        item2.setTitle("Spring框架进阶");
        item2.setObjective("深入理解Spring核心原理和高级特性");
        item2.setDifficultyLevel(AgentLearningDifficultyEnum.EXPERT);

        params.setItems(Arrays.asList(item1, item2));

        log.info("原始数据: {}", params);
        log.info("item1 难度级别: {}", item1.getDifficultyLevel());
        log.info("item2 难度级别: {}", item2.getDifficultyLevel());

        // 序列化为JSON
        String jsonStr = JSONUtil.toJsonStr(params);
        log.info("序列化结果: {}", jsonStr);

        // 验证序列化结果包含枚举名称
        assertTrue(jsonStr.contains("BEGINNER"), "序列化结果应包含BEGINNER");
        assertTrue(jsonStr.contains("EXPERT"), "序列化结果应包含EXPERT");

        // 反序列化回对象
        LearningPlanBatchCreateParams deserializedParams = JSONUtil.toBean(jsonStr, LearningPlanBatchCreateParams.class);
        log.info("反序列化结果: {}", deserializedParams);

        // 验证反序列化结果
        assertNotNull(deserializedParams, "反序列化结果不应为null");
        assertNotNull(deserializedParams.getItems(), "items列表不应为null");
        assertEquals(2, deserializedParams.getItems().size(), "items列表大小应为2");

        LearningPlanBatchCreateParams.CreateItem deserializedItem1 = deserializedParams.getItems().get(0);
        LearningPlanBatchCreateParams.CreateItem deserializedItem2 = deserializedParams.getItems().get(1);

        log.info("反序列化 item1 难度级别: {}", deserializedItem1.getDifficultyLevel());
        log.info("反序列化 item2 难度级别: {}", deserializedItem2.getDifficultyLevel());

        // 验证枚举值是否正确
        assertEquals(AgentLearningDifficultyEnum.BEGINNER, deserializedItem1.getDifficultyLevel(), "item1难度级别应为BEGINNER");
        assertEquals(AgentLearningDifficultyEnum.EXPERT, deserializedItem2.getDifficultyLevel(), "item2难度级别应为EXPERT");

        log.info("枚举序列化测试通过！");
    }

    @Test
    void testSingleEnumSerialization() {
        log.info("=== 开始测试单个枚举序列化 ===");

        AgentLearningDifficultyEnum enumValue = AgentLearningDifficultyEnum.BEGINNER;
        log.info("测试枚举值: {}", enumValue);

        // 测试枚举转换为字符串
        String convertedStr = Convert.toStr(enumValue);
        log.info("转换为字符串结果: {}", convertedStr);

        // 测试字符串转换为枚举
        AgentLearningDifficultyEnum convertedEnum = Convert.convert(AgentLearningDifficultyEnum.class, "BEGINNER");
        log.info("从字符串转换为枚举结果: {}", convertedEnum);

        // 验证转换结果
        assertEquals("BEGINNER", convertedStr);
        assertEquals(enumValue, convertedEnum);

        // 测试序列化方法
        Object serializationValue = enumValue.getSerializationValue();
        log.info("序列化值: {}", serializationValue);
        assertEquals("BEGINNER", serializationValue);

        log.info("单个枚举序列化测试通过！");
    }

    @Test
    void testEnumDeserializationFromString() {
        log.info("=== 开始测试从字符串反序列化枚举 ===");

        // 测试从枚举名称字符串反序列化
        String[] enumNames = {"BEGINNER", "INTERMEDIATE", "ADVANCED", "EXPERT"};
        AgentLearningDifficultyEnum[] expectedEnums = {
                AgentLearningDifficultyEnum.BEGINNER,
                AgentLearningDifficultyEnum.INTERMEDIATE,
                AgentLearningDifficultyEnum.ADVANCED,
                AgentLearningDifficultyEnum.EXPERT
        };

        for (int i = 0; i < enumNames.length; i++) {
            String enumName = enumNames[i];
            AgentLearningDifficultyEnum expected = expectedEnums[i];

            log.info("测试从字符串 '{}' 反序列化", enumName);

            // 使用接口的反序列化方法
            AgentLearningDifficultyEnum result = AgentLearningDifficultyEnum.fromSerializationValue(enumName);
            log.info("反序列化结果: {}", result);

            assertEquals(expected, result, "反序列化结果应正确");
        }

        // 测试无效的枚举名称
        log.info("测试无效枚举名称");
        AgentLearningDifficultyEnum invalidResult = AgentLearningDifficultyEnum.fromSerializationValue("INVALID");
        log.info("无效枚举名称反序列化结果: {}", invalidResult);
        assertNull(invalidResult, "无效枚举名称应返回null");

        log.info("字符串反序列化测试通过！");
    }
}