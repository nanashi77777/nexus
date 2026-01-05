package cn.lin037.nexus.web.rest.v1.agent;

import cn.dev33.satoken.stp.StpUtil;
import cn.lin037.nexus.application.agent.service.AgentChatAppService;
import cn.lin037.nexus.common.model.vo.ResultVO;
import cn.lin037.nexus.web.rest.v1.agent.req.ChatReq;
import cn.lin037.nexus.web.rest.v1.agent.vo.StreamingChatVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


/**
 * Agent聊天控制器
 * 提供AI聊天接口，支持流式响应
 *
 * @author Lin037
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/agent/chat")
@RequiredArgsConstructor
public class AgentChatController {

    private final AgentChatAppService agentChatAppService;

    /**
     * 流式聊天接口
     * 使用SSE(Server-Sent Events)方式返回流式响应
     *
     * @param req 聊天请求参数
     * @return 流式响应数据
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ResultVO<StreamingChatVO>> streamChat(@RequestBody @Valid ChatReq req) {
        log.debug("收到流式聊天请求: {}", req);

        StpUtil.checkLogin();
        // 调用实际的服务进行处理
        return agentChatAppService.streamingChat(req);
    }

    /*    *//**
     * 允许工具调用接口
     *
     * @param req 工具授权请求
     *//*
    @PostMapping(path = "/allow-tools", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StreamingChatVO> allowToolCalls(@RequestBody @Valid ToolCallActionReq req) {
        log.debug("允许工具调用，请求: {}", req);
        return agentChatAppService.allowToolCalls(req.getSessionId(), req.getToolRecordIds());
    }

    *//**
     * 拒绝工具调用接口
     *
     * @param req 工具拒绝请求
     *//*
    @PostMapping("/deny-tools")
    public void denyToolCalls(@RequestBody @Valid ToolCallActionReq req) {
        log.debug("拒绝工具调用，请求: {}", req);
        agentChatAppService.refuseToolCalls(req.getSessionId(), req.getToolRecordIds());
    }*/

    /**
     * 取消流式对话接口
     *
     * @param sessionId 会话ID
     */
    @PostMapping("/cancel/{sessionId}")
    public void cancelStreaming(@PathVariable Long sessionId) {
        log.debug("取消流式对话，会话ID: {}", sessionId);
        agentChatAppService.cancelStreaming(sessionId);
    }
}