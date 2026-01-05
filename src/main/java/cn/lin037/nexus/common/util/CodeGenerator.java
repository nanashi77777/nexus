package cn.lin037.nexus.common.util;

import java.security.SecureRandom;

/**
 * 唯一码生成工具
 *
 * @author LinSanQi
 */
public class CodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // 随机数生成器
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定长度的随机码
     *
     * @param length 码长度
     * @return 随机码
     */
    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    public static String generateNumberCode(int length) {
        StringBuilder numberCode = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            numberCode.append(RANDOM.nextInt(10));
        }
        return numberCode.toString();
    }
} 