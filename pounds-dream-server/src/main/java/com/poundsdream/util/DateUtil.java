package com.poundsdream.util;

import java.time.LocalDate;
import java.time.Period;

public class DateUtil {

    /**
     * 计算年龄
     */
    public static int calcAge(LocalDate birthday) {
        if (birthday == null) {
            return 25; // 默认年龄
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    /**
     * 获取本周起始日期 (周一)
     */
    public static LocalDate getWeekStart() {
        LocalDate today = LocalDate.now();
        return today.minusDays(today.getDayOfWeek().getValue() - 1);
    }

    /**
     * 获取本周结束日期 (周日)
     */
    public static LocalDate getWeekEnd() {
        return getWeekStart().plusDays(6);
    }
}
