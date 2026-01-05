package cn.lin037.nexus.infrastructure.common.ai.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @author lin037
 * @date 2024/7/26
 */
@Getter
@AllArgsConstructor
public enum GeneralStatusEnum {

    /**
     * 可用
     */
    ACTIVE(1, "可用"),

    /**
     * 停用
     */
    INACTIVE(2, "停用");

    private final Integer code;

    private final String description;

} 