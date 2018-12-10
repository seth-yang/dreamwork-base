package org.dreamwork.util.sort;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 18:25:39
 */
public class BubbleSort<E extends Comparable<E>> extends AbstractSorter<E> {
    public void sort () {
        int length = elements.length;
        for (int i = length - 1; i >= 0; i --) {
            for (int j = 1; j <= i - 1; j ++) {
                if (elements [j].compareTo (elements [j + 1]) > 0)
                    swap (j, j + 1);
            }
        }
    }
}