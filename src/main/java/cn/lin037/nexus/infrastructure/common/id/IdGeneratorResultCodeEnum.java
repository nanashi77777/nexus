package cn.lin037.nexus.infrastructure.common.id;

import cn.lin037.nexus.common.constant.enums.result.ResultCodeEnum;

/**
 * ID生成器错误码枚举
 * 定义ID生成相关的错误码
 *
 * @author LinSanQi
 */
public enum IdGeneratorResultCodeEnum implements ResultCodeEnum {

    // 通用ID生成错误 (ID_607xxx)
    ID_GENERATION_ERROR("ID_607000", "ID生成失败"),

    // 雪花算法相关错误 (ID_6071xx)
    SNOWFLAKE_CLOCK_BACKWARDS("ID_607100", "时钟回拨错误"),
    SNOWFLAKE_SEQUENCE_OVERFLOW("ID_607101", "序列号溢出"),
    SNOWFLAKE_INVALID_WORKER_ID("ID_607102", "无效的工作机器ID"),
    SNOWFLAKE_INVALID_DATACENTER_ID("ID_607103", "无效的数据中心ID"),
    SNOWFLAKE_INITIALIZATION_ERROR("ID_607104", "雪花算法初始化错误");

    private final String code;
    private final String message;

    IdGeneratorResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
