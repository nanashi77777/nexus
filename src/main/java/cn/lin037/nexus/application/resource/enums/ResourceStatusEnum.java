package cn.lin037.nexus.application.resource.enums;

import lombok.Getter;

/**
 * 资源状态枚举
 *
 * @author LinSanQi
 */
@Getter
public enum ResourceStatusEnum {
    PENDING_PARSE(0, "待解析"),
    PARSING(1, "解析中"),
    PARSE_COMPLETED(2, "解析完成"),
    PARSE_FAILED(3, "解析失败");

    private final int code;
    private final String description;

    ResourceStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ResourceStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResourceStatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static ResourceStatusEnum[] getAvailableStatus() {
        return new ResourceStatusEnum[]{PENDING_PARSE, PARSING, PARSE_COMPLETED};
    }
} 