package cn.lin037.nexus.infrastructure.common.persistent.handler;

import cn.lin037.nexus.infrastructure.common.persistent.entity.dto.NodeStyleConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 处理NodeStyleConfig对象与JSONB之间的转换
 */
@MappedTypes({NodeStyleConfig.class})
@MappedJdbcTypes({JdbcType.OTHER})
public class NodeStyleConfigTypeHandler extends BaseTypeHandler<NodeStyleConfig> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, NodeStyleConfig parameter, JdbcType jdbcType) throws SQLException {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(objectMapper.writeValueAsString(parameter));

            ps.setObject(i, jsonObject);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error setting JSONB parameter at position " + i + ": " + e.getMessage(), e);
        }
    }

    @Override
    public NodeStyleConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
        try {
            String json = rs.getString(columnName);
            return parseJson(json);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from column " + columnName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public NodeStyleConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            String json = rs.getString(columnIndex);
            return parseJson(json);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from column index " + columnIndex + ": " + e.getMessage(), e);
        }
    }

    @Override
    public NodeStyleConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            String json = cs.getString(columnIndex);
            return parseJson(json);
        } catch (SQLException e) {
            throw new SQLException("Error reading JSONB from callable statement at index " + columnIndex + ": " + e.getMessage(), e);
        }
    }

    private NodeStyleConfig parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, NodeStyleConfig.class);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error parsing JSON to NodeStyleConfig object.", e);
        }
    }
}