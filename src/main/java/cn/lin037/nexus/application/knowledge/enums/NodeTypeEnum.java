package cn.lin037.nexus.application.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图谱节点类型枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum NodeTypeEnum {
    /**
     * 实体投影
     */
    PROJECTION(1, "实体投影"),

    /**
     * 虚体
     */
    VIRTUAL(2, "虚体");

    private final Integer code;
    private final String description;
} 