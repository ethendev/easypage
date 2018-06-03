package com.github.ethendev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Mapper paging results encapsulation class
 * @author: chy
 * @Date: 2017/8/13
 */
public class Page<E> extends ArrayList<E> implements Serializable {

    private static final long serialVersionUID = 8124541304622348182L;

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

