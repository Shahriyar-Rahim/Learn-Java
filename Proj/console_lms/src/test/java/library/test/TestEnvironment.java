package library.test;

import library.persistence.DataStore;
import library.reporting.ReportExporter;
import library.repository.*;
import library.service.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Creates isolated sandbox test environments with fresh repositories and services.
 */
public class TestEnvironment {

    public final Path testDir;
    public final Path dataDir;
    public final Path reportsDir;
    public final Path logsDir;

    public final DataStore dataStore;
    public final UserRepository userRepository;
    public final AuthorRepository authorRepository;
    public final SectionRepository sectionRepository;
    public final BookRepository bookRepository;
    public final BookCopyRepository copyRepository;
    public final BorrowRepository borrowRepository;
    public final HoldRepository holdRepository;
    public final ConfigurationRepository configRepository;
    public final AuditLogRepository auditLogRepository;

    public final AuditLogService auditLogService;
    public final AuthenticationService authService;
    public final UserService userService;
    public final BookService bookService;
    public final AuthorService authorService;
    public final CatalogueService catalogueService;
    public final HoldService holdService;
    public final BorrowService borrowService;
    public final ReportExporter reportExporter;
    public final ReportService reportService;

    public static TestEnvironment create() {
        try {
            Path temp = Files.createTempDirectory("lms_test_");
            return new TestEnvironment(temp);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test temp dir", e);
        }
    }

    public TestEnvironment(Path testDir) {
        this.testDir = testDir;
        this.dataDir = testDir.resolve("data");
        this.reportsDir = testDir.resolve("reports");
        this.logsDir = dataDir.resolve("logs");

        this.dataStore = new DataStore(dataDir);
        this.userRepository = new UserRepository(dataStore);
        this.authorRepository = new AuthorRepository(dataStore);
        this.sectionRepository = new SectionRepository(dataStore);
        this.bookRepository = new BookRepository(dataStore);
        this.copyRepository = new BookCopyRepository(dataStore);
        this.borrowRepository = new BorrowRepository(dataStore);
        this.holdRepository = new HoldRepository(dataStore);
        this.configRepository = new ConfigurationRepository(dataStore);
        this.auditLogRepository = new AuditLogRepository(dataStore);

        this.auditLogService = new AuditLogService(auditLogRepository, logsDir);
        this.authService = new AuthenticationService(userRepository, auditLogService);
        this.userService = new UserService(userRepository, borrowRepository, holdRepository, auditLogService);
        this.bookService = new BookService(bookRepository, copyRepository, authorRepository, sectionRepository, borrowRepository, auditLogService);
        this.authorService = new AuthorService(authorRepository, bookRepository, auditLogService);
        this.catalogueService = new CatalogueService(sectionRepository, bookRepository, auditLogService);
        this.holdService = new HoldService(holdRepository, bookRepository, copyRepository, userRepository, borrowRepository, configRepository, auditLogService);
        this.borrowService = new BorrowService(borrowRepository, bookRepository, copyRepository, userRepository, holdRepository, holdService, configRepository, auditLogService);

        this.reportExporter = new ReportExporter(reportsDir);
        this.reportService = new ReportService(bookRepository, copyRepository, userRepository, borrowRepository, holdRepository, authorRepository, sectionRepository, reportExporter, auditLogService);
    }

    public void cleanup() {
        try {
            if (Files.exists(testDir)) {
                Files.walk(testDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (Exception ignored) {
        }
    }
}
