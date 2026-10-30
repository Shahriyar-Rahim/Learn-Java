package library.ui;

import library.exception.LibraryException;
import library.model.*;
import library.reporting.ReportData;
import library.reporting.ReportFormatter;
import library.repository.ConfigurationRepository;
import library.security.UserSession;
import library.service.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all Administrator interactions and workflows in the CLI.
 */
public class AdminController {

    private final ConsoleInputReader reader;
    private final UserSession session;
    private final UserService userService;
    private final BookService bookService;
    private final AuthorService authorService;
    private final CatalogueService catalogueService;
    private final BorrowService borrowService;
    private final HoldService holdService;
    private final ReportService reportService;
    private final ConfigurationRepository configRepository;
    private final AuditLogService auditLogService;

    public AdminController(ConsoleInputReader reader, UserSession session,
                           UserService userService, BookService bookService,
                           AuthorService authorService, CatalogueService catalogueService,
                           BorrowService borrowService, HoldService holdService,
                           ReportService reportService, ConfigurationRepository configRepository,
                           AuditLogService auditLogService) {
        this.reader = reader;
        this.session = session;
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

    public void run() {
        while (session.isLoggedIn() && session.isAdmin()) {
            ConsoleRenderer.printHeader("ADMINISTRATION PANEL");
            System.out.println("  1. User Management");
            System.out.println("  2. Book Management");
            System.out.println("  3. Author Management");
            System.out.println("  4. Catalogue Sections");
            System.out.println("  5. Borrowing & Returns Management");
            System.out.println("  6. Hold Queue Management");
            System.out.println("  7. Reports & Export");
            System.out.println("  8. System Configuration");
            System.out.println("  9. System Audit Logs");
            System.out.println("  0. Logout");

            int choice = reader.readInt("\nChoose an option (0-9): ", 0, 9);
            switch (choice) {
                case 1 -> userManagementMenu();
                case 2 -> bookManagementMenu();
                case 3 -> authorManagementMenu();
                case 4 -> sectionManagementMenu();
                case 5 -> borrowingManagementMenu();
                case 6 -> holdManagementMenu();
                case 7 -> reportsMenu();
                case 8 -> configurationMenu();
                case 9 -> viewAuditLogs();
                case 0 -> {
                    session.logout();
                    ConsoleRenderer.printInfo("Administrator logged out.");
                    return;
                }
            }
        }
    }

    // =========================================================================
    // 1. USER MANAGEMENT
    // =========================================================================
    private void userManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("USER MANAGEMENT");
            System.out.println("  1. View all users");
            System.out.println("  2. View pending registrations");
            System.out.println("  3. Approve user registration");
            System.out.println("  4. Reject user registration");
            System.out.println("  5. Block user");
            System.out.println("  6. Unblock user");
            System.out.println("  7. Suspend user");
            System.out.println("  8. Restore user (from suspended/archived)");
            System.out.println("  9. Archive user");
            System.out.println("  10. Delete user (permanent, if safe)");
            System.out.println("  11. Search user");
            System.out.println("  12. Add new user");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-12): ", 0, 12);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> listUsers(userService.getAllUsers(), "ALL REGISTERED USERS");
                    case 2 -> listUsers(userService.getPendingUsers(), "PENDING REGISTRATIONS");
                    case 3 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to approve: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        userService.approveUser(u.getId(), session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " (" + u.getId() + ") approved successfully.");
                    }
                    case 4 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to reject: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        String reason = reader.readLine("Enter rejection reason: ");
                        userService.rejectUser(u.getId(), session.getCurrentUser().getUsername(), reason);
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " registration rejected.");
                    }
                    case 5 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to block: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        String reason = reader.readLine("Enter block reason: ");
                        userService.blockUser(u.getId(), session.getCurrentUser().getUsername(), reason);
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " blocked.");
                    }
                    case 6 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to unblock: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        userService.unblockUser(u.getId(), session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " unblocked.");
                    }
                    case 7 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to suspend: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        String reason = reader.readLine("Enter suspension reason: ");
                        userService.suspendUser(u.getId(), session.getCurrentUser().getUsername(), reason);
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " suspended.");
                    }
                    case 8 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to restore: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        userService.restoreUser(u.getId(), session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("User " + u.getUsername() + " restored to APPROVED.");
                    }
                    case 9 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to archive: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        if (reader.readConfirmation("Are you sure you want to archive user " + u.getUsername() + "?")) {
                            userService.archiveUser(u.getId(), session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("User " + u.getUsername() + " archived.");
                        }
                    }
                    case 10 -> {
                        String id = reader.readNonEmptyString("Enter User ID or Username to delete: ", "User identifier");
                        User u = findUserByIdOrUsername(id);
                        if (reader.readConfirmation("WARNING: Permanent deletion cannot be undone. Delete user " + u.getUsername() + "?")) {
                            userService.deleteUser(u.getId(), session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("User " + u.getUsername() + " deleted.");
                        }
                    }
                    case 11 -> {
                        String q = reader.readNonEmptyString("Enter search keyword (name/username/email): ", "Query");
                        listUsers(userService.searchUsers(q), "SEARCH RESULTS: '" + q + "'");
                    }
                    case 12 -> {
                        ConsoleRenderer.printSubHeader("ADD NEW USER");
                        String username = reader.readNonEmptyString("Username: ", "Username");
                        char[] password = reader.readPassword("Password (min 8 chars, 1 letter, 1 digit): ");
                        String fullName = reader.readNonEmptyString("Full Name: ", "Full Name");
                        String email = reader.readNonEmptyString("Email Address: ", "Email");
                        String phone = reader.readLine("Phone Number: ");
                        System.out.println("Select Role:");
                        System.out.println("  1. MEMBER");
                        System.out.println("  2. ADMIN");
                        int rChoice = reader.readInt("Choice (1-2): ", 1, 2);
                        Role role = (rChoice == 2) ? Role.ADMIN : Role.MEMBER;
                        User created = userService.createUser(username, password, fullName, email, phone, role, UserState.APPROVED, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("User " + created.getUsername() + " (" + created.getId() + ") created successfully with role " + role + " and state APPROVED.");
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    private void listUsers(List<User> users, String title) {
        ConsoleRenderer.printSubHeader(title);
        if (users.isEmpty()) {
            System.out.println("  (No users found)");
            return;
        }
        System.out.printf("  %-10s | %-16s | %-22s | %-8s | %-12s\n", "User ID", "Username", "Full Name", "Role", "State");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(16) + "-+-" + "-".repeat(22) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(12));
        for (User u : users) {
            System.out.printf("  %-10s | %-16s | %-22s | %-8s | %-12s\n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getState());
        }
    }

    private User findUserByIdOrUsername(String idOrUser) {
        try {
            return userService.getUserById(idOrUser.trim());
        } catch (Exception e) {
            return userService.getUserByUsername(idOrUser.trim());
        }
    }

    // =========================================================================
    // 2. BOOK MANAGEMENT
    // =========================================================================
    private void bookManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("BOOK & INVENTORY MANAGEMENT");
            System.out.println("  1. Browse all books");
            System.out.println("  2. Search books (with filters)");
            System.out.println("  3. View book details & copies");
            System.out.println("  4. Add new book");
            System.out.println("  5. Edit book");
            System.out.println("  6. Archive book");
            System.out.println("  7. Restore book");
            System.out.println("  8. Delete book (permanent, if safe)");
            System.out.println("  9. Add copies to book");
            System.out.println("  10. Update copy status / location");
            System.out.println("  11. Remove copy");
            System.out.println("  12. Move book between sections");
            System.out.println("  13. View inventory summary");
            System.out.println("  14. View book borrowing & hold stats");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-14): ", 0, 14);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> ConsoleRenderer.printBookList(bookService.getAllBookDetails());
                    case 2 -> searchBooksInteractive();
                    case 3 -> {
                        String id = reader.readNonEmptyString("Enter Book ID: ", "Book ID");
                        ConsoleRenderer.printBookDetail(bookService.getBookDetail(id));
                    }
                    case 4 -> addNewBook();
                    case 5 -> editBook();
                    case 6 -> {
                        String id = reader.readNonEmptyString("Enter Book ID to archive: ", "Book ID");
                        bookService.archiveBook(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Book " + id + " archived.");
                    }
                    case 7 -> {
                        String id = reader.readNonEmptyString("Enter Book ID to restore: ", "Book ID");
                        bookService.restoreBook(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Book " + id + " restored.");
                    }
                    case 8 -> {
                        String id = reader.readNonEmptyString("Enter Book ID to delete: ", "Book ID");
                        if (reader.readConfirmation("Are you sure you want to permanently delete book " + id + "?")) {
                            bookService.deleteBook(id, session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("Book " + id + " deleted.");
                        }
                    }
                    case 9 -> addCopiesInteractive();
                    case 10 -> updateCopyStatusInteractive();
                    case 11 -> {
                        String copyId = reader.readNonEmptyString("Enter Copy ID to remove: ", "Copy ID");
                        if (reader.readConfirmation("Are you sure you want to remove copy " + copyId + "?")) {
                            bookService.removeCopy(copyId, session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("Copy " + copyId + " removed.");
                        }
                    }
                    case 12 -> {
                        String bookId = reader.readNonEmptyString("Enter Book ID: ", "Book ID");
                        Book book = bookService.getBookById(bookId);
                        System.out.println("Current section: " + (book.getSectionId().isBlank() ? "None" : book.getSectionId()));
                        List<CatalogueSection> sections = catalogueService.getAllSections();
                        if (sections.isEmpty()) {
                            System.out.println("No sections exist yet. Create a section first.");
                        } else {
                            System.out.println("Available Sections:");
                            for (CatalogueSection s : sections) {
                                System.out.printf("  [%s] %s\n", s.getId(), s.getName());
                            }
                            String newSecId = reader.readOptionalString("Enter new Section ID (or leave blank to unassign): ", "");
                            bookService.moveBookToSection(bookId, newSecId, session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("Book '" + book.getTitle() + "' moved to section: " + (newSecId.isBlank() ? "Unassigned" : newSecId));
                        }
                    }
                    case 13 -> {
                        ConsoleRenderer.printSubHeader("PHYSICAL INVENTORY SUMMARY");
                        java.util.Map<String, Integer> inv = bookService.getInventorySummary();
                        System.out.printf("  Total Distinct Titles : %d\n", inv.getOrDefault("TOTAL_BOOKS", 0));
                        System.out.printf("  Total Physical Copies : %d\n", inv.getOrDefault("TOTAL_COPIES", 0));
                        System.out.println("  ---------------------------------------");
                        System.out.printf("  Available on Shelf    : %d\n", inv.getOrDefault("AVAILABLE", 0));
                        System.out.printf("  Currently Borrowed    : %d\n", inv.getOrDefault("BORROWED", 0));
                        System.out.printf("  Reserved for Holds    : %d\n", inv.getOrDefault("RESERVED", 0));
                        System.out.printf("  Lost Copies           : %d\n", inv.getOrDefault("LOST", 0));
                        System.out.printf("  Damaged Copies        : %d\n", inv.getOrDefault("DAMAGED", 0));
                        System.out.printf("  Archived Copies       : %d\n", inv.getOrDefault("ARCHIVED", 0));
                    }
                    case 14 -> {
                        String bookId = reader.readNonEmptyString("Enter Book ID: ", "Book ID");
                        BookDetail detail = bookService.getBookDetail(bookId);
                        ConsoleRenderer.printSubHeader("BORROWING & HOLD STATS: " + detail.getBook().getTitle());
                        System.out.printf("  Book ID               : %s\n", detail.getBook().getId());
                        System.out.printf("  ISBN                  : %s\n", detail.getBook().getIsbn());
                        System.out.printf("  Total Copies          : %d\n", detail.getTotalCopies());
                        System.out.printf("  Available Copies      : %d\n", detail.getAvailableCopies());
                        System.out.printf("  Borrowed Copies       : %d\n", detail.getBorrowedCopies());
                        System.out.printf("  Reserved Copies       : %d\n", detail.getReservedCopies());
                        int activeHolds = holdService.getActiveHoldsForBook(bookId).size();
                        System.out.printf("  Active Hold Queue     : %d person(s) waiting\n", activeHolds);
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    private void addNewBook() {
        ConsoleRenderer.printSubHeader("ADD NEW BOOK");
        String title = reader.readNonEmptyString("Title: ", "Title");
        String isbn = reader.readLine("ISBN: ");
        String description = reader.readLine("Description: ");
        String publisher = reader.readLine("Publisher: ");
        LocalDate pubDate = reader.readDate("Publication Date (YYYY-MM-DD): ", true);
        String edition = reader.readOptionalString("Edition (default: 1st): ", "1st");
        String language = reader.readOptionalString("Language (default: English): ", "English");

        // Section selection
        List<CatalogueSection> sections = catalogueService.getAllSections();
        String sectionId = "";
        if (!sections.isEmpty()) {
            System.out.println("Available Sections:");
            for (CatalogueSection s : sections) {
                System.out.printf("  [%s] %s\n", s.getId(), s.getName());
            }
            sectionId = reader.readOptionalString("Enter Section ID (or leave blank): ", "");
        }

        // Author selection
        List<Author> authors = authorService.getAllAuthors();
        List<String> authorIds = new ArrayList<>();
        if (!authors.isEmpty()) {
            System.out.println("Available Authors:");
            for (Author a : authors) {
                System.out.printf("  [%s] %s\n", a.getId(), a.getName());
            }
            String aInput = reader.readLine("Enter Author ID(s) comma-separated (e.g. AUT-1001, AUT-1002): ");
            if (aInput != null && !aInput.isBlank()) {
                for (String part : aInput.split(",")) {
                    String clean = part.trim();
                    if (!clean.isEmpty()) authorIds.add(clean);
                }
            }
        }

        int copies = reader.readInt("Initial physical copies count (0-50): ", 0, 50);
        String loc = "Main Stacks";
        if (copies > 0) {
            loc = reader.readOptionalString("Shelf / Location (default: Main Stacks): ", "Main Stacks");
        }

        Book book = bookService.createBook(
                isbn, title, description, authorIds, sectionId, publisher,
                pubDate, language, edition, copies, loc, BookCondition.GOOD,
                session.getCurrentUser().getUsername()
        );
        ConsoleRenderer.printSuccess("Created book: " + book.getTitle() + " (ID: " + book.getId() + ")");
    }

    private void editBook() {
        String id = reader.readNonEmptyString("Enter Book ID to edit: ", "Book ID");
        Book book = bookService.getBookById(id);
        ConsoleRenderer.printSubHeader("EDIT BOOK: " + book.getTitle());

        String title = reader.readOptionalString("Title [" + book.getTitle() + "]: ", book.getTitle());
        String isbn = reader.readOptionalString("ISBN [" + book.getIsbn() + "]: ", book.getIsbn());
        String desc = reader.readOptionalString("Description [" + book.getDescription() + "]: ", book.getDescription());
        String pub = reader.readOptionalString("Publisher [" + book.getPublisher() + "]: ", book.getPublisher());
        LocalDate pubDate = reader.readDate("Publication Date [" + book.getPublicationDate() + "] (leave blank to keep): ", true);
        if (pubDate == null) pubDate = book.getPublicationDate();
        String edition = reader.readOptionalString("Edition [" + book.getEdition() + "]: ", book.getEdition());
        String lang = reader.readOptionalString("Language [" + book.getLanguage() + "]: ", book.getLanguage());

        bookService.updateBook(
                id, isbn, title, desc, book.getAuthorIds(), book.getSectionId(),
                pub, pubDate, lang, edition, session.getCurrentUser().getUsername()
        );
        ConsoleRenderer.printSuccess("Book " + id + " updated successfully.");
    }

    private void addCopiesInteractive() {
        while (true) {
            ConsoleRenderer.printSubHeader("ADD COPIES - SELECT STORAGE SHELF / LOCATION");
            List<BookService.ShelfSummary> shelves = bookService.getShelvesSummary();

            if (shelves.isEmpty()) {
                System.out.println("  (No shelves currently have inventory in the library)");
                System.out.println("  1. Enter a new shelf name to add copies");
                System.out.println("  0. Back to Book Management Menu");
                int choice = reader.readInt("\nChoice (0-1): ", 0, 1);
                if (choice == 0) return;
                String newShelf = reader.readNonEmptyString("Enter shelf/storage location name: ", "Shelf name");
                boolean keepGoing = manageShelfBookCopies(newShelf);
                if (!keepGoing) return;
            } else {
                System.out.println("Current available shelves where books are kept:");
                for (int i = 0; i < shelves.size(); i++) {
                    BookService.ShelfSummary s = shelves.get(i);
                    System.out.printf("  %d. %-28s [%d book(s), %d copy/copies on shelf, %d available]\n",
                            (i + 1), s.shelfName(), s.distinctBooks(), s.totalCopies(), s.availableCopies());
                }
                int optNewShelf = shelves.size() + 1;
                System.out.printf("  %d. Enter a new / different shelf name\n", optNewShelf);
                System.out.println("  0. Back to Book Management Menu");

                int choice = reader.readInt("\nSelect shelf to view books (0-" + optNewShelf + "): ", 0, optNewShelf);
                if (choice == 0) return;

                String selectedShelf;
                if (choice == optNewShelf) {
                    selectedShelf = reader.readNonEmptyString("Enter new shelf/storage location: ", "Shelf name");
                } else {
                    selectedShelf = shelves.get(choice - 1).shelfName();
                }

                boolean keepGoing = manageShelfBookCopies(selectedShelf);
                if (!keepGoing) return;
            }
        }
    }

    private boolean manageShelfBookCopies(String selectedShelf) {
        while (true) {
            ConsoleRenderer.printSubHeader("BOOKS ON SHELF: " + selectedShelf.toUpperCase());
            List<BookService.BookOnShelf> booksOnShelf = bookService.getBooksOnShelf(selectedShelf);

            if (booksOnShelf.isEmpty()) {
                System.out.println("  (No books currently located on this shelf)");
                System.out.println("  1. Select a book from catalogue to place on this shelf");
                System.out.println("  0. Back to Shelf Selection");
                int choice = reader.readInt("\nChoice (0-1): ", 0, 1);
                if (choice == 0) return true; // go back to shelf selection

                Book selectedBook = selectBookFromCatalogue();
                if (selectedBook == null) continue; // back button pressed in catalogue
                processAddCopies(selectedBook, selectedShelf);
            } else {
                System.out.println("Books on this shelf:");
                System.out.printf("  %-4s | %-10s | %-28s | %-15s | %-12s\n", "#", "Book ID", "Title", "Copies on Shelf", "Available");
                System.out.println("  " + "-".repeat(4) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(28) + "-+-" + "-".repeat(15) + "-+-" + "-".repeat(12));
                for (int i = 0; i < booksOnShelf.size(); i++) {
                    BookService.BookOnShelf bos = booksOnShelf.get(i);
                    String title = bos.book().getTitle();
                    if (title.length() > 28) title = title.substring(0, 25) + "...";
                    System.out.printf("  %-4d | %-10s | %-28s | %-15d | %-12d\n",
                            (i + 1), bos.book().getId(), title, bos.totalOnShelf(), bos.availableOnShelf());
                }
                int optAnyBook = booksOnShelf.size() + 1;
                System.out.printf("  %d. Select another book from catalogue to add to this shelf\n", optAnyBook);
                System.out.println("  0. Back to Shelf Selection");

                int choice = reader.readInt("\nSelect book to add copies (0-" + optAnyBook + "): ", 0, optAnyBook);
                if (choice == 0) return true; // go back to shelf selection

                Book selectedBook;
                if (choice == optAnyBook) {
                    selectedBook = selectBookFromCatalogue();
                    if (selectedBook == null) continue;
                } else {
                    selectedBook = booksOnShelf.get(choice - 1).book();
                }

                processAddCopies(selectedBook, selectedShelf);
            }
        }
    }

    private Book selectBookFromCatalogue() {
        List<Book> allBooks = bookService.getAllBooks();
        if (allBooks.isEmpty()) {
            ConsoleRenderer.printWarning("No books exist in the library catalogue. Add a book first.");
            reader.waitForEnter();
            return null;
        }

        ConsoleRenderer.printSubHeader("SELECT BOOK FROM CATALOGUE");
        System.out.printf("  %-4s | %-10s | %-32s | %-14s\n", "#", "Book ID", "Title", "ISBN");
        System.out.println("  " + "-".repeat(4) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(32) + "-+-" + "-".repeat(14));
        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            String title = b.getTitle();
            if (title.length() > 32) title = title.substring(0, 29) + "...";
            System.out.printf("  %-4d | %-10s | %-32s | %-14s\n", (i + 1), b.getId(), title, b.getIsbn());
        }
        System.out.println("  0. Back (Cancel selection)");

        int bChoice = reader.readInt("\nSelect book by number (0-" + allBooks.size() + ") or enter 0 to go back: ", 0, allBooks.size());
        if (bChoice == 0) return null;
        return allBooks.get(bChoice - 1);
    }

    private void processAddCopies(Book selectedBook, String selectedShelf) {
        ConsoleRenderer.printSubHeader("ADD COPIES: " + selectedBook.getTitle().toUpperCase());
        System.out.printf("  Book ID       : %s\n", selectedBook.getId());
        System.out.printf("  Title         : %s\n", selectedBook.getTitle());
        System.out.printf("  ISBN          : %s\n", selectedBook.getIsbn());
        System.out.printf("  Target Shelf  : %s\n", selectedShelf);
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("  (Enter 0 to cancel and go back)");
        int count = reader.readInt("Enter number of copies to add (0-100): ", 0, 100);
        if (count == 0) {
            ConsoleRenderer.printInfo("Addition cancelled.");
            return;
        }

        System.out.println("\nSelect initial copy condition:");
        System.out.println("  1. NEW");
        System.out.println("  2. GOOD (default)");
        System.out.println("  3. FAIR");
        System.out.println("  4. POOR");
        System.out.println("  0. Cancel and go back");
        int cChoice = reader.readInt("Condition (0-4): ", 0, 4);
        if (cChoice == 0) {
            ConsoleRenderer.printInfo("Addition cancelled.");
            return;
        }
        BookCondition condition = switch (cChoice) {
            case 1 -> BookCondition.NEW;
            case 3 -> BookCondition.FAIR;
            case 4 -> BookCondition.POOR;
            default -> BookCondition.GOOD;
        };

        String loc = reader.readOptionalString("Confirm shelf location [" + selectedShelf + "]: ", selectedShelf);

        bookService.addCopies(selectedBook.getId(), count, loc, condition, session.getCurrentUser().getUsername());
        BookDetail updated = bookService.getBookDetail(selectedBook.getId());
        ConsoleRenderer.printSuccess("Successfully added " + count + " copy/copies to book '" + selectedBook.getTitle() + "' on shelf '" + loc + "'.\n" +
                "  Updated Total Copies for Book: " + updated.getTotalCopies() + " (" + updated.getAvailableCopies() + " available)");
        reader.waitForEnter();
    }

    private void updateCopyStatusInteractive() {
        String copyId = reader.readNonEmptyString("Enter Copy ID: ", "Copy ID");
        BookCopy copy = bookService.getCopyById(copyId);
        System.out.println("Current status: " + copy.getStatus() + ", Condition: " + copy.getCondition() + ", Location: " + copy.getLocation());

        System.out.println("Choose new status:");
        System.out.println("  1. AVAILABLE");
        System.out.println("  2. RESERVED");
        System.out.println("  3. LOST");
        System.out.println("  4. DAMAGED");
        System.out.println("  5. ARCHIVED");
        System.out.println("  0. Keep current (" + copy.getStatus() + ")");
        int sc = reader.readInt("Choice (0-5): ", 0, 5);

        CopyStatus newStatus = copy.getStatus();
        switch (sc) {
            case 1 -> newStatus = CopyStatus.AVAILABLE;
            case 2 -> newStatus = CopyStatus.RESERVED;
            case 3 -> newStatus = CopyStatus.LOST;
            case 4 -> newStatus = CopyStatus.DAMAGED;
            case 5 -> newStatus = CopyStatus.ARCHIVED;
        }

        String loc = reader.readOptionalString("New location [" + copy.getLocation() + "]: ", copy.getLocation());
        String notes = reader.readLine("Notes: ");

        bookService.updateCopyStatus(copyId, newStatus, copy.getCondition(), loc, notes, session.getCurrentUser().getUsername());
        ConsoleRenderer.printSuccess("Copy " + copyId + " updated.");
    }

    private void searchBooksInteractive() {
        String q = reader.readLine("Search query (Title, Author, ISBN, Keyword): ");
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

        boolean advanced = reader.readConfirmation("Apply additional filters (Section, Author, Date Range)?");
        String secFilter = null;
        String autFilter = null;
        Integer yrFilter = null;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (advanced) {
            secFilter = reader.readOptionalString("Filter by Section ID (or leave blank): ", null);
            autFilter = reader.readOptionalString("Filter by Author ID (or leave blank): ", null);
            yrFilter = reader.readOptionalInt("Filter by Publication Year (or leave blank): ", null);
            fromDate = reader.readDate("Publication Date From (YYYY-MM-DD, or leave blank): ", true);
            toDate = reader.readDate("Publication Date To (YYYY-MM-DD, or leave blank): ", true);
        }

        boolean onlyAvail = reader.readConfirmation("Show available books only?");
        List<BookDetail> results = bookService.searchBooks(q, secFilter, autFilter, yrFilter, fromDate, toDate, onlyAvail, sort);
        ConsoleRenderer.printBookList(results);
    }

    // =========================================================================
    // 3. AUTHOR MANAGEMENT
    // =========================================================================
    private void authorManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("AUTHOR MANAGEMENT");
            System.out.println("  1. View all authors");
            System.out.println("  2. Add new author");
            System.out.println("  3. Edit author");
            System.out.println("  4. Archive author");
            System.out.println("  5. Restore author");
            System.out.println("  6. Delete author (permanent, if no books attached)");
            System.out.println("  7. List books by author");
            System.out.println("  8. Search author");
            System.out.println("  9. View author details");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-9): ", 0, 9);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> {
                        List<Author> authors = authorService.getAllAuthors();
                        ConsoleRenderer.printSubHeader("ALL AUTHORS");
                        if (authors.isEmpty()) System.out.println("  (No authors found)");
                        for (Author a : authors) {
                            System.out.printf("  [%-8s] %-25s | Birth Year: %-6s | Archived: %s\n",
                                    a.getId(), a.getName(), (a.getBirthYear() != null ? a.getBirthYear() : "N/A"), a.isArchived());
                        }
                    }
                    case 2 -> {
                        String name = reader.readNonEmptyString("Author Name: ", "Name");
                        String bio = reader.readLine("Biography: ");
                        Integer birth = reader.readOptionalInt("Birth Year: ", null);
                        Author a = authorService.createAuthor(name, bio, birth, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Created author: " + a.getName() + " (" + a.getId() + ")");
                    }
                    case 3 -> {
                        String id = reader.readNonEmptyString("Enter Author ID: ", "Author ID");
                        Author a = authorService.getAuthorById(id);
                        String name = reader.readOptionalString("Name [" + a.getName() + "]: ", a.getName());
                        String bio = reader.readOptionalString("Bio [" + a.getBiography() + "]: ", a.getBiography());
                        Integer birth = reader.readOptionalInt("Birth Year [" + a.getBirthYear() + "]: ", a.getBirthYear());
                        authorService.updateAuthor(id, name, bio, birth, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Author updated.");
                    }
                    case 4 -> {
                        String id = reader.readNonEmptyString("Enter Author ID to archive: ", "Author ID");
                        authorService.archiveAuthor(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Author archived.");
                    }
                    case 5 -> {
                        String id = reader.readNonEmptyString("Enter Author ID to restore: ", "Author ID");
                        authorService.restoreAuthor(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Author restored.");
                    }
                    case 6 -> {
                        String id = reader.readNonEmptyString("Enter Author ID to delete: ", "Author ID");
                        if (reader.readConfirmation("Are you sure you want to delete author " + id + "?")) {
                            authorService.deleteAuthor(id, session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("Author deleted.");
                        }
                    }
                    case 7 -> {
                        String id = reader.readNonEmptyString("Enter Author ID: ", "Author ID");
                        List<Book> books = authorService.getBooksByAuthor(id);
                        System.out.println("Books by author " + id + ":");
                        for (Book b : books) {
                            System.out.printf("  [%s] %s (ISBN: %s)\n", b.getId(), b.getTitle(), b.getIsbn());
                        }
                    }
                    case 8 -> {
                        String q = reader.readNonEmptyString("Enter author search keyword: ", "Query");
                        List<Author> results = authorService.searchAuthors(q);
                        ConsoleRenderer.printSubHeader("AUTHOR SEARCH RESULTS: '" + q + "'");
                        if (results.isEmpty()) {
                            System.out.println("  (No authors matched)");
                        } else {
                            for (Author a : results) {
                                System.out.printf("  [%-8s] %-25s | Birth Year: %-6s | Archived: %s\n",
                                        a.getId(), a.getName(), (a.getBirthYear() != null ? a.getBirthYear() : "N/A"), a.isArchived());
                            }
                        }
                    }
                    case 9 -> {
                        String id = reader.readNonEmptyString("Enter Author ID: ", "Author ID");
                        Author a = authorService.getAuthorById(id);
                        List<Book> books = authorService.getBooksByAuthor(id);
                        ConsoleRenderer.printSubHeader("AUTHOR DETAILS: " + a.getName());
                        System.out.printf("  ID         : %s\n", a.getId());
                        System.out.printf("  Name       : %s\n", a.getName());
                        System.out.printf("  Birth Year : %s\n", a.getBirthYear() != null ? a.getBirthYear() : "N/A");
                        System.out.printf("  Biography  : %s\n", a.getBiography().isBlank() ? "N/A" : a.getBiography());
                        System.out.printf("  Archived   : %s\n", a.isArchived());
                        System.out.printf("  Total Books: %d\n", books.size());
                        for (Book b : books) {
                            System.out.printf("    - [%s] %s\n", b.getId(), b.getTitle());
                        }
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    // =========================================================================
    // 4. CATALOGUE SECTIONS
    // =========================================================================
    private void sectionManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("CATALOGUE SECTIONS");
            System.out.println("  1. View all sections");
            System.out.println("  2. Create section");
            System.out.println("  3. Rename / Edit section");
            System.out.println("  4. Archive section");
            System.out.println("  5. Restore section");
            System.out.println("  6. Delete section");
            System.out.println("  7. View books in section");
            System.out.println("  8. Search sections");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-8): ", 0, 8);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> {
                        List<CatalogueSection> sections = catalogueService.getAllSections();
                        ConsoleRenderer.printSubHeader("CATALOGUE SECTIONS");
                        if (sections.isEmpty()) System.out.println("  (No sections created)");
                        for (CatalogueSection s : sections) {
                            System.out.printf("  [%-8s] %-20s | %s | Archived: %s\n",
                                    s.getId(), s.getName(), s.getDescription(), s.isArchived());
                        }
                    }
                    case 2 -> {
                        String name = reader.readNonEmptyString("Section Name: ", "Name");
                        String desc = reader.readLine("Description: ");
                        CatalogueSection s = catalogueService.createSection(name, desc, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Created section: " + s.getName() + " (" + s.getId() + ")");
                    }
                    case 3 -> {
                        String id = reader.readNonEmptyString("Enter Section ID: ", "Section ID");
                        CatalogueSection s = catalogueService.getSectionById(id);
                        String name = reader.readOptionalString("Name [" + s.getName() + "]: ", s.getName());
                        String desc = reader.readOptionalString("Desc [" + s.getDescription() + "]: ", s.getDescription());
                        catalogueService.updateSection(id, name, desc, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Section updated.");
                    }
                    case 4 -> {
                        String id = reader.readNonEmptyString("Enter Section ID to archive: ", "Section ID");
                        catalogueService.archiveSection(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Section archived.");
                    }
                    case 5 -> {
                        String id = reader.readNonEmptyString("Enter Section ID to restore: ", "Section ID");
                        catalogueService.restoreSection(id, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Section restored.");
                    }
                    case 6 -> {
                        String id = reader.readNonEmptyString("Enter Section ID to delete: ", "Section ID");
                        if (reader.readConfirmation("Are you sure you want to delete section " + id + "?")) {
                            catalogueService.deleteSection(id, session.getCurrentUser().getUsername());
                            ConsoleRenderer.printSuccess("Section deleted.");
                        }
                    }
                    case 7 -> {
                        List<CatalogueSection> sections = catalogueService.getAllSections();
                        if (sections.isEmpty()) {
                            System.out.println("  (No sections created yet)");
                            break;
                        }
                        ConsoleRenderer.printSubHeader("VIEW BOOKS IN SECTION");
                        for (int i = 0; i < sections.size(); i++) {
                            CatalogueSection s = sections.get(i);
                            System.out.printf("  %d. [%s] %s\n", (i + 1), s.getId(), s.getName());
                        }
                        System.out.println("  0. Back to Sections Menu");
                        int sChoice = reader.readInt("\nSelect section by number (0-" + sections.size() + "): ", 0, sections.size());
                        if (sChoice == 0) break;
                        CatalogueSection target = sections.get(sChoice - 1);
                        List<Book> books = catalogueService.getBooksBySection(target.getId());
                        ConsoleRenderer.printSubHeader("BOOKS IN SECTION: " + target.getName().toUpperCase());
                        if (books.isEmpty()) {
                            System.out.println("  (No books currently assigned to this section)");
                        } else {
                            for (Book b : books) {
                                BookDetail d = bookService.getBookDetail(b.getId());
                                System.out.printf("  [%s] %-28s | Available: %d / %d\n", 
                                        b.getId(), b.getTitle(), d.getAvailableCopies(), d.getTotalCopies());
                            }
                        }
                    }
                    case 8 -> {
                        String q = reader.readNonEmptyString("Enter section search keyword: ", "Query");
                        List<CatalogueSection> results = catalogueService.searchSections(q);
                        ConsoleRenderer.printSubHeader("SECTION SEARCH RESULTS: '" + q + "'");
                        if (results.isEmpty()) {
                            System.out.println("  (No sections matched)");
                        } else {
                            for (CatalogueSection s : results) {
                                System.out.printf("  [%-8s] %-20s | %s | Archived: %s\n",
                                        s.getId(), s.getName(), s.getDescription(), s.isArchived());
                            }
                        }
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    // =========================================================================
    // 5. BORROWING & CIRCULATION
    // =========================================================================
    private void borrowingManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("BORROWING & CIRCULATION MANAGEMENT");
            System.out.println("  1. View active loans");
            System.out.println("  2. View all loans (including returned)");
            System.out.println("  3. View overdue loans");
            System.out.println("  4. Process return of a book");
            System.out.println("  5. Renew a loan");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-5): ", 0, 5);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> printLoans(borrowService.getActiveLoans(), "ACTIVE LOANS");
                    case 2 -> printLoans(borrowService.getAllLoans(), "ALL LOAN RECORDS");
                    case 3 -> printLoans(borrowService.getOverdueLoans(LocalDate.now()), "OVERDUE LOANS");
                    case 4 -> {
                        String loanId = reader.readNonEmptyString("Enter Loan ID (e.g. BRW-1001): ", "Loan ID");
                        String notes = reader.readLine("Return notes (optional): ");
                        borrowService.returnBook(loanId, session.getCurrentUser().getUsername(), true, BookCondition.GOOD, notes);
                        ConsoleRenderer.printSuccess("Loan " + loanId + " returned successfully. Inventory updated.");
                    }
                    case 5 -> {
                        String loanId = reader.readNonEmptyString("Enter Loan ID to renew: ", "Loan ID");
                        borrowService.renewLoan(loanId, session.getCurrentUser().getUsername(), true);
                        ConsoleRenderer.printSuccess("Loan " + loanId + " renewed.");
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    private void printLoans(List<BorrowRecord> loans, String title) {
        ConsoleRenderer.printSubHeader(title);
        if (loans.isEmpty()) {
            System.out.println("  (No loans found)");
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.printf("  %-10s | %-10s | %-10s | %-12s | %-12s | %-10s | %-10s\n",
                "Loan ID", "User ID", "Copy ID", "Borrow Date", "Due Date", "Renewals", "Status");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(10));
        for (BorrowRecord r : loans) {
            String status = r.isOverdue(today) ? "OVERDUE" : r.getStatus().name();
            System.out.printf("  %-10s | %-10s | %-10s | %-12s | %-12s | %-10d | %-10s\n",
                    r.getBorrowId(), r.getUserId(), r.getCopyId(), r.getBorrowDate(), r.getDueDate(), r.getRenewalCount(), status);
        }
    }

    // =========================================================================
    // 6. HOLD QUEUE MANAGEMENT
    // =========================================================================
    private void holdManagementMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("HOLD & RESERVATION QUEUE");
            System.out.println("  1. View all holds");
            System.out.println("  2. View active holds for a specific book");
            System.out.println("  3. Process expired holds");
            System.out.println("  4. Cancel a hold");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-4): ", 0, 4);
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> printHolds(holdService.getAllHolds(), "ALL HOLDS");
                    case 2 -> {
                        String bookId = reader.readNonEmptyString("Enter Book ID: ", "Book ID");
                        printHolds(holdService.getActiveHoldsForBook(bookId), "ACTIVE HOLDS FOR BOOK " + bookId);
                    }
                    case 3 -> {
                        holdService.processExpiredHolds();
                        ConsoleRenderer.printSuccess("Expired holds processed and released.");
                    }
                    case 4 -> {
                        String holdId = reader.readNonEmptyString("Enter Hold ID: ", "Hold ID");
                        holdService.cancelHold(holdId, session.getCurrentUser().getUsername(), true);
                        ConsoleRenderer.printSuccess("Hold " + holdId + " cancelled.");
                    }
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    private void printHolds(List<HoldRecord> holds, String title) {
        ConsoleRenderer.printSubHeader(title);
        if (holds.isEmpty()) {
            System.out.println("  (No holds found)");
            return;
        }
        System.out.printf("  %-10s | %-10s | %-10s | %-8s | %-14s | %-12s | %-10s\n",
                "Hold ID", "User ID", "Book ID", "Priority", "Reserved Copy", "Requested", "Status");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(10) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(14) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(10));
        for (HoldRecord h : holds) {
            System.out.printf("  %-10s | %-10s | %-10s | #%-7d | %-14s | %-12s | %-10s\n",
                    h.getHoldId(), h.getUserId(), h.getBookId(), h.getPriority(),
                    (h.getReservedCopyId() != null ? h.getReservedCopyId() : "-"),
                    h.getRequestDate().toLocalDate(), h.getStatus());
        }
    }

    // =========================================================================
    // 7. REPORTS & EXPORT
    // =========================================================================
    private void reportsMenu() {
        while (true) {
            ConsoleRenderer.printSubHeader("SYSTEM REPORTS & EXPORT");
            System.out.println("  1. Inventory & Stock Report");
            System.out.println("  2. User Demographics Report");
            System.out.println("  3. Borrowing & Circulation Report");
            System.out.println("  4. Catalogue Classification Report");
            System.out.println("  5. Hold Queue Report");
            System.out.println("  6. Export Complete Report Bundle (.ZIP)");
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose an option (0-6): ", 0, 6);
            if (choice == 0) return;

            try {
                if (choice >= 1 && choice <= 5) {
                    ReportData report = switch (choice) {
                        case 1 -> reportService.generateInventoryReport();
                        case 2 -> reportService.generateUserReport();
                        case 3 -> reportService.generateBorrowingReport();
                        case 4 -> reportService.generateCatalogueReport();
                        default -> reportService.generateHoldReport();
                    };

                    System.out.println(ReportFormatter.toPlainText(report));

                    if (reader.readConfirmation("Export this report to a file?")) {
                        System.out.println("  1. Plain Text (.txt)");
                        System.out.println("  2. CSV Spreadsheet (.csv)");
                        int fmtChoice = reader.readInt("Format choice (1-2): ", 1, 2);
                        String fmt = (fmtChoice == 2) ? "csv" : "txt";
                        Path path = reportService.exportReport(report, fmt, session.getCurrentUser().getUsername());
                        ConsoleRenderer.printSuccess("Report generated successfully.\n  File:\n  " + path);
                    }
                } else if (choice == 6) {
                    Path zipPath = reportService.exportAllReportsBundle(session.getCurrentUser().getUsername());
                    ConsoleRenderer.printSuccess("Complete reports bundle generated successfully.\n  File:\n  " + zipPath);
                }
            } catch (Exception e) {
                ConsoleRenderer.printError("Failed to generate or export report: " + e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    // =========================================================================
    // 8. CONFIGURATION
    // =========================================================================
    private void configurationMenu() {
        while (true) {
            LibraryConfiguration config = configRepository.get();
            ConsoleRenderer.printSubHeader("LIBRARY SYSTEM CONFIGURATION");
            System.out.printf("  1. System Name                : %s\n", config.getSystemName());
            System.out.printf("  2. Maximum Active Loans       : %d books\n", config.getMaxActiveLoans());
            System.out.printf("  3. Default Loan Duration      : %d days\n", config.getDefaultLoanDurationDays());
            System.out.printf("  4. Maximum Renewals           : %d times\n", config.getMaxRenewals());
            System.out.printf("  5. Renewal Period Duration    : %d days\n", config.getRenewalPeriodDays());
            System.out.printf("  6. Maximum Active Holds       : %d holds\n", config.getMaxActiveHolds());
            System.out.printf("  7. Hold Pickup Expiry Window  : %d days\n", config.getHoldExpiryDays());
            System.out.println("  0. Back to Admin Panel");

            int choice = reader.readInt("\nChoose a setting to modify (0-7): ", 0, 7);
            if (choice == 0) return;

            switch (choice) {
                case 1 -> {
                    String name = reader.readNonEmptyString("New System Name: ", "Name");
                    config.setSystemName(name);
                }
                case 2 -> {
                    int val = reader.readInt("New Max Active Loans (1-50): ", 1, 50);
                    config.setMaxActiveLoans(val);
                }
                case 3 -> {
                    int val = reader.readInt("New Default Loan Duration in Days (1-180): ", 1, 180);
                    config.setDefaultLoanDurationDays(val);
                }
                case 4 -> {
                    int val = reader.readInt("New Max Renewals (0-10): ", 0, 10);
                    config.setMaxRenewals(val);
                }
                case 5 -> {
                    int val = reader.readInt("New Renewal Period in Days (1-180): ", 1, 180);
                    config.setRenewalPeriodDays(val);
                }
                case 6 -> {
                    int val = reader.readInt("New Max Active Holds (1-20): ", 1, 20);
                    config.setMaxActiveHolds(val);
                }
                case 7 -> {
                    int val = reader.readInt("New Hold Pickup Expiry in Days (1-30): ", 1, 30);
                    config.setHoldExpiryDays(val);
                }
            }
            configRepository.save(config);
            auditLogService.log("CONFIG_UPDATED", session.getCurrentUser().getUsername(), "CONFIG", "Updated library settings", true);
            ConsoleRenderer.printSuccess("Configuration saved.");
            reader.waitForEnter();
        }
    }

    // =========================================================================
    // 9. AUDIT LOGS
    // =========================================================================
    private void viewAuditLogs() {
        ConsoleRenderer.printSubHeader("RECENT AUDIT LOGS");
        List<AuditLogEntry> logs = auditLogService.getRecentLogs(30);
        if (logs.isEmpty()) {
            System.out.println("  (No logs recorded yet)");
        } else {
            for (AuditLogEntry l : logs) {
                System.out.println("  " + l.toString());
            }
        }
        reader.waitForEnter();
    }
}
