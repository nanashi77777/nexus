package cn.lin037.nexus.application.agent.context;

import cn.lin037.nexus.application.agent.dto.ToolListItem;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.LoopLimitContext;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentChatMessageEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentLearningTaskEntity;
import cn.lin037.nexus.infrastructure.common.persistent.entity.agent.AgentMemoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agent聊天执行上下文对象
 * 应用层自定义的上下文，包含用户ID、会话ID、学习空间ID等核心标识信息。
 * 用于在工具执行过程中传递必要的业务上下文信息。
 * 实现了LoopLimitContext接口，具备循环限制能力。
 *
 * @author Lin037
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatExecutionContext implements LoopLimitContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 学习空间ID
     */
    private Long learningSpaceId;

    /**
     * 是否需要规划 (默认: false)
     */
    @Builder.Default
    private boolean isPlanningNeeded = false;

    /**
     * 当前会话关联的学习任务列表
     */
    private List<AgentLearningTaskEntity> learningTasks;

    /**
     * 当前会话关联的记忆列表
     */
    private List<AgentMemoryEntity> memories;

    /**
     * 工具请求消息实体（线程安全）
     * 用于存储工具调用请求时创建的消息记录，确保在后续的onToolExecutionFinish方法中可以进行更新操作
     */
    private final AtomicReference<AgentChatMessageEntity> currentToolRequestMessage = new AtomicReference<>();

    /**
     * 循环计数器（线程安全）
     * 用于统计当前对话会话中的循环调用次数，防止无限循环
     */
    private final AtomicInteger loopCounter = new AtomicInteger(0);

    /**
     * 工具列表（线程安全）
     * 存储当前批次的工具列表及其执行状态
     */
    private final AtomicReference<List<ToolListItem>> toolList = new AtomicReference<>();

    /**
     * 等待授权后关闭标识（线程安全）
     * 当工具需要用户授权时设置为true，用于控制onStreamingComplete时的会话状态变更
     * true: 流式响应结束后会话状态应保持为WAITING_TOOL_AUTHORIZATION
     * false: 流式响应结束后会话状态应恢复为NORMAL
     */
    private final AtomicBoolean waitAuthAfterClose = new AtomicBoolean(false);

    /**
     * 当前执行中的工具ID（线程安全）
     * 存储当前正在执行的工具的ID，用于在onToolExecutionFinish时快速定位对应的ToolListItem
     * 因为langchain4j的ToolExecutionRequest.id()与我们生成的ID不同，需要单独存储映射关系
     */
    private final AtomicReference<Long> currentExecutingToolId = new AtomicReference<>();

    /**
     * 获取工具请求消息实体
     *
     * @return 工具请求消息实体
     */
    public AgentChatMessageEntity getCurrentToolRequestMessage() {
        return currentToolRequestMessage.get();
    }

    /**
     * 设置工具请求消息实体
     *
     * @param message 工具请求消息实体
     */
    public void setCurrentToolRequestMessage(AgentChatMessageEntity message) {
        this.currentToolRequestMessage.set(message);
    }

    /**
     * 获取循环计数器
     * 返回用于统计循环调用次数的线程安全计数器。
     *
     * @return 循环计数器
     */
    @Override
    public AtomicInteger getLoopCounter() {
        return loopCounter;
    }

    /**
     * 获取工具列表
     *
     * @return 工具列表
     */
    public List<ToolListItem> getToolList() {
        return toolList.get();
    }

    /**
     * 设置工具列表
     *
     * @param toolList 工具列表
     */
    public void setToolList(List<ToolListItem> toolList) {
        this.toolList.set(toolList);
    }

    /**
     * 获取等待授权后关闭标识
     *
     * @return 等待授权后关闭标识
     */
    public boolean isWaitAuthAfterClose() {
        return waitAuthAfterClose.get();
    }

    /**
     * 设置等待授权后关闭标识
     *
     * @param waitAuth 等待授权标识
     */
    public void setWaitAuthAfterClose(boolean waitAuth) {
        this.waitAuthAfterClose.set(waitAuth);
    }

    /**
     * 获取当前执行中的工具ID
     *
     * @return 当前执行中的工具ID
     */
    public Long getCurrentExecutingToolId() {
        return currentExecutingToolId.get();
    }

    /**
     * 设置当前执行中的工具ID
     *
     * @param toolId 工具ID
     */
    public void setCurrentExecutingToolId(Long toolId) {
        this.currentExecutingToolId.set(toolId);
    }

    /**
     * 根据工具ID查找工具列表项
     *
     * @param toolId 工具ID
     * @return 匹配的工具列表项，未找到返回null
     */
    public ToolListItem findToolListItemById(Long toolId) {
        List<ToolListItem> currentToolList = getToolList();
        if (currentToolList == null || toolId == null) {
            return null;
        }
        return currentToolList.stream()
                .filter(item -> toolId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }
}