package org.dreamwork.util.cal;

/**
 * Created by game on 2017/9/25
 */
public class Solar {
    public static final int[] MAX_DAY = {31, 0, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static final Holiday[] holidays = {
            new Holiday ( 1,  1, "元旦"),
            new Holiday ( 4, 22, "生日"),
            new Holiday ( 5,  1, "劳动节"),
            new Holiday ( 6,  1, "儿童节"),
            new Holiday ( 7,  1, "建党节"),
            new Holiday ( 8,  1, "建军节"),
            new Holiday ( 9, 10, "教师节"),
            new Holiday ( 9, 30, "昕泓生日"),
            new Holiday (10,  1, "国庆节"),
            new Holiday (10, 19, "老婆生日"),
            new Holiday (12, 25, "圣诞节")
    };
}
