package cn.lin037.nexus.common.util;

import cn.hutool.core.convert.Converter;
import cn.lin037.nexus.common.enums.JsonSerializableEnum;

/**
 * 枚举到字符串转换器
 * 实现Hutool的Converter接口，将实现了JsonSerializableEnum接口的枚举类型转换为字符串
 *
 * @author Lin037
 */
public class EnumToStringConverter implements Converter<String> {

    @Override
    public String convert(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        // 如果已经是字符串类型，直接返回
        if (value instanceof String) {
            return (String) value;
        }

        // 如果是实现了JsonSerializableEnum接口的枚举
        if (value instanceof JsonSerializableEnum) {
            Object serializationValue = ((JsonSerializableEnum<?>) value).getSerializationValue();
            return serializationValue != null ? serializationValue.toString() : defaultValue;
        }

        // 如果是普通枚举，返回枚举名称
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        // 其他情况，转换为字符串
        return value.toString();
    }
}