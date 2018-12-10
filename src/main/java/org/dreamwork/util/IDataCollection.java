package org.dreamwork.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-8-7
 * Time: 11:34:52
 */
public interface IDataCollection<T> extends Serializable {
    int getPageSize ();
    int getPageNo ();
    int getTotalRows ();
    int getTotalPages ();
    void setData (Collection<T> data);
    List<T> getData ();
}