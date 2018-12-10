package org.dreamwork.util.sort;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 11:20:26
 */
public class QuickSort<E extends Comparable<E>> extends AbstractSorter<E>{
    private int partition (int begin, int end) {
        int index = (begin + end) / 2;
        E e = elements [index];
        swap (index, end);
        for (int i = index = begin; i < end; ++ i) {
            if (elements [i].compareTo (e) <= 0) {
                swap (index ++, i);
            }
        }
        swap (index, end);
        return index;
    }

    private void sort (int begin, int end) {
        if (end > begin) {
            int index = partition (begin, end);
            sort (begin, index - 1);
            sort (index + 1, end);
        }
    }

    public void sort () {
        sort (0, elements.length - 1);
    }
}