package com.github.ethendev.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: chy
 * @Date: 2017/8/21
 */
public class Dialect {

    /**
     * Set parameters according to database type
     * @param ms
     * @param boundSql
     * @param offset
     * @param pageKey
     * @return
     * @throws SQLException
     */
    public BoundSql getBoungSQL(MappedStatement ms, BoundSql boundSql, int offset, String pageKey) throws SQLException {
        DatabaseMetaData dbmd = ms.getConfiguration().getEnvironment().getDataSource().getConnection().getMetaData();
        DBType dbType = DBType.valueOf(dbmd.getDatabaseProductName());

        String sql;
        switch (dbType) {
            case MySQL:
                sql = MysqlDialect.getLimitString(boundSql.getSql(), offset);
                break;
            case Oracle:
                sql = OracleDialect.getLimitString(boundSql.getSql(), offset);
                break;
            default:
                throw new IllegalArgumentException("Not supported dialect:" + dbType);
        }

        // copy a new list
        List<ParameterMapping> list = new ArrayList<>(boundSql.getParameterMappings());
        if (offset > 0) {
            list.add(new ParameterMapping.Builder(ms.getConfiguration(), pageKey + "offset", Integer.class).build());
            list.add(new ParameterMapping.Builder(ms.getConfiguration(), pageKey + "rows", Integer.class).build());
        } else {
            list.add(new ParameterMapping.Builder(ms.getConfiguration(), pageKey + "rows", Integer.class).build());
        }

        BoundSql newboundSql = new BoundSql(ms.getConfiguration(), sql, list, boundSql.getParameterObject());
        return newboundSql;
    }

}
