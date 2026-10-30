package library.test;

import library.exception.BorrowingException;
import library.exception.UnauthorizedException;
import library.model.*;

import java.time.LocalDate;
import java.util.List;

import static library.test.TestAssert.*;

public class BorrowAndReturnTests {

    public static void run() {
        testBorrowAndInventoryTracking();
        testBorrowRestrictions();
        testReturnAndDoubleReturnPrevention();
        testRenewalAndLimits();
    }

    private static void testBorrowAndInventoryTracking() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User member = env.authService.register("sam", "MemberPass123".toCharArray(), "MemberPass123".toCharArray(), "Sam Reader", "sam@test.com", null);
            env.userService.approveUser(member.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "Computer Science", "admin");
            Book book = env.bookService.createBook("978-0131103627", "The C Programming Language", "K&R", List.of(), sec.getId(), "PH", LocalDate.of(1988, 4, 1), "English", "2nd", 2, "Shelf C-1", BookCondition.GOOD, "admin");

            // Initial state: 2 copies available
            BookDetail before = env.bookService.getBookDetail(book.getId());
            assertEquals(2, before.getAvailableCopies(), "Initially 2 copies available");
            assertEquals(0, before.getBorrowedCopies(), "Initially 0 copies borrowed");

            // Borrow 1 copy
            BorrowRecord loan1 = env.borrowService.borrowBook(member.getId(), book.getId());
            assertNotNull(loan1.getBorrowId(), "Loan ID must be generated");
            assertEquals(BorrowStatus.ACTIVE, loan1.getStatus(), "Loan status must be ACTIVE");

            // Verify inventory changed
            BookDetail after = env.bookService.getBookDetail(book.getId());
            assertEquals(1, after.getAvailableCopies(), "Available copies should decrease to 1");
            assertEquals(1, after.getBorrowedCopies(), "Borrowed copies should increase to 1");

            // Attempting duplicate active borrow of same book by same member is prohibited
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.borrowBook(member.getId(), book.getId());
            }, "Cannot borrow same book twice concurrently");
        } finally {
            env.cleanup();
        }
    }

    private static void testBorrowRestrictions() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User pendingUser = env.authService.register("pending_usr", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "Pending", "p@test.com", null);

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-0262033848", "Introduction to Algorithms", "CLRS", List.of(), sec.getId(), "MIT", LocalDate.of(2009, 7, 31), "English", "3rd", 1, "Shelf C-2", BookCondition.GOOD, "admin");

            // 1. Pending user cannot borrow
            assertThrows(UnauthorizedException.class, () -> {
                env.borrowService.borrowBook(pendingUser.getId(), book.getId());
            }, "Pending user cannot borrow books");

            // Approve user
            env.userService.approveUser(pendingUser.getId(), "admin");

            // 2. Blocked user cannot borrow
            env.userService.blockUser(pendingUser.getId(), "admin", "Temporary block");
            assertThrows(UnauthorizedException.class, () -> {
                env.borrowService.borrowBook(pendingUser.getId(), book.getId());
            }, "Blocked user cannot borrow books");

            // Unblock and suspend user
            env.userService.unblockUser(pendingUser.getId(), "admin");
            env.userService.suspendUser(pendingUser.getId(), "admin", "Disciplinary suspension");

            // 3. Suspended user cannot borrow
            assertThrows(UnauthorizedException.class, () -> {
                env.borrowService.borrowBook(pendingUser.getId(), book.getId());
            }, "Suspended user cannot borrow books");

            // Restore user
            env.userService.restoreUser(pendingUser.getId(), "admin");

            // 4. Exceeding max active loans limit
            // Set max active loans to 2 for testing
            LibraryConfiguration cfg = env.configRepository.get();
            cfg.setMaxActiveLoans(2);
            env.configRepository.save(cfg);

            Book book2 = env.bookService.createBook("978-0132350884", "Clean Code", "Robert C. Martin", List.of(), sec.getId(), "PH", LocalDate.of(2008, 8, 1), "English", "1st", 1, "Shelf C-3", BookCondition.GOOD, "admin");
            Book book3 = env.bookService.createBook("978-0134494166", "Clean Architecture", "Robert C. Martin", List.of(), sec.getId(), "PH", LocalDate.of(2017, 9, 20), "English", "1st", 1, "Shelf C-4", BookCondition.GOOD, "admin");

            env.borrowService.borrowBook(pendingUser.getId(), book.getId());
            env.borrowService.borrowBook(pendingUser.getId(), book2.getId());

            // 3rd borrow should fail because limit is 2
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.borrowBook(pendingUser.getId(), book3.getId());
            }, "Borrowing beyond max active loans limit must fail");
        } finally {
            env.cleanup();
        }
    }

    private static void testReturnAndDoubleReturnPrevention() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User member = env.authService.register("lucy", "LucyPass123".toCharArray(), "LucyPass123".toCharArray(), "Lucy Reader", "lucy@test.com", null);
            env.userService.approveUser(member.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-1449331818", "Learning Python", "O'Reilly", List.of(), sec.getId(), "O'Reilly", LocalDate.of(2013, 6, 12), "English", "5th", 1, "Shelf D-1", BookCondition.GOOD, "admin");

            BorrowRecord record = env.borrowService.borrowBook(member.getId(), book.getId());
            assertEquals(0, env.bookService.getBookDetail(book.getId()).getAvailableCopies(), "Available copies should be 0");

            // Return the book
            env.borrowService.returnBook(record.getBorrowId(), member.getId(), false, BookCondition.GOOD, "Returned in perfect condition");
            BorrowRecord updatedRecord = env.borrowService.getBorrowById(record.getBorrowId());

            assertEquals(BorrowStatus.RETURNED, updatedRecord.getStatus(), "Loan status should be RETURNED");
            assertNotNull(updatedRecord.getReturnDate(), "Return date must be set");
            assertEquals(1, env.bookService.getBookDetail(book.getId()).getAvailableCopies(), "Available copies should be restored to 1");

            // Double return attempt must fail
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.returnBook(record.getBorrowId(), member.getId(), false, BookCondition.GOOD, null);
            }, "Returning an already returned loan must throw BorrowingException");
        } finally {
            env.cleanup();
        }
    }

    private static void testRenewalAndLimits() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User member = env.authService.register("dave", "DavePass123".toCharArray(), "DavePass123".toCharArray(), "Dave Reader", "dave@test.com", null);
            env.userService.approveUser(member.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-0596007126", "Head First Design Patterns", "HFDP", List.of(), sec.getId(), "O'Reilly", LocalDate.of(2004, 10, 1), "English", "1st", 1, "Shelf D-2", BookCondition.GOOD, "admin");

            BorrowRecord loan = env.borrowService.borrowBook(member.getId(), book.getId());
            LocalDate originalDue = loan.getDueDate();

            // Renewal 1
            env.borrowService.renewLoan(loan.getBorrowId(), member.getId(), false);
            BorrowRecord r1 = env.borrowService.getBorrowById(loan.getBorrowId());
            assertEquals(1, r1.getRenewalCount(), "Renewal count should be 1");
            assertTrue(r1.getDueDate().isAfter(originalDue), "Due date must be extended");

            // Renewal 2 (max renewals is 2 by default)
            env.borrowService.renewLoan(loan.getBorrowId(), member.getId(), false);
            BorrowRecord r2 = env.borrowService.getBorrowById(loan.getBorrowId());
            assertEquals(2, r2.getRenewalCount(), "Renewal count should be 2");

            // Renewal 3 should fail
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.renewLoan(loan.getBorrowId(), member.getId(), false);
            }, "Exceeding max renewals limit must throw BorrowingException");
        } finally {
            env.cleanup();
        }
    }
}
