package org.dreamwork.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by seth.yang on 2016/11/23
 */
public class DateUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
    private static SimpleDateFormat stf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

    public static String formatDate (Date date) {
        return sdf.format (date);
    }

    public static String formateDateTime (Date date) {
        return stf.format (date);
    }

    public static Date parseDate (String expr) {
        try {
            return sdf.parse (expr);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
    }

    public static Date parseDateTime (String expr) {
        try {
            return stf.parse (expr);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
    }

    public static DateUtil fromDate (Date date) {
        return new DateUtil (date);
    }

    public static DateUtil instance () {
        return new DateUtil ();
    }

    public static DateUtil fromTimestamp (long timestamp) {
        return new DateUtil (timestamp);
    }

    private Calendar c;

    public DateUtil () {
        c = Calendar.getInstance (Locale.getDefault ());
    }

    public DateUtil (long timestamp) {
        this ();
        c.setTimeInMillis (timestamp);
    }

    public DateUtil (Date date) {
        this ();
        c.setTime (date);
    }

    public DateUtil (Calendar c) {
        this.c = c;
    }

    public Date getTime () {
        return c.getTime ();
    }

    public long getTimestamp () {
        return c.getTimeInMillis ();
    }

    public DateUtil set (int field, int value) {
        c.set (field, value);
        return this;
    }

    public DateUtil add (int field, int value) {
        c.add (field, value);
        return this;
    }

    public int get (int field) {
        return c.get (field);
    }

    public DateUtil truncateToMinute () {
        return set (Calendar.MILLISECOND, 0).set (Calendar.SECOND, 0);
    }

    public DateUtil truncateToHour () {
        return truncateToMinute ().set (Calendar.MINUTE, 0);
    }

    public DateUtil truncateToDay () {
        return truncateToHour ().set (Calendar.HOUR_OF_DAY, 0);
    }

    public DateUtil truncateToAM () {
        return set (Calendar.HOUR, 12).truncateToHour ();
    }

    public DateUtil truncateToMonth () {
        return truncateToDay ().set (Calendar.DAY_OF_MONTH, 1);
    }

    public DateUtil truncateToYear () {
        return truncateToMonth ().set (Calendar.MONTH, 0);
    }

    public DateUtil nextDay () {
        return add (Calendar.DAY_OF_MONTH, 1).truncateToDay ();
    }

    public DateUtil prevDay () {
        return add (Calendar.DAY_OF_MONTH, -1).truncateToDay ();
    }

    public DateUtil nextMonth () {
        return truncateToMonth ().add (Calendar.MONTH, 1);
    }

    public DateUtil prevMonth () {
        return truncateToMonth ().add (Calendar.MONTH, -1);
    }
}