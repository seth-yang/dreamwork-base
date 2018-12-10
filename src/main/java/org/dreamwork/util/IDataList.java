package org.dreamwork.util;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-7-28
 * Time: 17:08:47
 */
public interface IDataList<T> extends Serializable {
    void add (T element);
    List<T> getData ();
    void setData (List<T> data);

    void setPageSize (int pageSize);
    int getPageSize ();

    void setPageNo (int pageNo);
    int getPageNo ();

    void setTotalRows (int totalRows);
    int getTotalRows ();
}