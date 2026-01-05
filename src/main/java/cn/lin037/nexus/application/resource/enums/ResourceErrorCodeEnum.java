package cn.lin037.nexus.application.resource.enums;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;
import lombok.Getter;

/**
 * 资源模块错误码枚举
 *
 * @author LinSanQi
 */
@Getter
public enum ResourceErrorCodeEnum implements ResultCodeEnum {

    RESOURCE_NOT_FOUND("RESOURCE_404001", "资源不存在"),
    RESOURCE_CREATION_FAILED("RESOURCE_500001", "资源创建失败"),
    RESOURCE_TITLE_EXISTS("RESOURCE_400001", "资源标题已存在"),
    CHUNK_NOT_FOUND("RESOURCE_404003", "资源分片不存在"),
    NO_PERMISSION_TO_OPERATE("RESOURCE_403001", "无权操作"),
    CHUNK_ALREADY_VECTORIZED("RESOURCE_400010", "分片已向量化"),

    FILE_UPLOAD_FAILED("RESOURCE_500002", "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED("RESOURCE_400002", "不支持的文件类型"),
    FILE_EMPTY("RESOURCE_400003", "上传文件不能为空"),
    INVALID_SLICE_STRATEGY("RESOURCE_400004", "无效的分片策略"),

    RESOURCE_PARSING_FAILED("RESOURCE_500003", "资源解析失败"),

    TASK_CANCELLED("RESOURCE_400005", "任务已被取消"),
    INVALID_SOURCE_URI("RESOURCE_400006", "资源来源URI无效"),
    UPLOAD_SOURCE_URI_EMPTY("RESOURCE_400007", "上传资源的存储路径不能为空"),
    LINK_SOURCE_URI_EMPTY("RESOURCE_400008", "链接资源的URL不能为空"),

    AI_GENERATE_FAILED("RESOURCE_400009", "AI生成任务失败"),
    ;

    private final String code;
    private final String message;

    ResourceErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
