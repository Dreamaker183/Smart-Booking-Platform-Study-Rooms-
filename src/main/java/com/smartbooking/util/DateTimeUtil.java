package com.smartbooking.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static LocalDateTime parse(String input) {
        return LocalDateTime.parse(input, FORMATTER);
    }

    public static String format(LocalDateTime time) {
        return FORMATTER.format(time);
    }
}
