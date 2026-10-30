package library;

import library.persistence.DataStore;
import library.reporting.ReportExporter;
import library.repository.*;
import library.security.UserSession;
import library.service.*;
import library.ui.ConsoleInputReader;
import library.ui.MainMenuController;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Main {

    public static void main(String[] args) {
        String dataDirPath = System.getProperty("library.data.dir", System.getenv().getOrDefault("LMS_DATA_DIR", "data"));
        Path dataDir = Paths.get(dataDirPath).toAbsolutePath().normalize();
        Path reportsDir = Paths.get("reports").toAbsolutePath().normalize();
        Path logsDir = dataDir.resolve("logs");

        // 1. Persistence Engine
        DataStore dataStore = new DataStore(dataDir);

        // 2. Repositories
        UserRepository userRepository = new UserRepository(dataStore);
        AuthorRepository authorRepository = new AuthorRepository(dataStore);
        SectionRepository sectionRepository = new SectionRepository(dataStore);
        BookRepository bookRepository = new BookRepository(dataStore);
        BookCopyRepository copyRepository = new BookCopyRepository(dataStore);
        BorrowRepository borrowRepository = new BorrowRepository(dataStore);
        HoldRepository holdRepository = new HoldRepository(dataStore);
        ConfigurationRepository configRepository = new ConfigurationRepository(dataStore);
        AuditLogRepository auditLogRepository = new AuditLogRepository(dataStore);

        // 3. Graceful Shutdown Hook to flush state
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                userRepository.flush();
                authorRepository.flush();
                sectionRepository.flush();
                bookRepository.flush();
                copyRepository.flush();
                borrowRepository.flush();
                holdRepository.flush();
                auditLogRepository.flush();
            } catch (Exception ignored) {
            }
        }));

        // 4. Services
        AuditLogService auditLogService = new AuditLogService(auditLogRepository, logsDir);
        AuthenticationService authService = new AuthenticationService(userRepository, auditLogService);
        UserService userService = new UserService(userRepository, borrowRepository, holdRepository, auditLogService);
        BookService bookService = new BookService(bookRepository, copyRepository, authorRepository, sectionRepository, borrowRepository, auditLogService);
        AuthorService authorService = new AuthorService(authorRepository, bookRepository, auditLogService);
        CatalogueService catalogueService = new CatalogueService(sectionRepository, bookRepository, auditLogService);
        HoldService holdService = new HoldService(holdRepository, bookRepository, copyRepository, userRepository, borrowRepository, configRepository, auditLogService);
        BorrowService borrowService = new BorrowService(borrowRepository, bookRepository, copyRepository, userRepository, holdRepository, holdService, configRepository, auditLogService);

        ReportExporter reportExporter = new ReportExporter(reportsDir);
        ReportService reportService = new ReportService(bookRepository, copyRepository, userRepository, borrowRepository, holdRepository, authorRepository, sectionRepository, reportExporter, auditLogService);

        // 5. User Session & CLI Controller
        UserSession session = new UserSession();
        ConsoleInputReader reader = new ConsoleInputReader(System.in);

        MainMenuController mainController = new MainMenuController(
                reader, session, authService, userService, bookService,
                authorService, catalogueService, borrowService, holdService,
                reportService, configRepository, auditLogService
        );

        // 6. Launch Application
        mainController.start();
    }
}
