package org.dreamwork.misc;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/15
 * Time: 23:30
 */
public interface ISerialNumberSequence {
    void set (long value);
    long get ();
    long next ();
    void save ();
    void restore ();
}