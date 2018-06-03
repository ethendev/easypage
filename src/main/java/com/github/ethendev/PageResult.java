package com.github.ethendev;

import java.io.Serializable;
import java.util.List;

/**
 * Encapsulation of pagination results
 * @author: chy
 * @Date: 2017/8/19
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 4387523960534307382L;

    /**
     * page number(count from 1)
     */
    private int index;

    /**
     * total number of data
     */
    private int total;

    /**
     * number of data displayed per page
     */
    private int rows;

    /**
     * result
     */
    private List<T> list;

    public PageResult(Page<T> page) {
        this.list = page;
        this.index = page.getIndex();
        this.total = page.getTotal();
        this.rows = page.getRows();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "index=" + index +
                ", total=" + total +
                ", rows=" + rows +
                ", list=" + list +
                '}';
    }
}
