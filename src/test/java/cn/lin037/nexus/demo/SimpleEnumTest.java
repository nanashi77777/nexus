package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import org.junit.jupiter.api.Test;

/**
 * 简单的枚举序列化测试，不使用任何自定义配置
 *
 * @author LinSanQi
 */
public class SimpleEnumTest {

    @Test
    void testDefaultEnumSerialization() {
        System.out.println("=== 测试默认枚举序列化（无自定义配置）===");

        // 测试单个枚举的序列化
        AgentLearningDifficultyEnum difficulty = AgentLearningDifficultyEnum.EXPERT;

        System.out.println("枚举名称: " + difficulty.name());
        System.out.println("枚举toString: " + difficulty.toString());
        System.out.println("枚举序列化值: " + difficulty.getSerializationValue());

        // 使用Hutool进行序列化
        String enumJson = JSONUtil.toJsonStr(difficulty);
        System.out.println("Hutool序列化结果: " + enumJson);

        // 尝试反序列化
        try {
            AgentLearningDifficultyEnum deserializedEnum = JSONUtil.toBean(enumJson, AgentLearningDifficultyEnum.class);
            System.out.println("反序列化成功: " + deserializedEnum);
            System.out.println("枚举相等: " + (difficulty == deserializedEnum));
        } catch (Exception e) {
            System.out.println("反序列化失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 测试直接使用枚举名称进行反序列化
        try {
            String enumNameJson = "\"" + difficulty.name() + "\"";
            System.out.println("使用枚举名称JSON: " + enumNameJson);
            AgentLearningDifficultyEnum fromName = JSONUtil.toBean(enumNameJson, AgentLearningDifficultyEnum.class);
            System.out.println("从名称反序列化成功: " + fromName);
        } catch (Exception e) {
            System.out.println("从名称反序列化失败: " + e.getMessage());
        }
    }
}