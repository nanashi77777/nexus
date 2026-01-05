package cn.lin037.nexus.infrastructure.common.task.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用于将异常信息序列化为JSON的标准化结构。
 *
 * @author LinSanQi
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    /**
     * 异常的类名。
     */
    private String exceptionClass;

    /**
     * 异常消息。
     */
    private String message;

    /**
     * 异常的堆栈跟踪信息。
     */
    private String stackTrace;

}
