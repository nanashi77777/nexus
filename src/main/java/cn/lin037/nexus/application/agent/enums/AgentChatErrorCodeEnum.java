package cn.lin037.nexus.application.agent.enums;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;

/**
 * Agent聊天错误码枚举
 * 定义Agent聊天业务相关的错误码和错误信息
 *
 * @author Lin037
 */
public enum AgentChatErrorCodeEnum implements ResultCodeEnum {

    /**
     * 并发流式对话不被允许
     */
    CONCURRENT_STREAMING_NOT_ALLOWED("AGENT_CHAT_001", "同一会话只能有一个流式对话进行"),

    /**
     * 无效的消息角色
     */
    INVALID_MESSAGE_ROLE("AGENT_CHAT_002", "无效的消息角色"),

    /**
     * 会话不存在
     */
    SESSION_NOT_FOUND("AGENT_CHAT_003", "会话不存在"),

    /**
     * 工具调用执行失败
     */
    TOOL_EXECUTION_FAILED("AGENT_CHAT_004", "工具调用执行失败"),

    /**
     * 流式响应异常
     */
    STREAMING_ERROR("AGENT_CHAT_005", "流式响应异常"),

    /**
     * 会话状态异常
     */
    SESSION_STATUS_ERROR("AGENT_CHAT_006", "会话状态异常"),

    /**
     * 消息不存在
     */
    MESSAGE_NOT_FOUND("AGENT_CHAT_007", "消息不存在"),

    /**
     * 流式初始化失败
     */
    STREAMING_INIT_FAILED("AGENT_CHAT_008", "流式聊天初始化失败"),

    /**
     * AI服务错误
     */
    AI_SERVICE_ERROR("AGENT_CHAT_009", "AI服务发生错误"),

    /**
     * 会话繁忙
     */
    SESSION_BUSY("AGENT_CHAT_010", "会话正在处理中，请稍后再试"),

    ;

    private final String code;
    private final String message;

    AgentChatErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}