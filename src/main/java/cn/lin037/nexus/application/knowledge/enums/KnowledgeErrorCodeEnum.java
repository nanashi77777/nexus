package cn.lin037.nexus.application.knowledge.enums;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识模块错误码枚举
 *
 * @author LinSanQi
 */
@Getter
@AllArgsConstructor
public enum KnowledgeErrorCodeEnum implements ResultCodeEnum {

    FOLDER_NOT_FOUND("K0001", "文件夹不存在"),
    FOLDER_ALREADY_EXISTS("K0002", "同名文件夹已存在"),
    PARENT_FOLDER_NOT_FOUND("K0003", "父文件夹不存在"),
    TARGET_FOLDER_NOT_FOUND("K0004", "目标文件夹不存在"),
    FOLDER_LEVEL_EXCEEDS_LIMIT("K0006", "文件夹层级超过限制"),
    FOLDER_COUNT_EXCEEDS_LIMIT("K0005", "子文件夹数量超过限制"),
    FOLDER_NOT_EMPTY("K0006", "文件夹不为空，无法删除"),
    KNOWLEDGE_SAVE_FAILED("K0007", "保存知识失败"),
    KNOWLEDGE_NOT_FOUND("K0008", "知识不存在"),
    KNOWLEDGE_UPDATE_FAILED("K0009", "更新知识失败"),
    FORBIDDEN("K0007", "无权操作"),
    RELATION_NOT_FOUND("K0010", "关系不存在"),

    // 图谱相关错误码
    GRAPH_NOT_FOUND("K1001", "知识图谱不存在"),
    GRAPH_NODE_NOT_FOUND("K1002", "图谱节点不存在"),
    GRAPH_EDGE_NOT_FOUND("K1003", "图谱边不存在"),
    MATERIALIZE_NODE_FAILED("K1004", "实体化节点失败"),
    MATERIALIZE_EDGE_FAILED("K1005", "实体化边失败"),
    CANNOT_MATERIALIZE_EDGE("K1006", "只有连接两个投影节点的边才能实体化"),
    GRAPH_OPERATION_FAILED("K1007", "图谱操作失败"),

    AI_GENERATION_FAILED("K2001", "AI生成知识失败"),
    RESOURCE_NOT_FOUND("K2002", "资源不存在"),
    INVALID_KNOWLEDGE_GENERATION_TYPE("K2003", "无效的知识生成类型");

    private final String code;
    private final String message;
} 