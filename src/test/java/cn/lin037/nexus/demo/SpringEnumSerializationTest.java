package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 使用Spring上下文的枚举序列化测试
 * 这样可以确保HutoolUtilsConfig中的序列化器配置生效
 *
 * @author LinSanQi
 */
@SpringBootTest
public class SpringEnumSerializationTest {

    @Test
    void testEnumSerializationWithSpringContext() {
        System.out.println("=== 测试Spring上下文中的枚举序列化 ===");

        // 测试单个枚举的序列化
        AgentLearningDifficultyEnum difficulty = AgentLearningDifficultyEnum.EXPERT;

        System.out.println("枚举名称: " + difficulty.name());
        System.out.println("枚举toString: " + difficulty.toString());
        System.out.println("枚举序列化值: " + difficulty.getSerializationValue());

        // 使用Hutool进行序列化（应该使用我们注册的自定义序列化器）
        String enumJson = JSONUtil.toJsonStr(difficulty);
        System.out.println("使用自定义序列化器的结果: " + enumJson);

        // 尝试反序列化
        try {
            AgentLearningDifficultyEnum deserializedEnum = JSONUtil.toBean(enumJson, AgentLearningDifficultyEnum.class);
            System.out.println("反序列化成功: " + deserializedEnum);
            System.out.println("枚举相等: " + (difficulty == deserializedEnum));
        } catch (Exception e) {
            System.out.println("反序列化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}