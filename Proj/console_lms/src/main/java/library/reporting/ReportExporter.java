package library.reporting;

import library.exception.PersistenceException;
import library.security.SecurityUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handles exporting reports to TXT, CSV, and compressed ZIP bundles.
 */
public class ReportExporter {

    private static final DateTimeFormatter FILE_TS_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final Path reportsDir;

    public ReportExporter(Path reportsDir) {
        this.reportsDir = reportsDir;
        try {
            Files.createDirectories(reportsDir);
        } catch (IOException e) {
            throw new PersistenceException("Could not create reports directory: " + reportsDir, e);
        }
    }

    public Path export(ReportData data, String format) {
        String ext = format.equalsIgnoreCase("csv") ? "csv" : "txt";
        String safeTitle = data.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "_");
        String filename = safeTitle + "_" + LocalDateTime.now().format(FILE_TS_FMT) + "." + ext;
        Path targetPath = SecurityUtil.resolveSafePath(reportsDir, filename);

        String content = ext.equals("csv") ? ReportFormatter.toCsv(data) : ReportFormatter.toPlainText(data);

        try {
            Files.writeString(targetPath, content, StandardCharsets.UTF_8);
            return targetPath.toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new PersistenceException("Failed to export report to " + targetPath, e);
        }
    }

    public Path exportZipBundle(List<ReportData> reports, String bundleName) {
        String safeName = bundleName.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        String filename = safeName + "_" + LocalDateTime.now().format(FILE_TS_FMT) + ".zip";
        Path zipPath = SecurityUtil.resolveSafePath(reportsDir, filename);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            for (ReportData r : reports) {
                String safeTitle = r.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "_");

                // Add TXT entry
                String txtContent = ReportFormatter.toPlainText(r);
                ZipEntry txtEntry = new ZipEntry(safeTitle + ".txt");
                zos.putNextEntry(txtEntry);
                zos.write(txtContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();

                // Add CSV entry
                String csvContent = ReportFormatter.toCsv(r);
                ZipEntry csvEntry = new ZipEntry(safeTitle + ".csv");
                zos.putNextEntry(csvEntry);
                zos.write(csvContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            return zipPath.toAbsolutePath().normalize();
        } catch (IOException e) {
            throw new PersistenceException("Failed to create ZIP bundle at " + zipPath, e);
        }
    }

    public Path getReportsDir() {
        return reportsDir;
    }
}
