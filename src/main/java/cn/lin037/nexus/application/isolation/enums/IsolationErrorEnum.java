package cn.lin037.nexus.application.isolation.enums;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IsolationErrorEnum implements ResultCodeEnum {

    LEARNING_SPACE_NOT_FOUND("I0001", "学习空间不存在"),
    ;
    private final String code;
    private final String message;
}
