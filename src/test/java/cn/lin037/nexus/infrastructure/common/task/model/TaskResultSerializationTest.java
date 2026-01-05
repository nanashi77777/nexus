package cn.lin037.nexus.infrastructure.common.task.model;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.TokenUsageAccumulator;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

public class TaskResultSerializationTest {

    @Test
    void testResultJson() {

        TokenUsageAccumulator tokenUsageAccumulator = new TokenUsageAccumulator();
        tokenUsageAccumulator.add(new TokenUsage(1, 2));
        TaskResult<TokenUsageAccumulator> result = TaskResult.success(tokenUsageAccumulator);
        String jsonStr = JSONUtil.toJsonStr(result);
        System.out.println(jsonStr);
    }
}
