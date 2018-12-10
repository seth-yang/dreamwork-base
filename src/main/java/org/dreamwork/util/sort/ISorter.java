package org.dreamwork.util.sort;
/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 16:46:30
 */
public interface ISorter<E extends Comparable<E>> {
    void sort ();
    void setElements (E... elements);
    void setRunner (Animation runner);
}