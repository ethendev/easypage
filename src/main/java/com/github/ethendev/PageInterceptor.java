package com.github.ethendev;

import com.github.ethendev.dialect.Dialect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pagination interceptor
 * @author: chy
 * @Date: 2017/8/13
 */
@Intercepts({@Signature(
        type = Executor.class,// the object that needs to be intercepted
        method = "query",// the method that needs to be intercepted
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
)})
public class PageInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(PageInterceptor.class);

    /**
     * Whether paging is required
     */
    private static boolean pageEnabled = true;

    private static int MAPPED_STATEMENT_INDEX = 0;
    private static int PARAMETER_INDEX = 1;

    /**
     * Replacing the intercepted object method
     * @param invocation responsibility chain object
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if(pageEnabled){
            Object[] queryArgs = invocation.getArgs();
            MappedStatement ms = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
            Object parameter = queryArgs[PARAMETER_INDEX];

            PageParam page = new PageParam();
            String pageKey = "";// the key of PageParam when parameter type is HashMap
            if (parameter instanceof PageParam) {// only one parameter
                page = (PageParam) parameter;
            } else if (parameter instanceof HashMap) {// 2 or more parameters
                HashMap<String, Object> parameterMap = (HashMap<String, Object>) parameter;
                for (String key : parameterMap.keySet()) {
                    if (parameterMap.get(key) instanceof PageParam) {
                        page = (PageParam) parameterMap.get(key);
                        pageKey = key + ".";
                        break;
                    }
                }
            }

            // To determine if paging is needed, paging when the parameter is not the default
            if (page != null && page.getRows() != Integer.MAX_VALUE) {
                int index = page.getIndex();
                int rows = page.getRows();

                BoundSql boundSql = ms.getBoundSql(parameter);
                int total = this.getCount(ms, parameter, boundSql);
                List list = Collections.EMPTY_LIST;
                if (total > 0) {
                    Dialect dialect = new Dialect();
                    BoundSql newBoundSql = dialect.getBoungSQL(ms, boundSql, page.getOffset(), pageKey);

                    MappedStatement newMs = copyFromMappedStatement(ms, new MySqlSource(newBoundSql));
                    queryArgs[MAPPED_STATEMENT_INDEX] = newMs;
                    list = (List) invocation.proceed();
                }
                return new Page(list, index, rows, total);
            }
        }
        return invocation.proceed();
    }

    /**
     * Generate proxy for intercepted objects
     * @param target the proxied object
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * Get plugin configuration properties
     * @param properties Mybatis configuration parameters
     */
    @Override
    public void setProperties(Properties properties) {
        String enabled = properties.getProperty("pageEnabled");
        pageEnabled = enabled != null ? Boolean.parseBoolean(enabled) : true;
    }

    /**
     * Get the total number of data
     * @param mappedStatement
     * @param parameter
     * @param boundSql
     * @return
     * @throws SQLException
     */
    private int getCount(MappedStatement mappedStatement, Object parameter, BoundSql boundSql) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select count(1) from (");
        sqlBuilder.append(clearOrderBy(boundSql.getSql())).append(") tmp");

        Connection connection;
        PreparedStatement countStmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
            countStmt = connection.prepareStatement(sqlBuilder.toString());
            DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, parameter, boundSql);
            handler.setParameters(countStmt);
            rs = countStmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            logger.debug("==> Preparing: {}", sqlBuilder.toString());
            logger.debug("<== Total: {}", count);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                if (countStmt != null) {
                    countStmt.close();
                }
            }
        }
        return count;
    }

    /**
     * Clear the sort in sql
     * @param sql
     * @return
     */
    private String clearOrderBy(String sql) {
        Pattern pattern = Pattern.compile("select.*(?=(?i)order[\\s\\t\\r\\n]+(?i)by[\\s\\t\\r\\n].*)");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            return matcher.group();
        }
        return sql;
    }

    /**
     * create a new MappedStatement object
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(),
                newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());

        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuffer keyProperties = new StringBuffer();
            String[] keyProp;
            int length = (keyProp = ms.getKeyProperties()).length;

            for (int i = 0; i < length; ++i) {
                String keyProperty = keyProp[i];
                keyProperties.append(keyProperty).append(",");
            }

            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }

        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

}
