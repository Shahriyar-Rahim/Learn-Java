package library.reporting;

import java.time.LocalDateTime;
import java.util.*;

/**
 * In-memory representation of structured report data.
 */
public class ReportData {
    private final String title;
    private final LocalDateTime generatedAt;
    private final Map<String, String> summaryStats = new LinkedHashMap<>();
    private final List<String> headers = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public ReportData(String title) {
        this.title = title;
        this.generatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<String, String> getSummaryStats() {
        return Collections.unmodifiableMap(summaryStats);
    }

    public void addSummaryStat(String label, Object value) {
        this.summaryStats.put(label, String.valueOf(value));
    }

    public List<String> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    public void setHeaders(List<String> headers) {
        this.headers.clear();
        if (headers != null) {
            this.headers.addAll(headers);
        }
    }

    public List<List<String>> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addRow(List<String> row) {
        this.rows.add(new ArrayList<>(row));
    }
}
