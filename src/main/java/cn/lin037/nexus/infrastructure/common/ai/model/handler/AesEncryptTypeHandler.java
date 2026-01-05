package cn.lin037.nexus.infrastructure.common.ai.model.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 使用 Hutool 实现的 MyBatis TypeHandler，用于 AES 加密和解密字符串
 */
public class AesEncryptTypeHandler extends BaseTypeHandler<String> {

    // 加密密钥（建议通过配置中心或环境变量注入）
    private static final String SECRET_KEY = "gHCd8BMyYL3xxK/ejLdcyX3BlD0Ttf3O6DgVWtOXZL0=";

    // 初始化 AES 对象
    private static final AES AES_CIPHER = SecureUtil.aes(SecureUtil.decode(SECRET_KEY));

    /**
     * 设置参数时加密数据
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        String encryptedValue = AES_CIPHER.encryptHex(parameter);
        ps.setString(i, encryptedValue);
    }

    /**
     * 从结果集中获取数据并解密
     */
    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String encryptedValue = rs.getString(columnName);
        return decrypt(encryptedValue);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String encryptedValue = rs.getString(columnIndex);
        return decrypt(encryptedValue);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String encryptedValue = cs.getString(columnIndex);
        return decrypt(encryptedValue);
    }

    /**
     * 解密方法
     */
    private String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        return AES_CIPHER.decryptStr(cipherText);
    }
}
