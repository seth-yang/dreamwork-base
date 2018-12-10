package org.dreamwork.util.cal;

import org.dreamwork.util.DateUtil;

import java.util.Calendar;

/**
 * Created by game on 2017/9/25
 */
public final class Date {
    public static int deltaByDay (Date a, Date b) {
        long millis = deltaByMilliseconds (a, b);
        return (int) millis / 1000        // seconds
                            / 60          // minutes
                            / 60          // hours
                            / 24          // days
        ;
    }

    public static long deltaByMilliseconds (Date a, Date b) {
        if (a == null || b == null)
            throw new NullPointerException ();

        long ms_a = a.c.getTimestamp (),
             ms_b = b.c.getTimestamp ();
        return ms_a - ms_b;
    }

    public final int year, month, date;
    private DateUtil c;

    public Date () {
        c = new DateUtil ().truncateToDay ();
        year  = c.get (Calendar.YEAR);
        month = c.get (Calendar.MONTH) + 1;
        date  = c.get (Calendar.DATE);
    }

    public Date (int year, int month, int date) {
        c = new DateUtil ()
            .set (Calendar.YEAR, year)
            .set (Calendar.MONTH, month - 1)
            .set (Calendar.DATE, date);
        this.year  = c.get (Calendar.YEAR);
        this.month = c.get (Calendar.MONTH) + 1;
        this.date  = c.get (Calendar.DATE);
    }

    public int deltaByDay (Date other) {
        return deltaByDay (this, other);
    }

    public long deltaByMilliseconds (Date other) {
        return deltaByMilliseconds (this, other);
    }

    public java.util.Date getTime () {
        return c.getTime ();
    }
}