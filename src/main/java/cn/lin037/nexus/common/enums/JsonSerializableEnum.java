package cn.lin037.nexus.common.enums;

/**
 * 枚举序列化专用接口
 * 定义枚举类型的序列化和反序列化标准方法
 *
 * @param <T> 枚举类型
 * @author Lin037
 */
public interface JsonSerializableEnum<T extends Enum<T>> {

    /**
     * 反序列化方法：根据指定格式还原枚举对象
     * 静态方法，由具体枚举类实现
     *
     * @param value     序列化的值
     * @param enumClass 枚举类型
     * @return 对应的枚举对象，如果找不到则返回null
     */
    static <T extends Enum<T> & JsonSerializableEnum<T>> T fromSerializationValue(Object value, Class<T> enumClass) {
        if (value == null || enumClass == null) {
            return null;
        }

        // 遍历所有枚举值，找到匹配的
        for (T enumConstant : enumClass.getEnumConstants()) {
            Object serializationValue = enumConstant.getSerializationValue();
            if (serializationValue != null && serializationValue.equals(value)) {
                return enumConstant;
            }
        }

        return null;
    }

    /**
     * 序列化方法：将枚举对象转换为指定格式
     * 不同枚举类可以自定义序列化格式（如使用name、code等）
     *
     * @return 序列化后的值
     */
    Object getSerializationValue();
}