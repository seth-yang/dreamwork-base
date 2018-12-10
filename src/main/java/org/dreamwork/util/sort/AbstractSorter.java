package org.dreamwork.util.sort;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-9-28
 * Time: 16:47:12
 */
public abstract class AbstractSorter<E extends Comparable<E>> implements ISorter<E> {
    protected Animation runner;
    protected E[] elements;

    public void setElements (E... elements) {
        this.elements = elements;
    }

    public void setRunner (Animation runner) {
        this.runner = runner;
    }

    protected void swap (int i, int j) {
        if (runner != null) {
            runner.setFrom (i);
            runner.setPovit (j);
            runner.run ();
            try {
                Thread.sleep (5);
            } catch (InterruptedException e1) {
                e1.printStackTrace ();
            }
        }
        E e = elements [i];
        elements[i] = elements [j];
        elements[j] = e;
        if (runner != null) {
            runner.run ();
            try {
                Thread.sleep (5);
            } catch (InterruptedException e1) {
                e1.printStackTrace ();
            }
        }
    }
}
