package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import org.junit.jupiter.api.Test;

/**
 * 枚举序列化调试测试
 * 用于调试枚举序列化问题
 *
 * @author Lin037
 */
public class EnumSerializationDebugTest {

    @Test
    void debugEnumSerialization() {
        System.out.println("=== 枚举序列化调试 ===");

        AgentLearningDifficultyEnum difficulty = AgentLearningDifficultyEnum.EXPERT;

        // 测试枚举的序列化值
        System.out.println("枚举名称: " + difficulty.name());
        System.out.println("序列化值: " + difficulty.getSerializationValue());
        System.out.println("toString: " + difficulty.toString());

        // 测试Hutool的JSON序列化
        String jsonStr = JSONUtil.toJsonStr(difficulty);
        System.out.println("Hutool JSON序列化结果: " + jsonStr);

        // 测试反序列化
        try {
            AgentLearningDifficultyEnum deserialized = JSONUtil.toBean(jsonStr, AgentLearningDifficultyEnum.class);
            System.out.println("反序列化结果: " + deserialized);
        } catch (Exception e) {
            System.out.println("反序列化失败: " + e.getMessage());
        }

        // 测试直接使用枚举名称进行反序列化
        try {
            String enumNameJson = "\"EXPERT\"";
            System.out.println("使用枚举名称JSON: " + enumNameJson);
            AgentLearningDifficultyEnum fromName = JSONUtil.toBean(enumNameJson, AgentLearningDifficultyEnum.class);
            System.out.println("从枚举名称反序列化: " + fromName);
        } catch (Exception e) {
            System.out.println("从枚举名称反序列化失败: " + e.getMessage());
        }
    }
}