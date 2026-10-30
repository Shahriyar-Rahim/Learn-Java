package library.service;

import library.exception.EntityNotFoundException;
import library.model.*;
import library.reporting.ReportData;
import library.reporting.ReportExporter;
import library.repository.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * Generates comprehensive library analytics and reports.
 */
public class ReportService {

    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final HoldRepository holdRepository;
    private final AuthorRepository authorRepository;
    private final SectionRepository sectionRepository;
    private final ReportExporter reportExporter;
    private final AuditLogService auditLogService;

    public ReportService(BookRepository bookRepository, BookCopyRepository copyRepository,
                         UserRepository userRepository, BorrowRepository borrowRepository,
                         HoldRepository holdRepository, AuthorRepository authorRepository,
                         SectionRepository sectionRepository, ReportExporter reportExporter,
                         AuditLogService auditLogService) {
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.borrowRepository = borrowRepository;
        this.holdRepository = holdRepository;
        this.authorRepository = authorRepository;
        this.sectionRepository = sectionRepository;
        this.reportExporter = reportExporter;
        this.auditLogService = auditLogService;
    }

    // -------------------------------------------------------------
    // 1. INVENTORY REPORT
    // -------------------------------------------------------------
    public ReportData generateInventoryReport() {
        ReportData report = new ReportData("Library Inventory & Stock Report");

        List<Book> books = bookRepository.findAll();
        List<BookCopy> copies = copyRepository.findAll();

        int totalBooks = books.size();
        int totalCopies = copies.size();
        int available = 0, borrowed = 0, reserved = 0, lost = 0, damaged = 0, archived = 0;

        for (BookCopy c : copies) {
            switch (c.getStatus()) {
                case AVAILABLE -> available++;
                case BORROWED -> borrowed++;
                case RESERVED -> reserved++;
                case LOST -> lost++;
                case DAMAGED -> damaged++;
                case ARCHIVED -> archived++;
            }
        }

        report.addSummaryStat("Total Book Titles", totalBooks);
        report.addSummaryStat("Total Physical Copies", totalCopies);
        report.addSummaryStat("Available Copies", available);
        report.addSummaryStat("Borrowed Copies", borrowed);
        report.addSummaryStat("Reserved for Hold", reserved);
        report.addSummaryStat("Lost Copies", lost);
        report.addSummaryStat("Damaged Copies", damaged);
        report.addSummaryStat("Archived Copies", archived);

        report.setHeaders(List.of("Book ID", "ISBN", "Title", "Section", "Total Copies", "Available", "Borrowed", "Reserved", "Status"));

        for (Book b : books) {
            List<BookCopy> bCopies = copyRepository.findByBookId(b.getId());
            int cTotal = bCopies.size();
            int cAvail = 0, cBrw = 0, cRes = 0;
            for (BookCopy c : bCopies) {
                if (c.getStatus() == CopyStatus.AVAILABLE) cAvail++;
                else if (c.getStatus() == CopyStatus.BORROWED) cBrw++;
                else if (c.getStatus() == CopyStatus.RESERVED) cRes++;
            }
            CatalogueSection sec = sectionRepository.findById(b.getSectionId()).orElse(null);
            report.addRow(List.of(
                    b.getId(),
                    b.getIsbn(),
                    b.getTitle(),
                    sec != null ? sec.getName() : "N/A",
                    String.valueOf(cTotal),
                    String.valueOf(cAvail),
                    String.valueOf(cBrw),
                    String.valueOf(cRes),
                    b.isArchived() ? "ARCHIVED" : "ACTIVE"
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // 2. USER REPORT
    // -------------------------------------------------------------
    public ReportData generateUserReport() {
        ReportData report = new ReportData("Library User Demographics & State Report");

        List<User> users = userRepository.findAll();
        int total = users.size();
        int approved = 0, pending = 0, blocked = 0, suspended = 0, archived = 0, rejected = 0;
        int admins = 0, members = 0;

        for (User u : users) {
            if (u.getRole() == Role.ADMIN) admins++; else members++;
            switch (u.getState()) {
                case APPROVED -> approved++;
                case PENDING -> pending++;
                case BLOCKED -> blocked++;
                case SUSPENDED -> suspended++;
                case ARCHIVED -> archived++;
                case REJECTED -> rejected++;
            }
        }

        report.addSummaryStat("Total Registered Users", total);
        report.addSummaryStat("Total Administrators", admins);
        report.addSummaryStat("Total Members", members);
        report.addSummaryStat("Approved / Active Users", approved);
        report.addSummaryStat("Pending Approval", pending);
        report.addSummaryStat("Blocked Users", blocked);
        report.addSummaryStat("Suspended Users", suspended);
        report.addSummaryStat("Archived Users", archived);
        report.addSummaryStat("Rejected Users", rejected);

        report.setHeaders(List.of("User ID", "Username", "Full Name", "Email", "Role", "State", "Active Loans", "Registered On"));

        for (User u : users) {
            int activeLoans = borrowRepository.countActiveLoansByUser(u.getId());
            report.addRow(List.of(
                    u.getId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole().name(),
                    u.getState().name(),
                    String.valueOf(activeLoans),
                    u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "N/A"
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // 3. BORROWING REPORT
    // -------------------------------------------------------------
    public ReportData generateBorrowingReport() {
        ReportData report = new ReportData("Borrowing & Circulation Activity Report");

        List<BorrowRecord> allLoans = borrowRepository.findAll();
        int totalLoans = allLoans.size();
        int activeLoans = 0, returnedLoans = 0, overdueLoans = 0;

        LocalDate today = LocalDate.now();
        Map<String, Integer> bookBorrowCounts = new HashMap<>();
        Map<String, Integer> userBorrowCounts = new HashMap<>();

        for (BorrowRecord r : allLoans) {
            if (r.getStatus() == BorrowStatus.ACTIVE) {
                activeLoans++;
                if (r.isOverdue(today)) {
                    overdueLoans++;
                }
            } else if (r.getStatus() == BorrowStatus.RETURNED) {
                returnedLoans++;
            }
            bookBorrowCounts.merge(r.getBookId(), 1, Integer::sum);
            userBorrowCounts.merge(r.getUserId(), 1, Integer::sum);
        }

        // Count unavailable books (books with 0 available copies)
        int unavailableBooks = 0;
        for (Book b : bookRepository.findAll()) {
            if (copyRepository.findAvailableByBookId(b.getId()).isEmpty()) {
                unavailableBooks++;
            }
        }

        report.addSummaryStat("Total Historical Loans", totalLoans);
        report.addSummaryStat("Active Loans", activeLoans);
        report.addSummaryStat("Returned Loans", returnedLoans);
        report.addSummaryStat("Currently Overdue Loans", overdueLoans);
        report.addSummaryStat("Books Currently Unavailable", unavailableBooks);

        // Find most borrowed book
        String topBookId = bookBorrowCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (topBookId != null) {
            Book topBook = bookRepository.findById(topBookId).orElse(null);
            report.addSummaryStat("Most Borrowed Book", (topBook != null ? topBook.getTitle() : topBookId) + " (" + bookBorrowCounts.get(topBookId) + " times)");
        }

        // Find most active borrower
        String topUserId = userBorrowCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        if (topUserId != null) {
            User topUser = userRepository.findById(topUserId).orElse(null);
            report.addSummaryStat("Top Borrower", (topUser != null ? topUser.getUsername() : topUserId) + " (" + userBorrowCounts.get(topUserId) + " loans)");
        }

        report.setHeaders(List.of("Loan ID", "User", "Book Title", "Copy ID", "Borrowed Date", "Due Date", "Returned Date", "Renewals", "Status"));

        for (BorrowRecord r : allLoans) {
            User u = userRepository.findById(r.getUserId()).orElse(null);
            Book b = bookRepository.findById(r.getBookId()).orElse(null);
            report.addRow(List.of(
                    r.getBorrowId(),
                    u != null ? u.getUsername() : r.getUserId(),
                    b != null ? b.getTitle() : r.getBookId(),
                    r.getCopyId(),
                    r.getBorrowDate().toString(),
                    r.getDueDate().toString(),
                    r.getReturnDate() != null ? r.getReturnDate().toString() : "-",
                    String.valueOf(r.getRenewalCount()),
                    r.isOverdue(today) ? "OVERDUE" : r.getStatus().name()
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // 4. CATALOGUE REPORT
    // -------------------------------------------------------------
    public ReportData generateCatalogueReport() {
        ReportData report = new ReportData("Library Catalogue Classification Report");

        List<Book> books = bookRepository.findAll();
        List<CatalogueSection> sections = sectionRepository.findAll();
        List<Author> authors = authorRepository.findAll();

        report.addSummaryStat("Total Titles", books.size());
        report.addSummaryStat("Total Sections", sections.size());
        report.addSummaryStat("Total Authors", authors.size());

        // Books by Section breakdown
        Map<String, Integer> booksBySection = new LinkedHashMap<>();
        for (Book b : books) {
            String secId = b.getSectionId();
            String secName = "Unassigned";
            if (secId != null && !secId.isBlank()) {
                CatalogueSection s = sectionRepository.findById(secId).orElse(null);
                if (s != null) secName = s.getName();
            }
            booksBySection.merge(secName, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : booksBySection.entrySet()) {
            report.addSummaryStat("Section: " + e.getKey(), e.getValue() + " book(s)");
        }

        // Books by Publication Year breakdown
        Map<String, Integer> booksByYear = new TreeMap<>(Comparator.reverseOrder());
        for (Book b : books) {
            String yr = b.getPublicationDate() != null ? String.valueOf(b.getPublicationDate().getYear()) : "Unknown Year";
            booksByYear.merge(yr, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> e : booksByYear.entrySet()) {
            report.addSummaryStat("Year: " + e.getKey(), e.getValue() + " book(s)");
        }

        report.setHeaders(List.of("Book ID", "Title", "Author(s)", "Section", "Publisher", "Pub Date", "Edition", "Language"));

        for (Book b : books) {
            List<String> authorNames = new ArrayList<>();
            for (String aid : b.getAuthorIds()) {
                authorRepository.findById(aid).ifPresent(a -> authorNames.add(a.getName()));
            }
            CatalogueSection sec = sectionRepository.findById(b.getSectionId()).orElse(null);
            report.addRow(List.of(
                    b.getId(),
                    b.getTitle(),
                    authorNames.isEmpty() ? "Unknown" : String.join(", ", authorNames),
                    sec != null ? sec.getName() : "Unassigned",
                    b.getPublisher(),
                    b.getPublicationDate() != null ? b.getPublicationDate().toString() : "N/A",
                    b.getEdition(),
                    b.getLanguage()
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // 5. HOLD REPORT
    // -------------------------------------------------------------
    public ReportData generateHoldReport() {
        ReportData report = new ReportData("Hold & Reservation Queue Report");

        List<HoldRecord> allHolds = holdRepository.findAll();
        int total = allHolds.size();
        int active = 0, fulfilled = 0, cancelled = 0, expired = 0;
        Map<String, Integer> holdQueueLengths = new LinkedHashMap<>();

        for (HoldRecord h : allHolds) {
            switch (h.getStatus()) {
                case ACTIVE -> {
                    active++;
                    holdQueueLengths.merge(h.getBookId(), 1, Integer::sum);
                }
                case FULFILLED -> fulfilled++;
                case CANCELLED -> cancelled++;
                case EXPIRED -> expired++;
            }
        }

        report.addSummaryStat("Total Hold Requests", total);
        report.addSummaryStat("Active Queue Holds", active);
        report.addSummaryStat("Fulfilled Holds", fulfilled);
        report.addSummaryStat("Cancelled Holds", cancelled);
        report.addSummaryStat("Expired Holds", expired);

        // Add Hold Queue Lengths per Book
        for (Map.Entry<String, Integer> entry : holdQueueLengths.entrySet()) {
            Book b = bookRepository.findById(entry.getKey()).orElse(null);
            String title = b != null ? b.getTitle() : entry.getKey();
            report.addSummaryStat("Queue Length: " + title, entry.getValue() + " waiting");
        }

        report.setHeaders(List.of("Hold ID", "User", "Book Title", "Queue Priority", "Reserved Copy", "Requested At", "Pickup Expiry", "Status"));

        for (HoldRecord h : allHolds) {
            User u = userRepository.findById(h.getUserId()).orElse(null);
            Book b = bookRepository.findById(h.getBookId()).orElse(null);
            report.addRow(List.of(
                    h.getHoldId(),
                    u != null ? u.getUsername() : h.getUserId(),
                    b != null ? b.getTitle() : h.getBookId(),
                    "#" + h.getPriority(),
                    h.getReservedCopyId() != null ? h.getReservedCopyId() : "Pending Copy",
                    h.getRequestDate().toLocalDate().toString(),
                    h.getExpiryDate() != null ? h.getExpiryDate().toLocalDate().toString() : "-",
                    h.getStatus().name()
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // 6. PERSONAL USER REPORT
    // -------------------------------------------------------------
    public ReportData generatePersonalUserReport(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        ReportData report = new ReportData("Personal Account Report - " + user.getUsername());

        List<BorrowRecord> userLoans = borrowRepository.findAllByUserId(userId);
        List<HoldRecord> userHolds = holdRepository.findAllByUserId(userId);

        LocalDate today = LocalDate.now();
        int activeLoans = 0;
        int overdueLoans = 0;
        for (BorrowRecord r : userLoans) {
            if (r.getStatus() == BorrowStatus.ACTIVE) {
                activeLoans++;
                if (r.isOverdue(today)) overdueLoans++;
            }
        }

        report.addSummaryStat("Account ID", user.getId());
        report.addSummaryStat("Username", user.getUsername());
        report.addSummaryStat("Full Name", user.getFullName());
        report.addSummaryStat("Email", user.getEmail());
        report.addSummaryStat("Account Status", user.getState().getDescription());
        report.addSummaryStat("Active Loans", activeLoans);
        report.addSummaryStat("Overdue Books", overdueLoans);
        report.addSummaryStat("Total Historical Loans", userLoans.size());
        report.addSummaryStat("Active Holds", holdRepository.countActiveHoldsByUser(userId));

        report.setHeaders(List.of("Loan ID", "Book Title", "Copy ID", "Borrowed On", "Due Date", "Returned On", "Renewals", "Status"));

        for (BorrowRecord r : userLoans) {
            Book b = bookRepository.findById(r.getBookId()).orElse(null);
            report.addRow(List.of(
                    r.getBorrowId(),
                    b != null ? b.getTitle() : r.getBookId(),
                    r.getCopyId(),
                    r.getBorrowDate().toString(),
                    r.getDueDate().toString(),
                    r.getReturnDate() != null ? r.getReturnDate().toString() : "-",
                    String.valueOf(r.getRenewalCount()),
                    r.isOverdue(today) ? "OVERDUE" : r.getStatus().name()
            ));
        }

        return report;
    }

    // -------------------------------------------------------------
    // EXPORT METHODS
    // -------------------------------------------------------------
    public Path exportReport(ReportData report, String format, String requestedBy) {
        Path exported = reportExporter.export(report, format);
        auditLogService.log("REPORT_EXPORTED", requestedBy, "REPORT", 
                "Exported " + report.getTitle() + " as " + format.toUpperCase() + " to " + exported.getFileName(), true);
        return exported;
    }

    public Path exportAllReportsBundle(String requestedBy) {
        List<ReportData> list = List.of(
                generateInventoryReport(),
                generateUserReport(),
                generateBorrowingReport(),
                generateCatalogueReport(),
                generateHoldReport()
        );
        Path zipPath = reportExporter.exportZipBundle(list, "all_library_reports");
        auditLogService.log("REPORT_BUNDLE_EXPORTED", requestedBy, "REPORT", 
                "Exported complete reports bundle to " + zipPath.getFileName(), true);
        return zipPath;
    }
}
