package cn.lin037.nexus.infrastructure.common.ai;

import cn.hutool.json.JSONUtil;
import cn.lin037.nexus.application.agent.context.AgentChatExecutionContext;
import cn.lin037.nexus.infrastructure.common.ai.constant.enums.*;
import cn.lin037.nexus.infrastructure.common.ai.model.dto.ToolExecutionResult;
import cn.lin037.nexus.infrastructure.common.ai.service.AiCoreService;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatListener;
import cn.lin037.nexus.infrastructure.common.ai.service.StreamingChatToolService;
import cn.lin037.nexus.infrastructure.common.ai.service.ToolExecutor;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class AiStreamingChatToolServiceTest {

    @Autowired
    private StreamingChatToolService streamingChatToolService;

    @Autowired
    private AiCoreService aiCoreService;

    @Autowired
    @Qualifier("MockAgentChatToolExecutorImpl")
    private ToolExecutor<AgentChatExecutionContext> mockAgentChatToolExecutor;

    @Test
    void testStreamingChatWithNewListener() throws InterruptedException {
        // 创建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("我想要学习Satoken"));
        messages.add(AiMessage.from("""
                我很高兴你想学习Satoken！这是一个很好的学习目标。让我先帮你明确一下学习方向。
                
                为了给你制定最适合的学习计划，我想先了解一些信息：
                
                1. **你目前的编程基础如何？** 比如：
                   - 对Java语言熟悉程度
                   - 是否有过Web开发经验
                   - 是否了解过其他认证授权框架（如Spring Security、Shiro等）
                
                2. **你学习Satoken的具体目的是什么？** 比如：
                   - 用于个人项目开发
                   - 工作需要
                   - 扩展技术栈
                   - 其他特定用途
                
                3. **你希望达到什么样的掌握程度？** 比如：
                   - 基本使用
                   - 深入理解原理
                   - 能够进行二次开发
                
                你能分享一下这些信息吗？这样我就能为你制定一个更有针对性的学习计划了！
                """));
        messages.add(new UserMessage("我有过Web开发经验,了解过Shiro,学习Satoken的具体目的是用于个人项目开发,希望达到基本使用的掌握程度"));

        // 使用CountDownLatch确保异步操作完成
        CountDownLatch latch = new CountDownLatch(1);

        // 模拟累积内容，用于跟踪内容累积过程
        StringBuilder accumulatedContent = new StringBuilder();
        
        // 创建新版本的StreamingChatListener，详细跟踪所有事件调用顺序
        StreamingChatListener<AgentChatExecutionContext> listener = new StreamingChatListener<>() {

            /**
             * 处理思考内容的文本片段
             */
            @Override
            public void onThinkingToken(String token, AgentChatExecutionContext context) {
                accumulatedContent.append(token);
                log.info("=== [事件1-思考Token] 收到思考片段: '{}', 累积内容长度: {}", 
                    token.replace("\n", "\\n"), accumulatedContent.length());
            }

            /**
             * 处理思考完成事件
             */
            @Override
            public ThinkingDecision onThinkingComplete(AgentChatExecutionContext context) {
                log.info("=== [事件2-思考完成] 思考阶段结束，累积内容长度: {}", accumulatedContent.length());
                return ThinkingDecision.CONTINUE;
            }

            /**
             * 处理内容的文本片段
             */
            @Override
            public void onContentToken(String token, AgentChatExecutionContext context) {
                accumulatedContent.append(token);
                log.info("=== [事件3-内容Token] 收到内容片段: '{}', 累积内容长度: {}", 
                    token.replace("\n", "\\n"), accumulatedContent.length());
            }

            /**
             * 处理完整消息事件
             */
            @Override
            public ContentDecision onCompleteMessage(AiMessage aiMessage, TokenUsage tokenUsage, FinishReason finishReason, AgentChatExecutionContext context) {
                log.info("=== [事件4-完整消息] =======================");
                String aiMessageText = aiMessage.text();
                log.info("AI消息内容长度: {}", aiMessageText != null ? aiMessageText.length() : 0);
                log.info("AI消息内容: '{}'", aiMessageText != null ? aiMessageText : "");
                log.info("完成原因: {}", finishReason);
                log.info("累积内容长度: {}", accumulatedContent.length());
                log.info("累积内容: '{}'", accumulatedContent.toString());
                if (tokenUsage != null) {
                    log.info("Token使用 - 输入: {}, 输出: {}, 总计: {}",
                            tokenUsage.inputTokenCount(),
                            tokenUsage.outputTokenCount(),
                            tokenUsage.totalTokenCount());
                }
                
                // 模拟实际业务逻辑：根据finishReason决定是否保存并清空内容
                if (finishReason != FinishReason.TOOL_EXECUTION) {
                    log.info(">>> 应该保存累积内容并清空: '{}'", accumulatedContent.toString());
                    accumulatedContent.setLength(0); // 清空
                } else {
                    log.info(">>> 跳过保存（工具执行情况），但累积内容未清空");
                }
                log.info("=== [事件4-完整消息] 结束 ===================");
                return ContentDecision.CONTINUE;
            }

            /**
             * 处理完整工具列表事件
             */
            @Override
            public ToolBatchDecision onReceiveCompleteToolList(List<ToolExecutionRequest> toolExecutionRequests, TokenUsage tokenUsage, AgentChatExecutionContext context) {
                log.info("=== [事件5-工具列表] 收到 {} 个工具调用请求", toolExecutionRequests.size());
                for (int i = 0; i < toolExecutionRequests.size(); i++) {
                    ToolExecutionRequest request = toolExecutionRequests.get(i);
                    log.info("工具请求{}: 名称={}, 参数={}", i + 1, request.name(), request.arguments());
                }
                log.info("当前累积内容长度: {}", accumulatedContent.length());
                return ToolBatchDecision.PROCEED;
            }

            /**
             * 处理单个工具执行开始事件
             */
            @Override
            public ToolDecision onToolExecutionStart(ToolExecutionRequest toolExecutionRequest, AgentChatExecutionContext context) {
                log.info("=== [事件6-工具执行开始] 工具: {}", toolExecutionRequest.name());
                return ToolDecision.EXECUTE;
            }

            /**
             * 处理工具执行完成事件
             */
            @Override
            public ToolContinueDecision onToolExecutionFinish(ToolExecutionResult result, AgentChatExecutionContext context) {
                log.info("=== [事件7-工具执行完成] 工具: {}, 状态: {}", 
                    result.getRequest() != null ? result.getRequest().name() : "未知",
                    result.getStatus());
                return ToolContinueDecision.CONTINUE;
            }

            /**
             * 处理所有工具完成事件
             */
            @Override
            public LoopDecision onAllToolsComplete(AgentChatExecutionContext context) {
                log.info("=== [事件8-所有工具完成] 继续下一轮循环，累积内容长度: {}", accumulatedContent.length());
                return LoopDecision.CONTINUE_LOOP;
            }

            /**
             * 处理流式响应完成事件
             */
            @Override
            public void onStreamingComplete(AiMessage finalMessage, AgentChatExecutionContext context) {
                log.info("=== [事件9-流式完成] 最终消息长度: {}, 累积内容长度: {}", 
                    finalMessage.text().length(), accumulatedContent.length());
                log.info("最终消息: '{}'", finalMessage.text());
                log.info("最终累积内容: '{}'", accumulatedContent.toString());
                latch.countDown();
            }

            /**
             * 处理错误事件
             */
            @Override
            public void onError(Throwable error, AgentChatExecutionContext context) {
                log.error("=== [事件ERROR] 发生异常: {}", error.getMessage(), error);
                latch.countDown();
            }
        };

        // 启动流式聊天
        log.info("[测试开始] 启动新版本流式聊天测试");
        streamingChatToolService.chat(
                "deepseek-chat",
                "STREAMING_CHAT_WITH_TOOL",
                messages,
                mockAgentChatToolExecutor,
                listener,
                new AgentChatExecutionContext(1L, 1L, 1L)
        );

        // 等待异步操作完成，设置超时时间避免无限等待
        boolean completed = latch.await(5, TimeUnit.MINUTES);
        assertTrue(completed, "测试应在5分钟内完成");
        log.info("[测试结束] 新版本流式聊天测试完成");
    }

    @Test
    void testGetToolSpecifications() {
        StreamingChatModel streamingChatWithTool = aiCoreService.getStreamingChatModel("deepseek-chat",
                "STREAMING_CHAT_WITH_TOOL");
        List<ToolSpecification> toolSpecifications = streamingChatWithTool.defaultRequestParameters().toolSpecifications();
        System.out.println(JSONUtil.toJsonStr(toolSpecifications));
    }
}
