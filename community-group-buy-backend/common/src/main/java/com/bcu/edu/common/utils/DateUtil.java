package com.bcu.edu.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
public class DateUtil {

    /**
     * 标准日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 标准时间格式
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 紧凑日期时间格式（用于文件名等）
     */
    public static final String COMPACT_DATETIME_PATTERN = "yyyyMMddHHmmss";

    /**
     * 紧凑日期格式
     */
    public static final String COMPACT_DATE_PATTERN = "yyyyMMdd";

    /**
     * 格式化日期时间
     *
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DATETIME_PATTERN);
    }

    /**
     * 格式化日期时间（指定格式）
     *
     * @param dateTime 日期时间
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        return format(date, DATE_PATTERN);
    }

    /**
     * 格式化日期（指定格式）
     *
     * @param date 日期
     * @param pattern 格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DATETIME_PATTERN);
    }

    /**
     * 解析日期时间字符串（指定格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, DATE_PATTERN);
    }

    /**
     * 解析日期字符串（指定格式）
     *
     * @param dateStr 日期字符串
     * @param pattern 格式
     * @return LocalDate
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取当前日期时间字符串
     *
     * @return 格式化后的当前日期时间
     */
    public static String now() {
        return format(LocalDateTime.now());
    }

    /**
     * 获取当前日期时间字符串（紧凑格式）
     *
     * @return 格式化后的当前日期时间（用于文件名等）
     */
    public static String nowCompact() {
        return format(LocalDateTime.now(), COMPACT_DATETIME_PATTERN);
    }

    /**
     * 获取当前日期字符串
     *
     * @return 格式化后的当前日期
     */
    public static String today() {
        return format(LocalDate.now());
    }

    /**
     * 获取当前日期字符串（紧凑格式）
     *
     * @return 格式化后的当前日期
     */
    public static String todayCompact() {
        return format(LocalDate.now(), COMPACT_DATE_PATTERN);
    }

    /**
     * 增加天数
     *
     * @param dateTime 日期时间
     * @param days 天数
     * @return 增加后的日期时间
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime == null ? null : dateTime.plusDays(days);
    }

    /**
     * 减少天数
     *
     * @param dateTime 日期时间
     * @param days 天数
     * @return 减少后的日期时间
     */
    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        return dateTime == null ? null : dateTime.minusDays(days);
    }

    /**
     * 增加小时
     *
     * @param dateTime 日期时间
     * @param hours 小时数
     * @return 增加后的日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime == null ? null : dateTime.plusHours(hours);
    }

    /**
     * 减少小时
     *
     * @param dateTime 日期时间
     * @param hours 小时数
     * @return 减少后的日期时间
     */
    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        return dateTime == null ? null : dateTime.minusHours(hours);
    }

    /**
     * 增加分钟
     *
     * @param dateTime 日期时间
     * @param minutes 分钟数
     * @return 增加后的日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime == null ? null : dateTime.plusMinutes(minutes);
    }

    /**
     * 计算两个日期时间之间的天数差
     *
     * @param start 开始日期时间
     * @param end 结束日期时间
     * @return 天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的小时差
     *
     * @param start 开始日期时间
     * @param end 结束日期时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟差
     *
     * @param start 开始日期时间
     * @param end 结束日期时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 判断日期时间是否在指定范围内
     *
     * @param dateTime 待判断的日期时间
     * @param start 开始时间
     * @param end 结束时间
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        if (dateTime == null || start == null || end == null) {
            return false;
        }
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * 判断是否已过期
     *
     * @param dateTime 日期时间
     * @return 是否已过期
     */
    public static boolean isExpired(LocalDateTime dateTime) {
        if (dateTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(dateTime);
    }

    /**
     * 获取一天的开始时间（00:00:00）
     *
     * @param date 日期
     * @return 开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    /**
     * 获取一天的结束时间（23:59:59）
     *
     * @param date 日期
     * @return 结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date == null ? null : date.atTime(23, 59, 59);
    }
}

