package cn.lin037.nexus.infrastructure.common.persistent.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@MappedJdbcTypes(JdbcType.ARRAY)
@MappedTypes(List.class)
public class LongListTypeHandler extends BaseTypeHandler<List<Long>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        Array sqlArray = ps.getConnection().createArrayOf("bigint", parameter.toArray());
        ps.setArray(i, sqlArray);
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getListFromSqlArray(rs.getArray(columnName));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getListFromSqlArray(rs.getArray(columnIndex));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getListFromSqlArray(cs.getArray(columnIndex));
    }

    private List<Long> getListFromSqlArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return Collections.emptyList();
        }
        try {
            return Arrays.asList((Long[]) sqlArray.getArray());
        } finally {
            sqlArray.free();
        }
    }

}

