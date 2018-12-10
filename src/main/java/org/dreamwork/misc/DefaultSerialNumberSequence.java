package org.dreamwork.misc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/15
 * Time: 23:58
 */
public class DefaultSerialNumberSequence implements ISerialNumberSequence {
    private AtomicLong atomic = new AtomicLong (0);
    private int step = 1;

    public DefaultSerialNumberSequence () {}

    public DefaultSerialNumberSequence (int step) {
        this.step = step;
    }

    @Override
    public void set (long value) {
        atomic.set (value);
    }

    @Override
    public long get () {
        return atomic.get ();
    }

    @Override
    public long next () {
        return atomic.getAndAdd (step);
    }

    @Override
    public void save () {
    }

    @Override
    public void restore () {
    }
}
