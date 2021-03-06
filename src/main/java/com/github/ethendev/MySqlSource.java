package com.github.ethendev;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;

/**
 * Encapsulating the BoundSql class
 * @author: chy
 * @Date: 2017/11/1
 */
public class MySqlSource implements SqlSource {

    private BoundSql boundSql;

    public MySqlSource(BoundSql boundSql) {
        this.boundSql = boundSql;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        return boundSql;
    }
}
