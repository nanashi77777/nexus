package cn.lin037.nexus.infrastructure.common.persistent.handler;

import cn.lin037.nexus.common.util.SymmetricEncryptionUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 加密字符串类型处理器
 * 用于对数据库中的敏感字符串（如API Key）进行自动加解密
 *
 * @author lin037
 */
@Slf4j
@Component
@MappedTypes(String.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    private static SymmetricEncryptionUtil encryptionUtil;

    private final SymmetricEncryptionUtil util;

    public EncryptedStringTypeHandler(SymmetricEncryptionUtil util) {
        this.util = util;
    }

    @PostConstruct
    public void init() {
        EncryptedStringTypeHandler.encryptionUtil = this.util;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encryptionUtil.encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? encryptionUtil.decrypt(value) : null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? encryptionUtil.decrypt(value) : null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? encryptionUtil.decrypt(value) : null;
    }
} 