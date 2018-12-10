package org.dreamwork.util.cal;

import java.text.SimpleDateFormat;

/**
 * http://www.cnblogs.com/qintangtao/archive/2013/03/01/2938887.html
 * http://blog.csdn.net/handyhuang/article/details/50439742
 * http://blog.jjonline.cn/userInterFace/173.html
 * Created by game on 2017/9/25
 */
public class Lunar {
    public static final int FIRST_YEAR          = 1900;
    public static final int BASE_DAYS           = 12 * 29;
    public static final int LEAP_YEAR_MASK      = 1 << 16;
    public static final int LEAP_MONTH_MASK     = 0x0f;
    public static final int MONTH_MASK_START    = LEAP_YEAR_MASK >> 1;

    public static final Date FIRST_DAY =        new Date (FIRST_YEAR, 1, 31);

    public static final String[] CHINESE_DATE = {
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    };
    public static final String[] CHINESE_MONTH = {
        "正月", "二月", "三月", "四月", "五月",   "六月",
        "七月", "八月", "九月", "十月", "十一月", "腊月"
    };
    public static final char CHINESE_LEAP = '润';

    /**
     * 来源于网上的农历数据
     * 数据结构如下，共使用17位数据
     * 第17位：表示闰月天数，0表示29天   1表示30天
     * 第16位-第5位（共12位）表示12个月，其中第16位表示第一月，如果该月为30天则为1，29天为0
     * 第4位-第1位（共4位）表示闰月是哪个月，如果当年没有闰月，则置0
     */
    public static final int[] LUNAR_DATA = {
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2, //1900-1909
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977, //1910-1919
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970, //1920-1929
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950, //1930-1939
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557, //1940-1949
        0x06ca0, 0x0b550, 0x15355, 0x04da0, 0x0a5b0, 0x14573, 0x052b0, 0x0a9a8, 0x0e950, 0x06aa0, //1950-1959
        0x0aea6, 0x0ab50, 0x04b60, 0x0aae4, 0x0a570, 0x05260, 0x0f263, 0x0d950, 0x05b57, 0x056a0, //1960-1969
        0x096d0, 0x04dd5, 0x04ad0, 0x0a4d0, 0x0d4d4, 0x0d250, 0x0d558, 0x0b540, 0x0b6a0, 0x195a6, //1970-1979
        0x095b0, 0x049b0, 0x0a974, 0x0a4b0, 0x0b27a, 0x06a50, 0x06d40, 0x0af46, 0x0ab60, 0x09570, //1980-1989
        0x04af5, 0x04970, 0x064b0, 0x074a3, 0x0ea50, 0x06b58, 0x055c0, 0x0ab60, 0x096d5, 0x092e0, //1990-1999
        0x0c960, 0x0d954, 0x0d4a0, 0x0da50, 0x07552, 0x056a0, 0x0abb7, 0x025d0, 0x092d0, 0x0cab5, //2000-2009
        0x0a950, 0x0b4a0, 0x0baa4, 0x0ad50, 0x055d9, 0x04ba0, 0x0a5b0, 0x15176, 0x052b0, 0x0a930, //2010-2019
        0x07954, 0x06aa0, 0x0ad50, 0x05b52, 0x04b60, 0x0a6e6, 0x0a4e0, 0x0d260, 0x0ea65, 0x0d530, //2020-2029
        0x05aa0, 0x076a3, 0x096d0, 0x04afb, 0x04ad0, 0x0a4d0, 0x1d0b6, 0x0d250, 0x0d520, 0x0dd45, //2030-2039
        0x0b5a0, 0x056d0, 0x055b2, 0x049b0, 0x0a577, 0x0a4b0, 0x0aa50, 0x1b255, 0x06d20, 0x0ada0, //2040-2049
        0x14b63, 0x09370, 0x049f8, 0x04970, 0x064b0, 0x168a6, 0x0ea50, 0x06b20, 0x1a6c4, 0x0aae0, //2050-2059
        0x0a2e0, 0x0d2e3, 0x0c960, 0x0d557, 0x0d4a0, 0x0da50, 0x05d55, 0x056a0, 0x0a6d0, 0x055d4, //2060-2069
        0x052d0, 0x0a9b8, 0x0a950, 0x0b4a0, 0x0b6a6, 0x0ad50, 0x055a0, 0x0aba4, 0x0a5b0, 0x052b0, //2070-2079
        0x0b273, 0x06930, 0x07337, 0x06aa0, 0x0ad50, 0x14b55, 0x04b60, 0x0a570, 0x054e4, 0x0d160, //2080-2089
        0x0e968, 0x0d520, 0x0daa0, 0x16aa6, 0x056d0, 0x04ae0, 0x0a9d4, 0x0a2d0, 0x0d150, 0x0f252, //2090-2099
        0x0d520
    };

    public static final Holiday[] holidays = {
            new Holiday ( 1,  1, "春节"),
            new Holiday ( 1, 15, "元宵节"),
            new Holiday ( 5,  5, "端午节"),
            new Holiday ( 8, 15, "中秋节"),
            new Holiday ( 9,  9, "重阳节"),
            new Holiday (12,  8, "腊八节"),
            // 除夕要单独计算
    };

    public static String toString (Date date) {
        return CHINESE_MONTH [date.month - 1] + CHINESE_DATE [date.date - 1];
    }

    public static Date toLunar (Date solar) {
        int delta = solar.deltaByDay (FIRST_DAY);
        System.out.println ("delta days = " + delta);

        return new Date ();
    }

    private static int getDaysInYear (int year) {
        if (year < 1901 || year > 2099) {
            throw new ArrayIndexOutOfBoundsException (year);
        }

        int data = LUNAR_DATA [year - FIRST_YEAR];
        int days = BASE_DAYS;
        boolean leap_year = ((data & LEAP_YEAR_MASK) != 0);
        for (int i = MONTH_MASK_START; i > 0x08; i >>= 1) {
            if ((data & i) != 0) days += 1;
        }

        if ((data & LEAP_MONTH_MASK) != 0) {
            days += leap_year ? 30 : 29;
        }
        return days;
    }

    private static int getDaysInMonth (int year, int month) {
        if (year < 1901 || year > 2099) {
            throw new ArrayIndexOutOfBoundsException (year);
        }

        if (month < 0 || month > 12) {
            throw new ArrayIndexOutOfBoundsException (month);
        }

        int data = LUNAR_DATA [year - FIRST_YEAR];
        if (month < 12) {

        }
        return 0;
    }

    public static void main (String[] args) {
/*
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
        Date date = new Date ();
        date = toLunar (date);
        System.out.println (sdf.format (date));
*/
        System.out.printf ("1 << 17 = 0x%X%n", 1 << 17);
        int date = LUNAR_DATA [0];
        for (int i = 16; i > 4; i --) {
            int mask = 1 << i;
            System.out.printf ("bits[%2d] = %d%n", i, (date & mask) != 0 ? 1 : 0);
        }
    }
}