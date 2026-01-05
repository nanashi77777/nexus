package cn.lin037.nexus.infrastructure.adapter.agent.impl;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.LearningPlanBatchCreateParams;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.LearningPlanCompletionParams;
import cn.lin037.nexus.infrastructure.adapter.agent.dto.ToolExecutionResponse;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.service.ToolExecutor;
import cn.lin037.nexus.infrastructure.common.id.HutoolSnowflakeIdGenerator;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock版本的AgentChat工具执行器（用于测试）
 * 不涉及任何数据库操作，使用内存数据结构模拟数据存储
 * 支持的工具清单：
 * - 记忆添加: tool name = "memory_add"
 * - 记忆删除: tool name = "memory_delete"
 * - 学习计划批量创建: tool name = "learning_plan_batch_create"
 * - 学习计划更新: tool name = "learning_plan_update"
 * - 学习计划批量删除: tool name = "learning_plan_batch_delete"
 * - 学习计划完成状态更新: tool name = "learning_plan_completion"
 * @author LinSanQi
 */
@Slf4j
@Component("MockAgentChatToolExecutorImpl")
public class MockAgentChatToolExecutorImpl implements ToolExecutor<AgentChatExecutionContext> {

    /**
     * 执行AI模型请求的工具（带上下文）
     *
     * @param toolExecutionRequest 工具执行请求
     * @param context              工具执行上下文
     * @return 工具执行结果消息
     */
    @Override
    public ToolExecutionResult execute(ToolExecutionRequest toolExecutionRequest, AgentChatExecutionContext context) {
        try {
            String toolName = toolExecutionRequest.name();
            String arguments = toolExecutionRequest.arguments();

            log.info("[Mock] 执行工具: {}, 参数: {}", toolName, arguments);

            String result = executeToolByName(toolName, arguments, context);
            return ToolExecutionResult.success(toolExecutionRequest, result);
        } catch (Exception e) {
            log.error("[Mock] 工具执行失败: {}", e.getMessage(), e);
            return ToolExecutionResult.error(toolExecutionRequest, "工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 根据工具名称执行具体的工具逻辑
     *
     * @param toolName  工具名称
     * @param arguments 工具参数
     * @param context   工具执行上下文
     * @return 执行结果
     */
    private String executeToolByName(String toolName, String arguments, AgentChatExecutionContext context) {
        return switch (toolName) {
            case "memory_add" -> memoryAdd(arguments, context);
            case "memory_delete" -> memoryDelete(arguments, context);
            case "learning_plan_batch_create" -> learningPlanBatchCreate(arguments, context);
            case "learning_plan_update" -> learningPlanUpdate(arguments, context);
            case "learning_plan_batch_delete" -> learningPlanBatchDelete(arguments, context);
            case "learning_plan_completion" -> learningPlanCompletion(arguments, context);
            default -> {
                log.warn("[Mock] 未知工具类型: {}", toolName);
                yield "未知工具类型: " + toolName;
            }
        };
    }

    /**
     * 记忆添加工具
     *
     * @param arguments JSON格式的记忆参数
     * @param context   执行上下文
     * @return 添加结果
     */
    private String memoryAdd(String arguments, AgentChatExecutionContext context) {
        try {
            ToolExecutionResponse response = ToolExecutionResponse.success("记忆添加成功", HutoolSnowflakeIdGenerator.generateLongId());
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("记忆添加失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("记忆添加失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 记忆删除工具
     *
     * @param arguments JSON格式的删除参数
     * @param context   执行上下文
     * @return 删除结果
     */
    private String memoryDelete(String arguments, AgentChatExecutionContext context) {
        try {

            ToolExecutionResponse response = ToolExecutionResponse.success("记忆删除成功");
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("记忆删除失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("记忆删除失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划批量创建工具
     *
     * @param arguments JSON格式的创建参数
     * @param context   执行上下文
     * @return 创建结果
     */
    private String learningPlanBatchCreate(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanBatchCreateParams params = JSONUtil.toBean(arguments, LearningPlanBatchCreateParams.class);
            List<Long> createdIds = new ArrayList<>();
            for (LearningPlanBatchCreateParams.CreateItem item : params.getItems()) {
                createdIds.add(HutoolSnowflakeIdGenerator.generateLongId());
            }

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划批量创建成功", createdIds);
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划批量创建失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划批量创建失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划更新工具
     *
     * @param arguments JSON格式的更新参数
     * @param context   执行上下文
     * @return 更新结果
     */
    private String learningPlanUpdate(String arguments, AgentChatExecutionContext context) {
        try {

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划更新成功");
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划更新失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划更新失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划批量删除工具
     *
     * @param arguments JSON格式的删除参数
     * @param context   执行上下文
     * @return 删除结果
     */
    private String learningPlanBatchDelete(String arguments, AgentChatExecutionContext context) {
        try {

            ToolExecutionResponse response = ToolExecutionResponse.success("学习计划批量删除成功");
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划批量删除失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划批量删除失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }

    /**
     * 学习计划完成状态更新工具
     *
     * @param arguments JSON格式的完成状态参数
     * @param context   执行上下文
     * @return 更新结果
     */
    private String learningPlanCompletion(String arguments, AgentChatExecutionContext context) {
        try {
            LearningPlanCompletionParams params = JSONUtil.toBean(arguments, LearningPlanCompletionParams.class);
            String message = params.getIsCompleted() ? "学习计划标记为已完成" : "学习计划取消完成状态";
            ToolExecutionResponse response = ToolExecutionResponse.success(message, params.getPlanId());
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("学习计划完成状态更新失败: {}", e.getMessage(), e);
            ToolExecutionResponse response = ToolExecutionResponse.error("学习计划完成状态更新失败: " + e.getMessage());
            return JSONUtil.toJsonStr(response);
        }
    }
}