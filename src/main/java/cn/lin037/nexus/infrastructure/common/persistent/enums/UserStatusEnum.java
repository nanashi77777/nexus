package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author GitHub Copilot
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    /**
     * 待验证
     */
    PENDING(0, "待验证"),
    /**
     * 正常
     */
    ACTIVE(1, "正常"),
    /**
     * 禁用
     */
    BANNED(2, "禁用");

    private final int code;
    private final String description;
}

