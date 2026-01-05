package cn.lin037.nexus.infrastructure.common.persistent.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用于 PostgreSQL 的 JSONB 和 Java 的 String 之间的类型转换
 *
 * @author LinSanQi
 */
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            // 改进: 使用"null"表示null值，这是有效的JSON
            jsonObject.setValue(parameter != null ? parameter : "null");

            // 使用 setObject 即可，无需强制转型 PgPreparedStatement
            ps.setObject(i, jsonObject);
        } catch (SQLException e) {
            throw new SQLException("Error setting JSONB parameter at position " + i + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from column " + columnName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return rs.getString(columnIndex);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from column index " + columnIndex + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return cs.getString(columnIndex);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from callable statement at index " + columnIndex + ": " + e.getMessage(), e);
        }
    }
}
