package library.test;

import library.exception.BorrowingException;
import library.exception.HoldException;
import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.*;
import library.persistence.DataStore;
import library.reporting.ReportData;
import library.repository.*;
import library.security.PasswordHasher;
import library.service.BorrowService;
import library.service.HoldService;
import library.validation.InputValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static library.test.TestAssert.*;

/**
 * Executes the complete 30-step realistic automated end-to-end verification workflow.
 */
public class EndToEndSimulation {

    public static void run() {
        System.out.println("\n>>> Starting 30-Step End-To-End Verification Workflow...");
        TestEnvironment env = TestEnvironment.create();
        Path dataDir = env.dataDir;

        try {
            // Step 1: Start application
            System.out.println("  [Step 1] Initializing system & verifying empty state...");
            assertTrue(env.authService.isFirstRun(), "Step 1: First run must be true");

            // Step 2: Create first admin
            System.out.println("  [Step 2] Creating first administrator account...");
            User admin = env.authService.createFirstAdmin(
                    "sysadmin",
                    "AdminMaster2026!".toCharArray(),
                    "AdminMaster2026!".toCharArray(),
                    "System Administrator",
                    "admin@central-library.org",
                    "+1-555-0199",
                    "default"
            );
            assertNotNull(admin, "Step 2: Admin created");
            assertEquals(Role.ADMIN, admin.getRole(), "Step 2: Role must be ADMIN");

            // Step 3: Register user
            System.out.println("  [Step 3] Registering normal member (status = PENDING)...");
            User user = env.authService.register(
                    "mary_curie",
                    "Radium1898!".toCharArray(),
                    "Radium1898!".toCharArray(),
                    "Marie Curie",
                    "curie@sorbonne.fr",
                    "+33-1-4046"
            );
            assertEquals(UserState.PENDING, user.getState(), "Step 3: User initially PENDING");

            // Step 4: Admin logs in
            System.out.println("  [Step 4] Admin authenticates securely...");
            User loggedAdmin = env.authService.login("sysadmin", "AdminMaster2026!".toCharArray());
            assertEquals(Role.ADMIN, loggedAdmin.getRole(), "Step 4: Authenticated as ADMIN");

            // Step 5: Approve user
            System.out.println("  [Step 5] Admin approves registered member...");
            env.userService.approveUser(user.getId(), "sysadmin");
            assertEquals(UserState.APPROVED, env.userService.getUserById(user.getId()).getState(), "Step 5: User APPROVED");

            // Step 6: Create catalogue section
            System.out.println("  [Step 6] Creating catalogue section 'Physics & Chemistry'...");
            CatalogueSection section = env.catalogueService.createSection(
                    "Physics & Chemistry",
                    "Fundamental sciences and research papers",
                    "sysadmin"
            );
            assertNotNull(section.getId(), "Step 6: Section created");

            // Step 7: Create author
            System.out.println("  [Step 7] Creating author 'Albert Einstein'...");
            Author author = env.authorService.createAuthor(
                    "Albert Einstein",
                    "Theoretical physicist, Nobel laureate",
                    1879,
                    "sysadmin"
            );
            assertNotNull(author.getId(), "Step 7: Author created");

            // Step 8: Add book
            System.out.println("  [Step 8] Adding book 'Relativity: The Special and the General Theory'...");
            Book book = env.bookService.createBook(
                    "978-0415253840",
                    "Relativity: The Special and General Theory",
                    "Popular exposition of Einstein's relativity theories",
                    List.of(author.getId()),
                    section.getId(),
                    "Routledge",
                    LocalDate.of(1920, 1, 1),
                    "English",
                    "1st",
                    0, // 0 initial copies first
                    "Archive Room",
                    BookCondition.GOOD,
                    "sysadmin"
            );
            assertNotNull(book.getId(), "Step 8: Book created");

            // Step 9: Add multiple copies
            System.out.println("  [Step 9] Adding 2 physical copies to inventory...");
            List<BookCopy> copies = env.bookService.addCopies(book.getId(), 2, "Main Stacks - Shelf P1", BookCondition.NEW, "sysadmin");
            assertEquals(2, copies.size(), "Step 9: Added 2 copies");
            BookDetail detail9 = env.bookService.getBookDetail(book.getId());
            assertEquals(2, detail9.getAvailableCopies(), "Step 9: Available copies = 2");

            // Step 10: User logs in
            System.out.println("  [Step 10] Member logs in...");
            User loggedMember = env.authService.login("mary_curie", "Radium1898!".toCharArray());
            assertEquals("mary_curie", loggedMember.getUsername(), "Step 10: Member logged in");

            // Step 11: Search book
            System.out.println("  [Step 11] Member searches book by keyword 'Relativity'...");
            List<BookDetail> searchRes = env.bookService.searchBooks("Relativity", null, null, null, null, BookSort.TITLE_ASC);
            assertEquals(1, searchRes.size(), "Step 11: Found 1 matching book");

            // Step 12: Borrow book
            System.out.println("  [Step 12] Member borrows a copy...");
            BorrowRecord loan = env.borrowService.borrowBook(loggedMember.getId(), book.getId());
            assertNotNull(loan.getBorrowId(), "Step 12: Loan generated");
            assertEquals(BorrowStatus.ACTIVE, loan.getStatus(), "Step 12: Loan status ACTIVE");

            // Step 13: Verify inventory changed
            System.out.println("  [Step 13] Verifying inventory decremented...");
            BookDetail detail13 = env.bookService.getBookDetail(book.getId());
            assertEquals(1, detail13.getAvailableCopies(), "Step 13: 1 copy available now");
            assertEquals(1, detail13.getBorrowedCopies(), "Step 13: 1 copy borrowed now");

            // Step 14: View personal loan
            System.out.println("  [Step 14] Viewing member's personal active loans...");
            List<BorrowRecord> userLoans = env.borrowService.getActiveLoansForUser(loggedMember.getId());
            assertEquals(1, userLoans.size(), "Step 14: Member has 1 active loan");

            // Step 15: Attempt invalid duplicate borrow
            System.out.println("  [Step 15] Verifying duplicate active borrow of same title is blocked...");
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.borrowBook(loggedMember.getId(), book.getId());
            }, "Step 15: Duplicate active borrow must fail");

            // Step 16: Return book
            System.out.println("  [Step 16] Member returns the borrowed book...");
            env.borrowService.returnBook(loan.getBorrowId(), loggedMember.getId(), false, BookCondition.GOOD, "Returned safely");
            assertEquals(BorrowStatus.RETURNED, env.borrowService.getBorrowById(loan.getBorrowId()).getStatus(), "Step 16: Loan RETURNED");

            // Step 17: Verify inventory restored
            System.out.println("  [Step 17] Verifying inventory restored to 2 available...");
            BookDetail detail17 = env.bookService.getBookDetail(book.getId());
            assertEquals(2, detail17.getAvailableCopies(), "Step 17: Available copies restored to 2");
            assertEquals(0, detail17.getBorrowedCopies(), "Step 17: Borrowed copies restored to 0");

            // Step 18 & 19: Create hold scenario & place multiple holds
            System.out.println("  [Step 18 & 19] Creating hold scenario (checking out all copies then placing holds)...");
            User user2 = env.authService.register("niels_bohr", "Quantum1913!".toCharArray(), "Quantum1913!".toCharArray(), "Niels Bohr", "bohr@copenhagen.dk", null);
            User user3 = env.authService.register("max_planck", "Constant1900!".toCharArray(), "Constant1900!".toCharArray(), "Max Planck", "planck@berlin.de", null);
            User user4 = env.authService.register("erwin_schrod", "WaveEq1926!".toCharArray(), "WaveEq1926!".toCharArray(), "Erwin Schrodinger", "erwin@vienna.at", null);
            env.userService.approveUser(user2.getId(), "sysadmin");
            env.userService.approveUser(user3.getId(), "sysadmin");
            env.userService.approveUser(user4.getId(), "sysadmin");

            // Check out both copies so book is out of stock
            BorrowRecord l1 = env.borrowService.borrowBook(user.getId(), book.getId());
            BorrowRecord l2 = env.borrowService.borrowBook(user2.getId(), book.getId());
            assertEquals(0, env.bookService.getBookDetail(book.getId()).getAvailableCopies(), "0 copies left");

            // Place multiple holds
            HoldRecord h1 = env.holdService.placeHold(user3.getId(), book.getId());
            HoldRecord h2 = env.holdService.placeHold(user4.getId(), book.getId());

            // Step 20: Verify FIFO behavior
            System.out.println("  [Step 20] Verifying FIFO queue priorities (#1 Max Planck, #2 Erwin Schrodinger)...");
            assertEquals(1, h1.getPriority(), "Step 20: First hold priority = 1");
            assertEquals(2, h2.getPriority(), "Step 20: Second hold priority = 2");

            // Step 21: Generate reports
            System.out.println("  [Step 21] Generating system analytical reports...");
            ReportData invRep = env.reportService.generateInventoryReport();
            ReportData usrRep = env.reportService.generateUserReport();
            ReportData brwRep = env.reportService.generateBorrowingReport();
            assertNotNull(invRep, "Step 21: Inventory report generated");
            assertNotNull(usrRep, "Step 21: User report generated");
            assertNotNull(brwRep, "Step 21: Borrowing report generated");

            // Step 22: Export TXT report
            System.out.println("  [Step 22] Exporting TXT report to reports/ directory...");
            Path txtPath = env.reportService.exportReport(invRep, "txt", "sysadmin");
            assertTrue(Files.exists(txtPath), "Step 22: TXT report file created");

            // Step 23: Export CSV report
            System.out.println("  [Step 23] Exporting CSV report to reports/ directory...");
            Path csvPath = env.reportService.exportReport(usrRep, "csv", "sysadmin");
            assertTrue(Files.exists(csvPath), "Step 23: CSV report file created");

            // Step 24 & 25: Restart application & verify data survived
            System.out.println("  [Step 24 & 25] Simulating application restart and verifying data integrity...");
            DataStore restartedStore = new DataStore(dataDir);
            UserRepository rUserRepo = new UserRepository(restartedStore);
            BookRepository rBookRepo = new BookRepository(restartedStore);
            BorrowRepository rBorrowRepo = new BorrowRepository(restartedStore);
            HoldRepository rHoldRepo = new HoldRepository(restartedStore);

            assertEquals(5, rUserRepo.count(), "Step 25: All 5 users survived reload");
            assertEquals(1, rBookRepo.count(), "Step 25: Book survived reload");
            assertEquals(3, rBorrowRepo.findAll().size(), "Step 25: 3 loan transactions survived reload");
            assertEquals(2, rHoldRepo.findAll().size(), "Step 25: 2 holds survived reload");

            // Step 26: Test invalid input
            System.out.println("  [Step 26] Testing invalid inputs and resilience...");
            assertThrows(ValidationException.class, () -> {
                InputValidator.validateUsername("ab"); // too short
            }, "Step 26: Short username rejected");
            assertThrows(ValidationException.class, () -> {
                InputValidator.validateEmail("invalid-email");
            }, "Step 26: Invalid email rejected");

            // Step 27: Test blocked user
            System.out.println("  [Step 27] Testing blocked user restrictions...");
            env.userService.blockUser(user.getId(), "sysadmin", "Temporary block");
            assertThrows(UnauthorizedException.class, () -> {
                env.authService.login("mary_curie", "Radium1898!".toCharArray());
            }, "Step 27: Blocked user cannot login");
            env.userService.unblockUser(user.getId(), "sysadmin");

            // Step 28: Test archived book
            System.out.println("  [Step 28] Testing archived book restrictions...");
            Book testArchBook = env.bookService.createBook("978-0000000001", "Archived Title", "Desc", List.of(), section.getId(), "Pub", LocalDate.now(), "English", "1st", 1, "Shelf", BookCondition.GOOD, "sysadmin");
            env.bookService.archiveBook(testArchBook.getId(), "sysadmin");
            assertTrue(env.bookService.getBookById(testArchBook.getId()).isArchived(), "Step 28: Book is archived");
            assertThrows(ValidationException.class, () -> {
                env.borrowService.borrowBook(user.getId(), testArchBook.getId());
            }, "Step 28: Archived book cannot be borrowed");

            // Step 29: Test renewal
            System.out.println("  [Step 29] Testing loan renewal...");
            // First verify renewal is blocked while holds exist on the book
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.renewLoan(l2.getBorrowId(), user2.getId(), false);
            }, "Step 29: Renewal blocked while other members have holds");

            // Cancel waiting holds
            env.holdService.cancelHold(h1.getHoldId(), user3.getId(), false);
            env.holdService.cancelHold(h2.getHoldId(), user4.getId(), false);

            // Now renewal succeeds
            LocalDate preRenewalDue = l2.getDueDate();
            env.borrowService.renewLoan(l2.getBorrowId(), user2.getId(), false);
            BorrowRecord renewed = env.borrowService.getBorrowById(l2.getBorrowId());
            assertEquals(1, renewed.getRenewalCount(), "Step 29: Renewal count is 1");
            assertTrue(renewed.getDueDate().isAfter(preRenewalDue), "Step 29: Due date extended");

            // Step 30: Verify logs
            System.out.println("  [Step 30] Verifying audit logs...");
            List<AuditLogEntry> logs = env.auditLogService.getAllLogs();
            assertTrue(logs.size() >= 15, "Step 30: Audit log entries recorded for actions");
            System.out.println("  [Step 30] Recorded " + logs.size() + " audit events successfully.");

            System.out.println("\n>>> 30-Step End-To-End Verification Workflow Completed Successfully! All assertions passed.");
        } finally {
            env.cleanup();
        }
    }
}
