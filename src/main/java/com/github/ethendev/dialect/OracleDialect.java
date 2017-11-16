package com.github.ethendev.dialect;

/**
 * @author: chy
 * @Date: 2017/8/21
 */
public class OracleDialect {

    public static String getLimitString(String sql, int offset) {
        StringBuffer buffer = new StringBuffer(sql.length() + 100);
        if (offset > 0) {
            buffer.append("select * from ( select row_.*, rownum rownum_ from ( ");
            buffer.append(sql);
            buffer.append(" ) row_ ) where rownum_ <= ? and rownum_ > ?");
        } else {
            buffer.append("select * from ( ");
            buffer.append(sql);
            buffer.append(" ) where rownum <= ?");
        }
        return buffer.toString();
    }

}
