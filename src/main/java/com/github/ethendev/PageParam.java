package com.github.ethendev;

/**
 * pagination parameters
 * @author: chy
 * @Date: 2017/8/13
 */
public class PageParam {

    /**
     * page number(count from 1)
     */
    private int index = 0;

    /**
     * number of data displayed per page
     */
    private int rows = Integer.MAX_VALUE;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getOffset() {
        if (index > 0) {
            return (index - 1) * rows;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "PageParam{" +
                "index=" + index +
                ", rows=" + rows +
                '}';
    }
}
