package cn.lin037.nexus.infrastructure.adapter.knowledge;

import cn.hutool.core.bean.BeanUtil;
import cn.lin037.nexus.infrastructure.adapter.knowledge.constant.KnowledgeTaskConstant;
import cn.lin037.nexus.infrastructure.adapter.knowledge.params.AiGenerateKnowledgeTaskParameters;
import cn.lin037.nexus.infrastructure.common.task.api.AsyncTaskManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 知识模块任务适配器
 *
 * @author LinSanQi
 */
@Component
@RequiredArgsConstructor
public class KnowledgeTaskAdapter {

    private final AsyncTaskManager asyncTaskManager;

    /**
     * 提交一个知识图谱AI生成任务
     *
     * @param parameters      任务参数
     * @param ownerIdentifier 任务所有者标识
     * @return 任务ID
     */
    public Long submitAiGenerateTask(AiGenerateKnowledgeTaskParameters parameters, String ownerIdentifier) {
        // 1. 将参数对象转换为Map，因为AsyncTaskManager.submit需要Map<String, Object>
        Map<String, Object> parametersMap = BeanUtil.beanToMap(parameters);

        // 2. 调用异步任务管理器提交任务
        return asyncTaskManager.submit(
                KnowledgeTaskConstant.TASK_TYPE_KNOWLEDGE_AI_GENERATE,
                parametersMap,
                ownerIdentifier
        );
    }
}
