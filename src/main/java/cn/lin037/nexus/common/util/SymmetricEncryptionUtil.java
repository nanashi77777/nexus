package cn.lin037.nexus.common.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.AES;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 对称加密工具类
 * 用于加密敏感信息，如API Key等
 *
 * @author lin037
 */
@Component
public class SymmetricEncryptionUtil {

    // TODO: 将密钥移动到更安全的配置中心或环境变量中
    @Value("${nexus.security.secret-key:gN4hT2kLpA7vSjQdF8xYmWbZcE9uJ5rR}")
    private String secretKey;

    private AES aes;

    @PostConstruct
    public void init() {
        // 使用256位密钥，需要32个ASCII字符
        if (secretKey == null || secretKey.length() != 32) {
            throw new IllegalArgumentException("`nexus.security.secret-key` 必须是一个32字节(32个ASCII字符)的字符串");
        }
        // IV (Initialization Vector) 需要16个字节
        byte[] iv = secretKey.substring(0, 16).getBytes(CharsetUtil.CHARSET_UTF_8);
        aes = new AES("CBC", "PKCS5Padding", secretKey.getBytes(CharsetUtil.CHARSET_UTF_8), iv);
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return 加密后的内容
     */
    public String encrypt(String content) {
        if (content == null) {
            return null;
        }
        return aes.encryptBase64(content);
    }

    /**
     * 解密
     *
     * @param encryptedContent 加密过的内容
     * @return 解密后的内容
     */
    public String decrypt(String encryptedContent) {
        if (encryptedContent == null) {
            return null;
        }
        return aes.decryptStr(encryptedContent);
    }
} 