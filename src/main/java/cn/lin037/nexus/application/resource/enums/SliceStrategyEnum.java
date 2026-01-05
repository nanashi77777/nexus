package cn.lin037.nexus.application.resource.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 资源分片策略枚举
 *
 * @author LinSanQi
 */
@Getter
public enum SliceStrategyEnum {
    RECURSIVE_BY_TOKEN(0, "按Token递归拆分"),
    BY_SENTENCE(1, "按行拆分"),
    BY_MARKDOWN(2, "按Markdown格式拆分");

    private final int code;
    private final String description;

    SliceStrategyEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(e -> Objects.equals(e.getCode(), code));
    }

    public static SliceStrategyEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.getCode(), code))
                .findFirst()
                .orElse(null);
    }
} 