package library.util;

/**
 * Utility helpers for String manipulation, trimming, padding, and formatting.
 */
public final class StringUtil {

    private StringUtil() {
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (maxLength <= 3 || str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String str, int totalWidth) {
        if (str == null) str = "";
        if (str.length() >= totalWidth) return str;
        return str + " ".repeat(totalWidth - str.length());
    }

    public static String padLeft(String str, int totalWidth) {
        if (str == null) str = "";
        if (str.length() >= totalWidth) return str;
        return " ".repeat(totalWidth - str.length()) + str;
    }

    public static boolean containsIgnoreCase(String source, String query) {
        if (source == null || query == null) return false;
        return source.toLowerCase().contains(query.toLowerCase().trim());
    }

    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) return email;
        int atIdx = email.indexOf('@');
        String name = email.substring(0, atIdx);
        String domain = email.substring(atIdx);
        if (name.length() <= 2) return name.charAt(0) + "*" + domain;
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1) + domain;
    }
}
