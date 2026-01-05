package cn.lin037.nexus.demo;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.persistent.enums.AgentLearningDifficultyEnum;
import cn.lin037.nexus.infrastructure.common.task.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Hutool 枚举序列化测试
 * 测试 Hutool JSONUtil 对枚举的序列化和反序列化功能
 *
 * @author LinSanQi
 */
@SpringBootTest
public class HutoolEnumTest {

    @Test
    void testEnumSerialization() {
        System.out.println("=== 测试 Hutool JSONUtil 枚举序列化 ===");

        // 创建测试数据
        TestData testData = new TestData(
                "测试数据",
                AgentLearningDifficultyEnum.INTERMEDIATE,
                TaskStatusEnum.RUNNING,
                ToolExecutionResult.ToolExecutionStatus.SUCCESS
        );

        // 序列化为JSON
        String jsonStr = JSONUtil.toJsonStr(testData);
        System.out.println("序列化结果: " + jsonStr);

        // 反序列化回对象
        TestData deserializedData = JSONUtil.toBean(jsonStr, TestData.class);
        System.out.println("反序列化结果: " + deserializedData);

        // 验证枚举值是否正确
        System.out.println("原始枚举值: " + testData.getDifficulty());
        System.out.println("反序列化枚举值: " + deserializedData.getDifficulty());
        System.out.println("枚举值相等: " + (testData.getDifficulty() == deserializedData.getDifficulty()));

        System.out.println("原始状态枚举: " + testData.getStatus());
        System.out.println("反序列化状态枚举: " + deserializedData.getStatus());
        System.out.println("状态枚举相等: " + (testData.getStatus() == deserializedData.getStatus()));

        System.out.println("原始工具状态: " + testData.getToolStatus());
        System.out.println("反序列化工具状态: " + deserializedData.getToolStatus());
        System.out.println("工具状态相等: " + (testData.getToolStatus() == deserializedData.getToolStatus()));
    }

    @Test
    void testSingleEnumSerialization() {
        System.out.println("\n=== 测试单个枚举序列化 ===");

        // 测试单个枚举的序列化
        AgentLearningDifficultyEnum difficulty = AgentLearningDifficultyEnum.EXPERT;
        String enumJson = JSONUtil.toJsonStr(difficulty);
        System.out.println("单个枚举序列化: " + enumJson);

        // 反序列化单个枚举
        AgentLearningDifficultyEnum deserializedEnum = JSONUtil.toBean(enumJson, AgentLearningDifficultyEnum.class);
        System.out.println("反序列化枚举: " + deserializedEnum);
        System.out.println("枚举相等: " + (difficulty == deserializedEnum));
    }

    @Test
    void testEnumWithCustomValues() {
        System.out.println("\n=== 测试带自定义值的枚举 ===");

        // 测试带有自定义描述的枚举
        ToolExecutionResult.ToolExecutionStatus status = ToolExecutionResult.ToolExecutionStatus.ERROR;
        String statusJson = JSONUtil.toJsonStr(status);
        System.out.println("带描述枚举序列化: " + statusJson);

        // 反序列化
        ToolExecutionResult.ToolExecutionStatus deserializedStatus = JSONUtil.toBean(statusJson, ToolExecutionResult.ToolExecutionStatus.class);
        System.out.println("反序列化状态: " + deserializedStatus);
        System.out.println("状态描述: " + deserializedStatus.getDesc());
        System.out.println("状态相等: " + (status == deserializedStatus));
    }

    /**
     * 测试数据类，包含不同类型的枚举字段
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestData {
        private String name;
        private AgentLearningDifficultyEnum difficulty;
        private TaskStatusEnum status;
        private ToolExecutionResult.ToolExecutionStatus toolStatus;
    }
}