package com.software.dev.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    /**
     * 获取当前时间的字符串表示
     * @return 当前时间字符串 (格式: yyyy-MM-dd HH:mm:ss)
     */
    public static String now() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    /**
     * 格式化日期为HTTP日期格式
     * @param date 日期
     * @return HTTP日期格式字符串
     */
    public static String formatHttpDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
    }

    /**
     * 生成基于时间戳的ID，精确到毫秒
     * @return 时间戳ID字符串
     */
    public static String generateTimestampId() {
        return String.valueOf(System.currentTimeMillis());
    }
}