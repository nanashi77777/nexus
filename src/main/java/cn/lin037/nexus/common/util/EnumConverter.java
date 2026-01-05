package cn.lin037.nexus.common.util;

import cn.hutool.core.convert.Converter;
import cn.lin037.nexus.common.enums.JsonSerializableEnum;

/**
 * 枚举转换器
 * 实现Hutool的Converter接口，支持实现了JsonSerializableEnum接口的枚举类型转换
 *
 * @param <T> 枚举类型
 * @author Lin037
 */
public class EnumConverter<T extends Enum<T> & JsonSerializableEnum<T>> implements Converter<T> {

    private final Class<T> enumClass;

    public EnumConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T convert(Object value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        // 如果已经是目标枚举类型，直接返回
        if (enumClass.isInstance(value)) {
            return enumClass.cast(value);
        }

        // 使用接口的反序列化方法
        T result = JsonSerializableEnum.fromSerializationValue(value, enumClass);

        // 如果找不到匹配的枚举值，尝试按枚举名称查找
        if (result == null && value instanceof String) {
            try {
                result = Enum.valueOf(enumClass, (String) value);
            } catch (IllegalArgumentException e) {
                // 忽略异常，返回默认值
            }
        }

        return result != null ? result : defaultValue;
    }
}