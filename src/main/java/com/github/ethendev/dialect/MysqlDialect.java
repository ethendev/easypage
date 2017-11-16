package com.github.ethendev.dialect;

/**
 * @author: chy
 * @Date: 2017/8/21
 */
public class MysqlDialect {

    public static String getLimitString(String sql, int offset) {
        StringBuffer buffer = new StringBuffer(sql.length() + 20);
        buffer.append(sql);
        if (offset > 0) {
            buffer.append(" limit ?, ?");
        } else {
            buffer.append(" limit ?");
        }
        return buffer.toString();
    }

}
