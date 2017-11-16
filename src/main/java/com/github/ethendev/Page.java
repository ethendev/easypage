package com.github.ethendev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * mapper分页结果封装类
 * @author: chy
 * @Date: 2017/8/13
 */
public class Page<E> extends ArrayList<E> implements Serializable {

    private static final long serialVersionUID = 8124541304622348182L;

    /**
     * 页码(从1开始计数)
     */
    private int index;

    /**
     * 数据总数
     */
    private int total;

    /**
     * 每个页面展示数据条数
     */
    private int rows;

    public Page() {
    }

    public Page(Collection<E> collection, int index, int rows, int total) {
        super(collection);
        this.index = index;
        this.total = total;
        this.rows = rows;
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

    public String toString() {
        return "Page{index=" + this.index + ",rows=" + this.rows +
                ", total=" + this.total + ", list=" + super.toString() + "}";
    }
}

