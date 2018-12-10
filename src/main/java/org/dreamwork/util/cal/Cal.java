package org.dreamwork.util.cal;

import org.dreamwork.util.DateUtil;

import java.util.List;

/**
 * Created by game on 2017/9/25
 */
public class Cal {
    private DateUtil du;
    private List<Holiday> solarHolidaies, lunarHolidaies;

    public static final String[] TERMS = {
            "小寒", "大寒",
            "立春", "雨水", "惊蛰", "春分", "清明", "谷雨",
            "立夏", "小满", "芒种", "夏至", "小暑", "大暑",
            "立秋", "处暑", "白露", "秋分", "寒露", "霜降",
            "立冬", "小雪", "大雪", "冬至"
    };
}