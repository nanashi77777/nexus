package cn.lin037.nexus.application.resource.enums;

import lombok.Getter;

/**
 * 资源来源类型枚举
 *
 * @author LinSanQi
 */
@Getter
public enum ResourceSourceTypeEnum {
    UPLOAD(0, "用户上传"),
    LINK(1, "链接导入"),
    AI_GENERATED(2, "AI生成"),
    MANUAL(3, "手动创建");

    private final int code;
    private final String description;

    ResourceSourceTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取对应的枚举值
     *
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码无效
     */
    public static ResourceSourceTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResourceSourceTypeEnum sourceType : values()) {
            if (sourceType.code == code) {
                return sourceType;
            }
        }
        return null;
    }
} 