package cn.lin037.nexus.common.enums;

/**
 * JSON Schema类型枚举
 * 定义所有支持的JSON Schema类型，用于类型安全的序列化和反序列化
 *
 * @author Lin037
 */
public enum JsonSchemaTypeEnum implements JsonSerializableEnum<JsonSchemaTypeEnum> {

    /**
     * 对象类型
     */
    OBJECT("object"),

    /**
     * 字符串类型
     */
    STRING("string"),

    /**
     * 整数类型
     */
    INTEGER("integer"),

    /**
     * 数字类型（包含小数）
     */
    NUMBER("number"),

    /**
     * 布尔类型
     */
    BOOLEAN("boolean"),

    /**
     * 数组类型
     */
    ARRAY("array"),

    /**
     * 空值类型
     */
    NULL("null"),

    /**
     * 枚举类型
     */
    ENUM("enum");

    private final String value;

    JsonSchemaTypeEnum(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取对应的枚举
     *
     * @param value 字符串值
     * @return 对应的枚举，如果找不到则返回null
     */
    public static JsonSchemaTypeEnum fromValue(String value) {
        return JsonSerializableEnum.fromSerializationValue(value, JsonSchemaTypeEnum.class);
    }

    public String getValue() {
        return value;
    }

    @Override
    public Object getSerializationValue() {
        return value;
    }
}