package library.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility helpers for Date and Time parsing, formatting, and arithmetic.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean isBetween(LocalDate target, LocalDate start, LocalDate end) {
        if (target == null) return false;
        if (start != null && target.isBefore(start)) return false;
        if (end != null && target.isAfter(end)) return false;
        return true;
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    public static String formatDaysOverdue(LocalDate dueDate, LocalDate referenceDate) {
        if (dueDate == null || referenceDate == null) return "0 days";
        long days = ChronoUnit.DAYS.between(dueDate, referenceDate);
        if (days <= 0) return "Not overdue";
        return days + " day" + (days == 1 ? "" : "s") + " overdue";
    }
}
