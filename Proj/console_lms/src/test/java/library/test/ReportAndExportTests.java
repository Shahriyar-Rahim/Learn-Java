package library.test;

import library.model.BookCondition;
import library.model.User;
import library.reporting.ReportData;
import library.reporting.ReportFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static library.test.TestAssert.*;

public class ReportAndExportTests {

    public static void run() {
        testEmptyDatasetReports();
        testReportGenerationAndFormatting();
        testFileExportsTxtAndCsv();
        testZipExport();
    }

    private static void testEmptyDatasetReports() {
        TestEnvironment env = TestEnvironment.create();
        try {
            // Generating reports on empty dataset must not throw exceptions
            ReportData inv = env.reportService.generateInventoryReport();
            assertNotNull(inv, "Inventory report on empty database must not be null");
            assertEquals("0", inv.getSummaryStats().get("Total Book Titles"), "Total books should be 0");

            ReportData usr = env.reportService.generateUserReport();
            assertNotNull(usr, "User report on empty database must not be null");
            assertEquals("0", usr.getSummaryStats().get("Total Registered Users"), "Total users should be 0");

            ReportData brw = env.reportService.generateBorrowingReport();
            assertNotNull(brw, "Borrowing report on empty database must not be null");
            assertEquals("0", brw.getSummaryStats().get("Total Historical Loans"), "Total loans should be 0");

            ReportData cat = env.reportService.generateCatalogueReport();
            assertNotNull(cat, "Catalogue report on empty database must not be null");

            ReportData hld = env.reportService.generateHoldReport();
            assertNotNull(hld, "Hold report on empty database must not be null");
        } finally {
            env.cleanup();
        }
    }

    private static void testReportGenerationAndFormatting() {
        TestEnvironment env = TestEnvironment.create();
        try {
            User admin = env.authService.createFirstAdmin("reportadmin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "rep@test.com", null, "default");
            User member = env.authService.register("reader1", "ReaderPass123".toCharArray(), "ReaderPass123".toCharArray(), "Reader One", "r1@test.com", null);
            env.userService.approveUser(member.getId(), "reportadmin");

            var sec = env.catalogueService.createSection("History", "History", "reportadmin");
            var aut = env.authorService.createAuthor("Will Durant", "Historian", 1885, "reportadmin");
            var b1 = env.bookService.createBook("978-1451647617", "The Story of Philosophy", "Classic", List.of(aut.getId()), sec.getId(), "Simon & Schuster", LocalDate.of(1926, 1, 1), "English", "1st", 2, "Shelf H-1", BookCondition.GOOD, "reportadmin");

            env.borrowService.borrowBook(member.getId(), b1.getId());

            ReportData inv = env.reportService.generateInventoryReport();
            String plainText = ReportFormatter.toPlainText(inv);
            assertTrue(plainText.contains("LIBRARY INVENTORY & STOCK REPORT"), "Plain text report should contain title");
            assertTrue(plainText.contains("The Story of Philosophy"), "Plain text report should list book title");

            String csvText = ReportFormatter.toCsv(inv);
            assertTrue(csvText.contains("\"The Story of Philosophy\""), "CSV should contain quoted title");
            assertTrue(csvText.contains("Book ID"), "CSV should contain headers");

            // Personal user report
            ReportData personal = env.reportService.generatePersonalUserReport(member.getId());
            assertNotNull(personal, "Personal report must not be null");
            assertTrue(personal.getTitle().contains("reader1"), "Personal report title should have username");
        } finally {
            env.cleanup();
        }
    }

    private static void testFileExportsTxtAndCsv() {
        TestEnvironment env = TestEnvironment.create();
        try {
            ReportData report = new ReportData("Test Export Report");
            report.addSummaryStat("Metric A", 42);
            report.setHeaders(List.of("Col 1", "Col 2"));
            report.addRow(List.of("Val 1", "Val 2"));

            // Export TXT
            Path txtPath = env.reportService.exportReport(report, "txt", "tester");
            assertTrue(Files.exists(txtPath), "TXT report file must exist on disk");
            assertTrue(Files.size(txtPath) > 0, "TXT file must not be empty");

            // Export CSV
            Path csvPath = env.reportService.exportReport(report, "csv", "tester");
            assertTrue(Files.exists(csvPath), "CSV report file must exist on disk");
            assertTrue(Files.size(csvPath) > 0, "CSV file must not be empty");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            env.cleanup();
        }
    }

    private static void testZipExport() {
        TestEnvironment env = TestEnvironment.create();
        try {
            Path zipPath = env.reportService.exportAllReportsBundle("tester");
            assertTrue(Files.exists(zipPath), "ZIP archive must exist on disk");
            assertTrue(Files.size(zipPath) > 0, "ZIP archive must not be empty");

            // Verify zip contains entries
            boolean hasTxt = false, hasCsv = false;
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".txt")) hasTxt = true;
                    if (entry.getName().endsWith(".csv")) hasCsv = true;
                    zis.closeEntry();
                }
            }
            assertTrue(hasTxt, "ZIP must contain TXT entries");
            assertTrue(hasCsv, "ZIP must contain CSV entries");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            env.cleanup();
        }
    }
}
