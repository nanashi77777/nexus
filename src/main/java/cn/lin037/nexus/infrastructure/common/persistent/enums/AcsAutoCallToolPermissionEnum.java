package cn.lin037.nexus.infrastructure.common.persistent.enums;

import lombok.Getter;

@Getter
public enum AcsAutoCallToolPermissionEnum {
    READ_ONLY(1, "只读"),
    WRITE_ONLY(2, "只写"),
    READ_WRITE(3, "可读可写"),
    CLOSED(4, "关闭");

    private final Integer code;
    private final String description;

    AcsAutoCallToolPermissionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AcsAutoCallToolPermissionEnum fromCode(Integer code) {
        for (AcsAutoCallToolPermissionEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
