package cn.lin037.nexus.common.constant;

/**
 * @author LinSanQi
 */
public class SystemConstant {

    public static final String SYSTEM_NAME = "NEXUS";

    public static final String REDIS_SEPARATOR = ":";

    public static final String DATA_SOURCE_ID_KEY = "dataSourceId";

    public static String buildRedisKey(String... keys) {

        String join = String.join(REDIS_SEPARATOR, keys);
        return SYSTEM_NAME + REDIS_SEPARATOR + join;
    }

    public static String appendToKey(String key, String... parts) {
        String join = String.join(SystemConstant.REDIS_SEPARATOR, parts);
        return key + SystemConstant.REDIS_SEPARATOR + join;
    }
}
