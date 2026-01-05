package cn.lin037.nexus.web.rest.v1.agent.vo;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;
import cn.lin037.nexus.common.model.vo.ResultVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI流式响应VO类
 * 用于封装AI流式响应的各种事件类型和数据
 *
 * @author Lin037
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamingChatVO {

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 文本内容（用于content事件类型）
     */
    private String content;

    /**
     * 创建思考内容片段事件
     *
     * @param content 思考内容片段
     * @return 思考内容片段事件VO
     */
    public static ResultVO<StreamingChatVO> thinkingContent(String content) {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.THINKING_CONTENT)
                .content(content)
                .build());
    }

    /**
     * 创建思考完成事件
     *
     * @return 思考完成事件VO
     */
    public static ResultVO<StreamingChatVO> thinkingComplete() {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.THINKING_COMPLETE)
                .build());
    }

    /**
     * 创建内容片段事件
     *
     * @param content 内容片段
     * @return 内容片段事件VO
     */
    public static ResultVO<StreamingChatVO> content(String content) {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.CONTENT)
                .content(content)
                .build());
    }

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 思考内容片段事件
         */
        THINKING_CONTENT,

        /**
         * 思考完成事件
         */
        THINKING_COMPLETE,

        /**
         * 内容片段事件
         */
        CONTENT,

        /**
         * 工具列表事件 - 返回完整的工具列表给用户
         */
        TOOL_LIST,

        /**
         * 工具调用开始事件
         */
        TOOL_CALL_START,

        /**
         * 工具执行延迟事件 - 工具执行被延迟并关闭当前对话流
         */
        TOOL_EXECUTION_DEFERRED,

        /**
         * 工具调用结果事件
         */
        TOOL_CALL_RESULT,

        /**
         * 完成事件
         */
        DONE,

        /**
         * 取消事件
         */
        CANCELLED,

        /**
         * 错误事件
         */
        ERROR
    }

    public static ResultVO<StreamingChatVO> toolList(String content) {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.TOOL_LIST)
                .content(content)
                .build());
    }

    public static ResultVO<StreamingChatVO> toolCallStart(String content) {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.TOOL_CALL_START)
                .content(content)
                .build());
    }

    public static ResultVO<StreamingChatVO> toolExecutionDeferred() {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.TOOL_EXECUTION_DEFERRED)
                .build());
    }

    public static ResultVO<StreamingChatVO> toolCallResult(String content) {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.TOOL_CALL_RESULT)
                .content(content)
                .build());
    }

    public static ResultVO<StreamingChatVO> done() {
        return ResultVO.success(StreamingChatVO.builder()
                .eventType(EventType.DONE)
                .build());
    }

    public static ResultVO<StreamingChatVO> error(ResultCodeEnum code) {
        return ResultVO.error(code.getCode(), code.getMessage());
    }

    public static ResultVO<StreamingChatVO> error(ResultCodeEnum code, String detailMessage) {
        return ResultVO.error(code.getCode(), code.getMessage() + "：" + detailMessage);
    }
}