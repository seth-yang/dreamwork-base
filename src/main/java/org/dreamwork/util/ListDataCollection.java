package org.dreamwork.util;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-8-7
 * Time: 11:36:21
 */
public class ListDataCollection<T> implements IDataCollection<T> {
    private int pageSize;
    private int pageNo;
    private int totalRows;

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

    public int getTotalPages () {
        if (totalRows == 0) return 1;
        int pages = totalRows / pageSize;
        if (totalRows % pageSize != 0) pages ++;
        return pages;
    }

    public void setData (Collection<T> data) {
        this.data.addAll (data);
    }

    public List<T> getData () {
        return data;
    }

    public void setTotalRows (int totalRows) {
        this.totalRows = totalRows;
    }

    public int size () {
        return data.size ();
    }

    public boolean isEmpty () {
        return data.isEmpty ();
    }

    public boolean contains (T o) {
        return data.contains (o);
    }

    public Iterator<T> iterator () {
        return data.iterator ();
    }

    public Object[] toArray () {
        return data.toArray ();
    }

    public T[] toArray (T[] a) {
        return data.toArray (a);
    }

    public boolean add (T o) {
        return data.add (o);
    }

    public boolean remove (T o) {
        return data.remove (o);
    }

    public boolean containsAll (Collection<?> c) {
        return data.containsAll (c);
    }

    public boolean addAll (Collection<? extends T> c) {
        return data.addAll (c);
    }

    public boolean removeAll (Collection<?> c) {
        return data.removeAll (c);
    }

    public boolean retainAll (Collection<?> c) {
        return data.retainAll (c);
    }

    public void clear () {
        data.clear ();
    }

/*
    public static void main (String[] args) throws Exception {
        IDataCollection<String> data = new ListDataCollection<String> ();
        for (int i = 0; i < 10; i ++) {
            data.add ("Item " + (i + 1));
        }


        for (String s : data) {
            System.out.println (s);
        }
    }
*/
}