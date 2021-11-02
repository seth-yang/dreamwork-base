package org.dreamwork.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by seth.yang on 2016/11/23
 */
@SuppressWarnings ("unused")
public class DateUtil {
    private final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
    private final SimpleDateFormat stf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

    /**
     * 按默认的 {@code yyyy-MM-dd} 模式格式化日期
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate (Date date) {
        return new SimpleDateFormat ("yyyy-MM-dd").format (date);
    }

    /**
     * 按默认的 {@code yyyy-MM-dd HH:mm:ss} 模式格式化日期
     * @param date 时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime (Date date) {
        return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").format (date);
    }

    /**
     * 按默认的 {@code yyyy-MM-dd} 模式来解析日期
     * @param expr 表达式
     * @return 解析后的日期对象
     */
    public static Date parseDate (String expr) {
        try {
            return new SimpleDateFormat ("yyyy-MM-dd").parse (expr);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * 按默认的 {@code yyyy-MM-dd HH:mm:ss} 模式来解析日期
     * @param expr 表达式
     * @return 解析后的日期对象
     */
    public static Date parseDateTime (String expr) {
        try {
            return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss").parse (expr);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
    }

    /**
     * 从一个给定的 Date 对象构建实例
     * @param date 给定的 Date 对象
     * @return DateUtil 实例
     */
    public static DateUtil fromDate (Date date) {
        return new DateUtil (date);
    }

    /**
     * 从一个给定的日期字符串来构建实例
     * @param date 给定的日期表达式，符合 {@code yyyy-MM-dd} 或 {@code yyyy-MM-dd HH:mm:ss} 格式的字符串
     * @return DateUtil 实例
     */
    public static DateUtil fromDate (String date) {
        try {
            return new DateUtil (parseDate (date));
        } catch (RuntimeException ex) {
            return new DateUtil (parseDateTime (date));
        }
    }

    /**
     * 获取默认的实例，携带当前时间
     * @return DateUtil 实例
     */
    public static DateUtil instance () {
        return new DateUtil ();
    }

    /**
     * 从一个给定的时间戳(精确到毫秒)构建新的实例
     * @param timestamp 给定的时间戳
     * @return DateUtil 实例
     */
    public static DateUtil fromTimestamp (long timestamp) {
        return new DateUtil (timestamp);
    }

    private final Calendar c;

    /**
     * 构造函数
     */
    public DateUtil () {
        c = Calendar.getInstance (Locale.getDefault ());
    }

    /**
     * 从一个给定的时间戳(精确到毫秒)构建新的实例
     * @param timestamp 给定的时间戳
     */
    public DateUtil (long timestamp) {
        this ();
        c.setTimeInMillis (timestamp);
    }

    /**
     * 从一个给定的 Date 对象构建实例
     * @param date 给定的 Date 对象
     */
    public DateUtil (Date date) {
        this ();
        c.setTime (date);
    }

    /**
     * 从一个给定的 日历 对象构建实例
     * @param c 给定的 Date 对象
     */
    public DateUtil (Calendar c) {
        this.c = c;
    }

    /**
     * 获取当前实例持有的 Date 对象
     * @return Date 对象
     */
    public Date getTime () {
        return c.getTime ();
    }

    /**
     * 获取当前实例持有的 Date 对象的时间戳
     * @return 当前持有的时间戳
     */
    public long getTimestamp () {
        return c.getTimeInMillis ();
    }

    /**
     * 当实例当前持有的日期对象格式化为 {@code yyyy-MM-dd} 格式
     * @return 格式化后的结果
     */
    public String formatDate () {
        return formatDate (c.getTime ());
    }

    /**
     * 当实例当前持有的日期对象格式化为 {@code yyyy-MM-dd HH:mm:ss} 格式
     * @return 格式化后的结果
     */
    public String formatDateTime () {
        return formatDateTime (c.getTime ());
    }

    /**
     * 设置指定字段值。这是对 {@link java.util.Calendar#set(int, int) java.util.Calendar.set (int, int)} 方法的代理
     * @param field 时间字段 代码
     * @param value 值
     * @return 当前 DateUtil 对象
     *
     * @see java.util.Calendar#set(int, int)
     */
    public DateUtil set (int field, int value) {
        c.set (field, value);
        return this;
    }

    /**
     * 对指定的字段进行偏移计算。这是对 {@link java.util.Calendar#add(int, int) java.util.Calendar.add (int, int)} 方法的代理
     * @param field 指定的字段代码
     * @param value 偏移量
     * @return 当前 DateUtil 对象
     * @see java.util.Calendar#add(int, int)
     */
    public DateUtil add (int field, int value) {
        c.add (field, value);
        return this;
    }

    /**
     * 获取指定字段的值。这是对 {@link java.util.Calendar#get(int) java.util.Calendar.get (int)} 方法的代理
     * @param field 指定字段
     * @return 字段值
     * @see java.util.Calendar#get(int)
     */
    public int get (int field) {
        return c.get (field);
    }

    /**
     * 将当前持有的日期对象截断到分钟，即<strong>仅保留</strong>到分钟值，秒和毫秒字段值置零
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToMinute () {
        return set (Calendar.MILLISECOND, 0).set (Calendar.SECOND, 0);
    }

    /**
     * 将当前持有的日期对象截断到分钟，即<strong>仅保留</strong>到小时值，分钟、秒和毫秒字段值置零
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToHour () {
        return truncateToMinute ().set (Calendar.MINUTE, 0);
    }

    /**
     * 将当前持有的日期对象截断到日，即当前实例所持有的日期的零点
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToDay () {
        return truncateToHour ().set (Calendar.HOUR_OF_DAY, 0);
    }

    /**
     * 将当前持有的日期对象截断到中午 12:00
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToAM () {
        return set (Calendar.HOUR, 12).truncateToHour ();
    }

    /**
     * 将当前持有的日期对象截断到所在<strong>自然周</strong>的第一天的00:00:00
     * @param firstDay 周的第一天， 可以是 {@link java.util.Calendar#SUNDAY java.util.Calendar.SUNDAY(值1)} 或
     *                 {@link java.util.Calendar#MONDAY java.util.Calendar.MONDAY(值2)}，其他值无效
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToWeek (int firstDay) {
        if (firstDay != Calendar.SUNDAY && firstDay != Calendar.MONDAY)
            throw new IllegalArgumentException ("Invalid the first day of a week");
        int now = c.get (Calendar.DAY_OF_WEEK);
        int offset = now - firstDay;
        return truncateToDay ().add (Calendar.DAY_OF_MONTH, -offset);
    }

    /**
     * 将当前持有的日期对象截断到所在<strong>自然周</strong>的周日的00:00:00
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToWeek () {
        return truncateToWeek (Calendar.SUNDAY);
    }

    /**
     * 将当前持有的日期对象截断到所在<strong>自然月</strong>的 1号 00:00:00
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToMonth () {
        return truncateToDay ().set (Calendar.DAY_OF_MONTH, 1);
    }

    /**
     * 将当前持有的日期对象截断到所在年的1月1号 00:00:00
     * @return 当前 DateUtil 对象
     */
    public DateUtil truncateToYear () {
        return truncateToMonth ().set (Calendar.MONTH, 0);
    }

    /**
     * 将当前持有的日期对象设置到明天零点
     * @return 当前 DateUtil 对象
     */
    public DateUtil nextDay () {
        return add (Calendar.DAY_OF_MONTH, 1).truncateToDay ();
    }

    /**
     * 将当前持有的日期对象设置到昨天零点
     * @return 当前 DateUtil 对象
     */
    public DateUtil prevDay () {
        return add (Calendar.DAY_OF_MONTH, -1).truncateToDay ();
    }

    /**
     * 将当前实例所持有的日期对象设置到所在下个<strong>自然周</strong>的周日的00:00:
     * @return 当前 DateUtil 实例
     */
    public DateUtil nextWeek () {
        return nextWeek (Calendar.SUNDAY);
    }

    /**
     * 将当前实例所持有的日期对象设置到所在下个<strong>自然周</strong>的第一天的00:00:00
     * @param firstDay 周的第一天， 可以是 {@link java.util.Calendar#SUNDAY java.util.Calendar.SUNDAY(值1)} 或
     *                 {@link java.util.Calendar#MONDAY java.util.Calendar.MONDAY(值2)}，其他值无效
     * @return 当前 DateUtil 实例
     */
    public DateUtil nextWeek (int firstDay) {
        return truncateToWeek (firstDay).add (Calendar.WEEK_OF_YEAR, 1);
    }

    /**
     * 将当前实例所持有的日期对象设置到所在上个<strong>自然周</strong>的周日的00:00:
     * @return 当前 DateUtil 实例
     */
    public DateUtil prevWeek () {
        return prevWeek (Calendar.SUNDAY);
    }

    /**
     * 将当前实例所持有的日期对象设置到所在上个<strong>自然周</strong>的第一天的00:00:00
     * @param firstDay 周的第一天， 可以是 {@link java.util.Calendar#SUNDAY java.util.Calendar.SUNDAY(值1)} 或
     *                 {@link java.util.Calendar#MONDAY java.util.Calendar.MONDAY(值2)}，其他值无效
     * @return 当前 DateUtil 实例
     */
    public DateUtil prevWeek (int firstDay) {
        return truncateToWeek (firstDay).add (Calendar.WEEK_OF_YEAR, -1);
    }

    /**
     * 将当前持有的日期对象设置到下个<strong>自然月</strong>的1日零点
     * @return 当前 DateUtil 对象
     */
    public DateUtil nextMonth () {
        return truncateToMonth ().add (Calendar.MONTH, 1);
    }

    /**
     * 将当前持有的日期对象设置到上个<strong>自然月</strong>的1日零点
     * @return 当前 DateUtil 对象
     */
    public DateUtil prevMonth () {
        return truncateToMonth ().add (Calendar.MONTH, -1);
    }
}