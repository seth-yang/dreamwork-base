package org.dreamwork.util.sort;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-29
 * Time: 10:58:36
 */
public class SelectionSort<E extends Comparable<E>> extends AbstractSorter<E>{
    public void sort () {
        for (int i = 0; i < elements.length; i ++) {
            int index = i;
            E min = elements [i];
            for (int j = i + 1; j < elements.length; j ++) {
                if (min.compareTo (elements [j]) > 0) {
                    index = j;
                    min = elements [j];
                }
            }
            if (index != i) swap (i, index);
        }
    }
}
