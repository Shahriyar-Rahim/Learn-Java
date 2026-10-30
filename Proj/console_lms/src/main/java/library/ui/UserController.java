package library.ui;

import library.exception.LibraryException;
import library.model.*;
import library.reporting.ReportData;
import library.reporting.ReportFormatter;
import library.security.UserSession;
import library.service.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Handles member workflows: browsing, borrowing, returns, renewals, holds, and personal reports.
 */
public class UserController {

    private final ConsoleInputReader reader;
    private final UserSession session;
    private final UserService userService;
    private final BookService bookService;
    private final BorrowService borrowService;
    private final HoldService holdService;
    private final ReportService reportService;

    public UserController(ConsoleInputReader reader, UserSession session,
                          UserService userService, BookService bookService,
                          BorrowService borrowService, HoldService holdService,
                          ReportService reportService) {
        this.reader = reader;
        this.session = session;
        this.userService = userService;
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.holdService = holdService;
        this.reportService = reportService;
    }

    public void run() {
        while (session.isLoggedIn()) {
            User current = session.getCurrentUser();
            ConsoleRenderer.printHeader("MY ACCOUNT - " + current.getFullName().toUpperCase());
            System.out.println("  Status: " + current.getState().getDescription());
            System.out.println("--------------------------------------------------------------------------");
            System.out.println("  1. My Profile & Account Status");
            System.out.println("  2. Browse Catalogue");
            System.out.println("  3. Search Books");
            System.out.println("  4. View Book Details");
            System.out.println("  5. Borrow a Book");
            System.out.println("  6. Return a Book");
            System.out.println("  7. Renew a Loan");
            System.out.println("  8. Place a Hold on a Book");
            System.out.println("  9. Cancel My Hold");
            System.out.println("  10. My Current Loans");
            System.out.println("  11. My Borrowing History");
            System.out.println("  12. My Active Holds");
            System.out.println("  13. My Personal Report & Export");
            System.out.println("  14. My Overdue Books");
            System.out.println("  0. Logout");

            int choice = reader.readInt("\nChoose an option (0-14): ", 0, 14);
            if (choice == 0) {
                session.logout();
                ConsoleRenderer.printInfo("You have been logged out.");
                return;
            }

            try {
                switch (choice) {
                    case 1 -> viewProfile();
                    case 2 -> ConsoleRenderer.printBookList(bookService.getAllBookDetails());
                    case 3 -> searchBooksInteractive();
                    case 4 -> {
                        String id = reader.readNonEmptyString("Enter Book ID: ", "Book ID");
                        ConsoleRenderer.printBookDetail(bookService.getBookDetail(id));
                    }
                    case 5 -> borrowBookInteractive();
                    case 6 -> returnBookInteractive();
                    case 7 -> renewBookInteractive();
                    case 8 -> placeHoldInteractive();
                    case 9 -> cancelHoldInteractive();
                    case 10 -> printMyActiveLoans();
                    case 11 -> printMyLoanHistory();
                    case 12 -> printMyActiveHolds();
                    case 13 -> personalReportInteractive();
                    case 14 -> printMyOverdueLoans();
                }
            } catch (LibraryException e) {
                ConsoleRenderer.printError(e.getMessage());
            } catch (Exception e) {
                ConsoleRenderer.printError("Unexpected error: " + e.getMessage());
            }
            reader.waitForEnter();
        }
    }

    private void printMyOverdueLoans() {
        User u = session.getCurrentUser();
        List<BorrowRecord> active = borrowService.getActiveLoansForUser(u.getId());
        LocalDate today = LocalDate.now();
        List<BorrowRecord> overdue = new java.util.ArrayList<>();
        for (BorrowRecord r : active) {
            if (r.isOverdue(today)) {
                overdue.add(r);
            }
        }
        ConsoleRenderer.printSubHeader("MY OVERDUE BOOKS");
        if (overdue.isEmpty()) {
            System.out.println("  Great news! You have no overdue books.");
            return;
        }
        System.out.printf("  %-10s | %-28s | %-12s | %-12s | %-16s\n",
                "Loan ID", "Book Title", "Borrowed On", "Due Date", "Days Overdue");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(28) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(16));
        for (BorrowRecord r : overdue) {
            Book b = bookService.getBookById(r.getBookId());
            String title = b != null ? b.getTitle() : r.getBookId();
            if (title.length() > 28) title = title.substring(0, 25) + "...";
            long days = java.time.temporal.ChronoUnit.DAYS.between(r.getDueDate(), today);
            System.out.printf("  %-10s | %-28s | %-12s | %-12s | %d day(s) overdue\n",
                    r.getBorrowId(), title, r.getBorrowDate(), r.getDueDate(), days);
        }
    }

    private void viewProfile() {
        User u = userService.getUserById(session.getCurrentUser().getId());
        ConsoleRenderer.printSubHeader("MY PROFILE");
        System.out.printf("  User ID     : %s\n", u.getId());
        System.out.printf("  Username    : %s\n", u.getUsername());
        System.out.printf("  Full Name   : %s\n", u.getFullName());
        System.out.printf("  Email       : %s\n", u.getEmail());
        System.out.printf("  Phone       : %s\n", u.getPhone().isEmpty() ? "(Not set)" : u.getPhone());
        System.out.printf("  Role        : %s\n", u.getRole());
        System.out.printf("  State       : %s\n", u.getState().getDescription());
        System.out.printf("  Member Since: %s\n", u.getCreatedAt().toLocalDate());

        int active = borrowService.getActiveLoansForUser(u.getId()).size();
        int holds = holdService.getActiveHoldsForUser(u.getId()).size();
        System.out.printf("  Active Loans: %d\n", active);
        System.out.printf("  Active Holds: %d\n", holds);
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

        boolean advanced = reader.readConfirmation("Apply additional filters (Section, Year, Date Range)?");
        String secFilter = null;
        Integer yrFilter = null;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        if (advanced) {
            secFilter = reader.readOptionalString("Filter by Section ID (or leave blank): ", null);
            yrFilter = reader.readOptionalInt("Filter by Publication Year (or leave blank): ", null);
            fromDate = reader.readDate("Publication Date From (YYYY-MM-DD, or leave blank): ", true);
            toDate = reader.readDate("Publication Date To (YYYY-MM-DD, or leave blank): ", true);
        }

        boolean onlyAvail = reader.readConfirmation("Show available books only?");
        List<BookDetail> results = bookService.searchBooks(q, secFilter, null, yrFilter, fromDate, toDate, onlyAvail, sort);
        ConsoleRenderer.printBookList(results);
    }

    private void borrowBookInteractive() {
        User u = session.getCurrentUser();
        if (!u.canBorrow()) {
            ConsoleRenderer.printWarning("Your account status (" + u.getState().getDescription() + ") does not permit borrowing books.");
            return;
        }

        String bookId = reader.readNonEmptyString("Enter Book ID to borrow: ", "Book ID");
        BorrowRecord record = borrowService.borrowBook(u.getId(), bookId);
        Book b = bookService.getBookById(bookId);
        ConsoleRenderer.printSuccess("Successfully checked out '" + b.getTitle() + "'!\n" +
                "  Loan ID : " + record.getBorrowId() + "\n" +
                "  Copy ID : " + record.getCopyId() + "\n" +
                "  Due Date: " + record.getDueDate());
    }

    private void returnBookInteractive() {
        User u = session.getCurrentUser();
        List<BorrowRecord> active = borrowService.getActiveLoansForUser(u.getId());
        if (active.isEmpty()) {
            ConsoleRenderer.printInfo("You have no active loans to return.");
            return;
        }

        printMyActiveLoans();
        String loanId = reader.readNonEmptyString("Enter Loan ID to return: ", "Loan ID");
        borrowService.returnBook(loanId, u.getId(), false, BookCondition.GOOD, "Returned by member");
        ConsoleRenderer.printSuccess("Book successfully returned! Thank you.");
    }

    private void renewBookInteractive() {
        User u = session.getCurrentUser();
        List<BorrowRecord> active = borrowService.getActiveLoansForUser(u.getId());
        if (active.isEmpty()) {
            ConsoleRenderer.printInfo("You have no active loans to renew.");
            return;
        }

        printMyActiveLoans();
        String loanId = reader.readNonEmptyString("Enter Loan ID to renew: ", "Loan ID");
        borrowService.renewLoan(loanId, u.getId(), false);
        BorrowRecord r = borrowService.getBorrowById(loanId);
        ConsoleRenderer.printSuccess("Loan renewed successfully! New due date is " + r.getDueDate() + ".");
    }

    private void placeHoldInteractive() {
        User u = session.getCurrentUser();
        if (!u.canHold()) {
            ConsoleRenderer.printWarning("Your account status (" + u.getState().getDescription() + ") does not permit placing holds.");
            return;
        }

        String bookId = reader.readNonEmptyString("Enter Book ID to place on hold: ", "Book ID");
        HoldRecord hold = holdService.placeHold(u.getId(), bookId);
        Book b = bookService.getBookById(bookId);
        ConsoleRenderer.printSuccess("Hold placed successfully for '" + b.getTitle() + "'!\n" +
                "  Hold ID      : " + hold.getHoldId() + "\n" +
                "  Queue Position: #" + hold.getPriority());
    }

    private void cancelHoldInteractive() {
        User u = session.getCurrentUser();
        List<HoldRecord> holds = holdService.getActiveHoldsForUser(u.getId());
        if (holds.isEmpty()) {
            ConsoleRenderer.printInfo("You have no active holds to cancel.");
            return;
        }

        printMyActiveHolds();
        String holdId = reader.readNonEmptyString("Enter Hold ID to cancel: ", "Hold ID");
        holdService.cancelHold(holdId, u.getId(), false);
        ConsoleRenderer.printSuccess("Hold " + holdId + " cancelled.");
    }

    private void printMyActiveLoans() {
        User u = session.getCurrentUser();
        List<BorrowRecord> loans = borrowService.getActiveLoansForUser(u.getId());
        ConsoleRenderer.printSubHeader("MY CURRENT LOANS");
        if (loans.isEmpty()) {
            System.out.println("  (No active loans)");
            return;
        }
        LocalDate today = LocalDate.now();
        System.out.printf("  %-10s | %-28s | %-12s | %-12s | %-8s | %-10s\n",
                "Loan ID", "Book Title", "Borrowed On", "Due Date", "Renewals", "Status");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(28) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(10));
        for (BorrowRecord r : loans) {
            Book b = bookService.getBookById(r.getBookId());
            String title = b != null ? b.getTitle() : r.getBookId();
            if (title.length() > 28) title = title.substring(0, 25) + "...";
            String status = r.isOverdue(today) ? "OVERDUE" : "ACTIVE";
            System.out.printf("  %-10s | %-28s | %-12s | %-12s | %-8d | %-10s\n",
                    r.getBorrowId(), title, r.getBorrowDate(), r.getDueDate(), r.getRenewalCount(), status);
        }
    }

    private void printMyLoanHistory() {
        User u = session.getCurrentUser();
        List<BorrowRecord> loans = borrowService.getAllLoansForUser(u.getId());
        ConsoleRenderer.printSubHeader("MY BORROWING HISTORY");
        if (loans.isEmpty()) {
            System.out.println("  (No historical loans)");
            return;
        }
        System.out.printf("  %-10s | %-28s | %-12s | %-12s | %-10s\n",
                "Loan ID", "Book Title", "Borrowed On", "Returned On", "Status");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(28) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(10));
        for (BorrowRecord r : loans) {
            Book b = bookService.getBookById(r.getBookId());
            String title = b != null ? b.getTitle() : r.getBookId();
            if (title.length() > 28) title = title.substring(0, 25) + "...";
            System.out.printf("  %-10s | %-28s | %-12s | %-12s | %-10s\n",
                    r.getBorrowId(), title, r.getBorrowDate(),
                    (r.getReturnDate() != null ? r.getReturnDate().toString() : "-"), r.getStatus());
        }
    }

    private void printMyActiveHolds() {
        User u = session.getCurrentUser();
        List<HoldRecord> holds = holdService.getActiveHoldsForUser(u.getId());
        ConsoleRenderer.printSubHeader("MY ACTIVE HOLDS");
        if (holds.isEmpty()) {
            System.out.println("  (No active holds)");
            return;
        }
        System.out.printf("  %-10s | %-28s | %-8s | %-14s | %-12s\n",
                "Hold ID", "Book Title", "Priority", "Reserved Copy", "Pickup Expiry");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(28) + "-+-" + "-".repeat(8) + "-+-" + "-".repeat(14) + "-+-" + "-".repeat(12));
        for (HoldRecord h : holds) {
            Book b = bookService.getBookById(h.getBookId());
            String title = b != null ? b.getTitle() : h.getBookId();
            if (title.length() > 28) title = title.substring(0, 25) + "...";
            System.out.printf("  %-10s | %-28s | #%-7d | %-14s | %-12s\n",
                    h.getHoldId(), title, h.getPriority(),
                    (h.getReservedCopyId() != null ? h.getReservedCopyId() : "Pending"),
                    (h.getExpiryDate() != null ? h.getExpiryDate().toLocalDate().toString() : "-"));
        }
    }

    private void personalReportInteractive() {
        User u = session.getCurrentUser();
        ReportData report = reportService.generatePersonalUserReport(u.getId());
        System.out.println(ReportFormatter.toPlainText(report));

        if (reader.readConfirmation("Export your personal report to a file?")) {
            System.out.println("  1. Plain Text (.txt)");
            System.out.println("  2. CSV Spreadsheet (.csv)");
            int fmtChoice = reader.readInt("Format choice (1-2): ", 1, 2);
            String fmt = (fmtChoice == 2) ? "csv" : "txt";
            Path path = reportService.exportReport(report, fmt, u.getUsername());
            ConsoleRenderer.printSuccess("Report generated successfully.\n  File:\n  " + path);
        }
    }
}
