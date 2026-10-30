package library.reporting;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Formats ReportData into Plain Text (TXT) and Comma-Separated Values (CSV).
 */
public final class ReportFormatter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportFormatter() {
    }

    public static String toPlainText(ReportData data) {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(75);
        sb.append(line).append("\n");
        sb.append("  ").append(data.getTitle().toUpperCase()).append("\n");
        sb.append("  Generated At: ").append(data.getGeneratedAt().format(DT_FMT)).append("\n");
        sb.append(line).append("\n\n");

        if (!data.getSummaryStats().isEmpty()) {
            sb.append("--- SUMMARY METRICS ---\n");
            for (Map.Entry<String, String> entry : data.getSummaryStats().entrySet()) {
                sb.append(String.format("  %-32s : %s\n", entry.getKey(), entry.getValue()));
            }
            sb.append("\n");
        }

        if (!data.getHeaders().isEmpty()) {
            sb.append("--- DETAILED RECORDS ---\n");
            List<String> headers = data.getHeaders();
            List<List<String>> rows = data.getRows();

            int colCount = headers.size();
            int[] widths = new int[colCount];
            for (int i = 0; i < colCount; i++) {
                widths[i] = Math.max(widths[i], headers.get(i).length());
            }
            for (List<String> row : rows) {
                for (int i = 0; i < Math.min(colCount, row.size()); i++) {
                    String val = row.get(i);
                    widths[i] = Math.max(widths[i], val != null ? val.length() : 0);
                }
            }

            // Cap column width to keep readable
            for (int i = 0; i < colCount; i++) {
                widths[i] = Math.min(widths[i], 35);
            }

            // Print header row
            sb.append("  ");
            for (int i = 0; i < colCount; i++) {
                sb.append(String.format("%-" + widths[i] + "s", truncate(headers.get(i), widths[i])));
                if (i < colCount - 1) sb.append(" | ");
            }
            sb.append("\n  ");
            for (int i = 0; i < colCount; i++) {
                sb.append("-".repeat(widths[i]));
                if (i < colCount - 1) sb.append("-+-");
            }
            sb.append("\n");

            // Print data rows
            if (rows.isEmpty()) {
                sb.append("  (No records found)\n");
            } else {
                for (List<String> row : rows) {
                    sb.append("  ");
                    for (int i = 0; i < colCount; i++) {
                        String val = (i < row.size() && row.get(i) != null) ? row.get(i) : "";
                        sb.append(String.format("%-" + widths[i] + "s", truncate(val, widths[i])));
                        if (i < colCount - 1) sb.append(" | ");
                    }
                    sb.append("\n");
                }
            }
        }
        sb.append("\n").append(line).append("\n");
        return sb.toString();
    }

    public static String toCsv(ReportData data) {
        StringBuilder sb = new StringBuilder();
        // Comment header
        sb.append("# ").append(escapeCsv(data.getTitle())).append("\n");
        sb.append("# Generated: ").append(data.getGeneratedAt().format(DT_FMT)).append("\n");

        if (!data.getSummaryStats().isEmpty()) {
            sb.append("# Summary Metrics\n");
            for (Map.Entry<String, String> entry : data.getSummaryStats().entrySet()) {
                sb.append("# ").append(escapeCsv(entry.getKey())).append(",").append(escapeCsv(entry.getValue())).append("\n");
            }
        }

        // Column headers
        if (!data.getHeaders().isEmpty()) {
            List<String> escapedHeaders = new ArrayList<>();
            for (String h : data.getHeaders()) {
                escapedHeaders.add(escapeCsv(h));
            }
            sb.append(String.join(",", escapedHeaders)).append("\n");

            for (List<String> row : data.getRows()) {
                List<String> escapedCols = new ArrayList<>();
                for (String col : row) {
                    escapedCols.add(escapeCsv(col));
                }
                sb.append(String.join(",", escapedCols)).append("\n");
            }
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String s = value.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen - 2) + "..";
    }
}
