package org.dreamwork.util;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-7-28
 * Time: 17:10:10
 */
public class DataList<T> implements IDataList<T> {
    private int pageSize, pageNo, totalRows;
    private List<T> data = new ArrayList<T> ();

    public int getPageSize () {
        return pageSize;
    }

    public void setPageSize (int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNo () {
        return pageNo;
    }

    public void setPageNo (int pageNo) {
        this.pageNo = pageNo;
    }

    public int getTotalRows () {
        return totalRows;
    }

    public void setTotalRows (int totalRows) {
        this.totalRows = totalRows;
    }

    public List<T> getData () {
        return data;
    }

    public void setData (List<T> data) {
        this.data = data;
    }

    public void add (T element) {
        data.add (element);
    }
}