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
 * @author: chy
 * @Date: 2017/8/13
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
)})
public class PageInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(PageInterceptor.class);

    private static int MAPPED_STATEMENT_INDEX = 0;
    private static int PARAMETER_INDEX = 1;

    public Object intercept(Invocation invocation) throws Throwable {
        Object[] queryArgs = invocation.getArgs();
        MappedStatement ms = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
        Object parameter = queryArgs[PARAMETER_INDEX];

        PageParam page = new PageParam();
        String pageKey = "";// 分页参数前缀
        if (parameter instanceof PageParam) {// 只有分页参数
            page = (PageParam) parameter;
        } else if (parameter instanceof PageParam || parameter instanceof HashMap) {// 2个及以上参数
            HashMap<String, Object> parameterMap = (HashMap<String, Object>) parameter;
            for (String key : parameterMap.keySet()) {
                if (parameterMap.get(key) instanceof PageParam) {
                    page = (PageParam) parameterMap.get(key);
                    pageKey = key + ".";
                    break;
                }
            }
        }

        if (page != null && page.getIndex() != 0 && page.getRows() != Integer.MAX_VALUE) {
            int index = page.getIndex();
            int rows = page.getRows();

            BoundSql boundSql = ms.getBoundSql(parameter);
            int total = this.getCount(ms, parameter, boundSql);
            List list = Collections.EMPTY_LIST;
            if (total > 0) {
                Dialect dialect = new Dialect();
                BoundSql newBoundSql = dialect.getBoungSQL(ms, boundSql, (index - 1) * rows, pageKey);

                MappedStatement newMs = copyFromMappedStatement(ms, new MySqlSource(newBoundSql));
                queryArgs[MAPPED_STATEMENT_INDEX] = newMs;
                list = (List) invocation.proceed();
            }
            return new Page(list, index, rows, total);
        }
        return invocation.proceed();
    }

    /**
     * 获取数据总条数
     * @param mappedStatement
     * @param parameter
     * @param boundSql
     * @return
     * @throws SQLException
     */
    public int getCount(MappedStatement mappedStatement, Object parameter, BoundSql boundSql) throws SQLException {
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

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
    }

    /**
     * 清除sql中的排序
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