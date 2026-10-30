package library.ui;

import library.exception.LibraryException;
import library.model.BookDetail;
import library.model.BookSort;
import library.model.Role;
import library.model.User;
import library.repository.ConfigurationRepository;
import library.security.UserSession;
import library.service.*;

import java.util.List;

/**
 * Main application entrypoint controller. Handles first-run setup, guest browsing, registration, and login.
 */
public class MainMenuController {

    private final ConsoleInputReader reader;
    private final UserSession session;
    private final AuthenticationService authService;
    private final UserService userService;
    private final BookService bookService;
    private final AuthorService authorService;
    private final CatalogueService catalogueService;
    private final BorrowService borrowService;
    private final HoldService holdService;
    private final ReportService reportService;
    private final ConfigurationRepository configRepository;
    private final AuditLogService auditLogService;

    public MainMenuController(ConsoleInputReader reader, UserSession session,
                              AuthenticationService authService, UserService userService,
                              BookService bookService, AuthorService authorService,
                              CatalogueService catalogueService, BorrowService borrowService,
                              HoldService holdService, ReportService reportService,
                              ConfigurationRepository configRepository, AuditLogService auditLogService) {
        this.reader = reader;
        this.session = session;
        this.authService = authService;
        this.userService = userService;
        this.bookService = bookService;
        this.authorService = authorService;
        this.catalogueService = catalogueService;
        this.borrowService = borrowService;
        this.holdService = holdService;
        this.reportService = reportService;
        this.configRepository = configRepository;
        this.auditLogService = auditLogService;
    }

    public void start() {
        checkFirstRunAdminSetup();

        while (reader.hasNext()) {
            String systemName = configRepository.get().getSystemName();
            ConsoleRenderer.printHeader(systemName.toUpperCase());
            System.out.println("  1. Login");
            System.out.println("  2. Register");
            System.out.println("  3. Browse Catalogue");
            System.out.println("  4. Search Books");
            System.out.println("  5. Exit");

            int choice = reader.readInt("\nChoose an option (1-5): ", 1, 5);
            if (choice == -1) break; // EOF

            switch (choice) {
                case 1 -> handleLogin();
                case 2 -> handleRegister();
                case 3 -> handleBrowseCatalogue();
                case 4 -> handleSearchBooks();
                case 5 -> {
                    System.out.println("\nThank you for using " + systemName + ". Goodbye!\n");
                    return;
                }
            }
        }
    }

    private void checkFirstRunAdminSetup() {
        if (!authService.isFirstRun()) {
            return;
        }

        ConsoleRenderer.printHeader("INITIAL SYSTEM SETUP");
        System.out.println("  No administrator account exists.");
        System.out.println("  Please create the first administrator account to continue.\n");

        while (true) {
            try {
                String username = reader.readNonEmptyString("Admin Username: ", "Username");
                char[] password = reader.readPassword("Admin Password (min 8 chars, 1 letter, 1 digit): ");
                char[] confirm = reader.readPassword("Confirm Password: ");
                String fullName = reader.readNonEmptyString("Full Name: ", "Full Name");
                String email = reader.readNonEmptyString("Email Address: ", "Email");
                String phone = reader.readLine("Phone Number: ");

                String secret = System.getenv("LMS_ADMIN_SECRET");
                String enteredSecret = "default";
                if (secret != null && !secret.isBlank()) {
                    enteredSecret = reader.readNonEmptyString("Enter Admin Setup Secret Key: ", "Secret Key");
                }

                User admin = authService.createFirstAdmin(username, password, confirm, fullName, email, phone, enteredSecret);
                ConsoleRenderer.printSuccess("Administrator account '" + admin.getUsername() + "' created successfully!");
                reader.waitForEnter();
                break;
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
                System.out.println("  Please retry administrator setup.");
            }
        }
    }

    private void handleLogin() {
        ConsoleRenderer.printSubHeader("USER LOGIN");
        String username = reader.readLine("Username: ");
        if (username == null || username.isBlank()) {
            ConsoleRenderer.printWarning("Username cannot be empty.");
            return;
        }
        char[] password = reader.readPassword("Password: ");

        try {
            User user = authService.login(username, password);
            session.login(user);
            ConsoleRenderer.printSuccess("Welcome back, " + user.getFullName() + "!");

            if (user.getRole() == Role.ADMIN) {
                AdminController adminController = new AdminController(
                        reader, session, userService, bookService, authorService,
                        catalogueService, borrowService, holdService, reportService,
                        configRepository, auditLogService
                );
                adminController.run();
            } else {
                UserController userController = new UserController(
                        reader, session, userService, bookService,
                        borrowService, holdService, reportService
                );
                userController.run();
            }
        } catch (LibraryException e) {
            ConsoleRenderer.printError(e.getMessage());
            reader.waitForEnter();
        }
    }

    private void handleRegister() {
        ConsoleRenderer.printSubHeader("NEW MEMBER REGISTRATION");
        try {
            String username = reader.readNonEmptyString("Desired Username: ", "Username");
            char[] password = reader.readPassword("Password (min 8 chars, at least 1 letter and 1 digit): ");
            char[] confirm = reader.readPassword("Confirm Password: ");
            String fullName = reader.readNonEmptyString("Full Name: ", "Full Name");
            String email = reader.readNonEmptyString("Email Address: ", "Email");
            String phone = reader.readLine("Phone Number: ");

            User registered = authService.register(username, password, confirm, fullName, email, phone);
            ConsoleRenderer.printSuccess("Registration submitted successfully!\n" +
                    "  User ID : " + registered.getId() + "\n" +
                    "  Username: " + registered.getUsername() + "\n" +
                    "  Status  : PENDING_APPROVAL\n" +
                    "  NOTE    : An administrator must approve your account before you can log in.");
        } catch (LibraryException e) {
            ConsoleRenderer.printError(e.getMessage());
        }
        reader.waitForEnter();
    }

    private void handleBrowseCatalogue() {
        ConsoleRenderer.printSubHeader("LIBRARY CATALOGUE");
        List<BookDetail> books = bookService.getAllBookDetails();
        ConsoleRenderer.printBookList(books);
        System.out.println("\n  (Tip: Register or log in to borrow books or place holds)");
        reader.waitForEnter();
    }

    private void handleSearchBooks() {
        ConsoleRenderer.printSubHeader("SEARCH BOOKS");
        String q = reader.readLine("Enter search keyword (Title, Author, ISBN, Section): ");
        System.out.println("Sort by:");
        System.out.println("  1. Title (A-Z)");
        System.out.println("  2. Title (Z-A)");
        System.out.println("  3. Newest publication date");
        System.out.println("  4. Oldest publication date");
        System.out.println("  5. Author name");
        System.out.println("  6. Catalogue section");
        System.out.println("  7. Availability");
        int sc = reader.readInt("Choice (1-7): ", 1, 7);
        BookSort sort = switch (sc) {
            case 1 -> BookSort.TITLE_ASC;
            case 2 -> BookSort.TITLE_DESC;
            case 3 -> BookSort.DATE_NEWEST;
            case 4 -> BookSort.DATE_OLDEST;
            case 5 -> BookSort.AUTHOR;
            case 6 -> BookSort.SECTION;
            default -> BookSort.AVAILABILITY;
        };

        boolean onlyAvail = reader.readConfirmation("Show available books only?");
        List<BookDetail> results = bookService.searchBooks(q, null, null, null, onlyAvail, sort);
        ConsoleRenderer.printBookList(results);
        reader.waitForEnter();
    }
}
