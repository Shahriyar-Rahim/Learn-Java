package library.test;

import library.exception.BorrowingException;
import library.exception.HoldException;
import library.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static library.test.TestAssert.*;

public class HoldQueueTests {

    public static void run() {
        testHoldPlacementAndQueueFIFO();
        testHoldFulfillmentOnReturn();
        testHoldCancellationAndReallocation();
        testSkippingInactiveUsersInHoldQueue();
        testHoldExpiration();
    }

    private static void testHoldPlacementAndQueueFIFO() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");

            User u1 = env.authService.register("user_a", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "User A", "a@test.com", null);
            User u2 = env.authService.register("user_b", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "User B", "b@test.com", null);
            User u3 = env.authService.register("user_c", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "User C", "c@test.com", null);
            env.userService.approveUser(u1.getId(), "admin");
            env.userService.approveUser(u2.getId(), "admin");
            env.userService.approveUser(u3.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("Science", "Science books", "admin");
            Book book = env.bookService.createBook("978-0199535729", "The Origin of Species", "Darwin", List.of(), sec.getId(), "Oxford", LocalDate.of(1859, 11, 24), "English", "1st", 1, "Shelf S-1", BookCondition.GOOD, "admin");

            // Holding when copy is currently available must fail (member should borrow directly)
            assertThrows(HoldException.class, () -> {
                env.holdService.placeHold(u2.getId(), book.getId());
            }, "Cannot place hold when copies are available on the shelf");

            // User A borrows the only copy
            env.borrowService.borrowBook(u1.getId(), book.getId());

            // User B places hold -> Priority #1
            HoldRecord h1 = env.holdService.placeHold(u2.getId(), book.getId());
            assertEquals(1, h1.getPriority(), "User B should be #1 in queue");

            // User C places hold -> Priority #2
            HoldRecord h2 = env.holdService.placeHold(u3.getId(), book.getId());
            assertEquals(2, h2.getPriority(), "User C should be #2 in queue");

            // Duplicate hold by User B on same book rejected
            assertThrows(HoldException.class, () -> {
                env.holdService.placeHold(u2.getId(), book.getId());
            }, "Duplicate hold by same user must be rejected");

            // Verify queue ordering
            List<HoldRecord> activeQueue = env.holdService.getActiveHoldsForBook(book.getId());
            assertEquals(2, activeQueue.size(), "Queue length should be 2");
            assertEquals(u2.getId(), activeQueue.get(0).getUserId(), "First in queue must be User B");
            assertEquals(u3.getId(), activeQueue.get(1).getUserId(), "Second in queue must be User C");
        } finally {
            env.cleanup();
        }
    }

    private static void testHoldFulfillmentOnReturn() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User u1 = env.authService.register("borrower", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "Borrower", "b1@test.com", null);
            User u2 = env.authService.register("waiter", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "Waiter", "w1@test.com", null);
            User u3 = env.authService.register("stranger", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "Stranger", "s1@test.com", null);
            env.userService.approveUser(u1.getId(), "admin");
            env.userService.approveUser(u2.getId(), "admin");
            env.userService.approveUser(u3.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("Science", "Science", "admin");
            Book book = env.bookService.createBook("978-0199535729", "The Origin of Species", "Darwin", List.of(), sec.getId(), "Oxford", LocalDate.of(1859, 11, 24), "English", "1st", 1, "Shelf S-1", BookCondition.GOOD, "admin");

            BorrowRecord loan = env.borrowService.borrowBook(u1.getId(), book.getId());
            HoldRecord hold = env.holdService.placeHold(u2.getId(), book.getId());

            // When borrower returns book, hold fulfillment should trigger
            env.borrowService.returnBook(loan.getBorrowId(), u1.getId(), false, BookCondition.GOOD, "Good");

            // Copy should now be RESERVED for user U2
            BookCopy copy = env.bookService.getCopiesForBook(book.getId()).get(0);
            assertEquals(CopyStatus.RESERVED, copy.getStatus(), "Returned copy should be RESERVED for hold");

            HoldRecord updatedHold = env.holdService.getHoldById(hold.getHoldId());
            assertEquals(copy.getCopyId(), updatedHold.getReservedCopyId(), "Hold should track reserved copy ID");
            assertNotNull(updatedHold.getExpiryDate(), "Pickup deadline should be set");

            // Stranger U3 attempts to borrow the reserved copy: must fail
            assertThrows(BorrowingException.class, () -> {
                env.borrowService.borrowBook(u3.getId(), book.getId());
            }, "Non-holding member cannot take a reserved copy");

            // User U2 (who holds the reservation) borrows it: should succeed and mark hold FULFILLED
            BorrowRecord newLoan = env.borrowService.borrowBook(u2.getId(), book.getId());
            assertNotNull(newLoan.getBorrowId(), "Hold beneficiary must be able to borrow the reserved copy");

            HoldRecord fulfilledHold = env.holdService.getHoldById(hold.getHoldId());
            assertEquals(HoldStatus.FULFILLED, fulfilledHold.getStatus(), "Hold status should be FULFILLED");
        } finally {
            env.cleanup();
        }
    }

    private static void testHoldCancellationAndReallocation() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User u1 = env.authService.register("usr1", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U1", "u1@test.com", null);
            User u2 = env.authService.register("usr2", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U2", "u2@test.com", null);
            User u3 = env.authService.register("usr3", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U3", "u3@test.com", null);
            env.userService.approveUser(u1.getId(), "admin");
            env.userService.approveUser(u2.getId(), "admin");
            env.userService.approveUser(u3.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-0131103627", "C Prog", "K&R", List.of(), sec.getId(), "PH", LocalDate.of(1988, 4, 1), "English", "2nd", 1, "Shelf C-1", BookCondition.GOOD, "admin");

            BorrowRecord loan = env.borrowService.borrowBook(u1.getId(), book.getId());
            HoldRecord h2 = env.holdService.placeHold(u2.getId(), book.getId());
            HoldRecord h3 = env.holdService.placeHold(u3.getId(), book.getId());

            // U1 returns book -> copy reserved for U2
            env.borrowService.returnBook(loan.getBorrowId(), u1.getId(), false, BookCondition.GOOD, null);
            assertEquals(CopyStatus.RESERVED, env.bookService.getCopiesForBook(book.getId()).get(0).getStatus(), "Copy should be reserved for U2");

            // U2 cancels hold -> copy must automatically reallocate to U3 (the next in queue!)
            env.holdService.cancelHold(h2.getHoldId(), u2.getId(), false);

            HoldRecord updatedH3 = env.holdService.getHoldById(h3.getHoldId());
            assertEquals(CopyStatus.RESERVED, env.bookService.getCopiesForBook(book.getId()).get(0).getStatus(), "Copy should reallocate to U3");
            assertNotNull(updatedH3.getReservedCopyId(), "U3 should now have reserved copy");

            // U3 cancels hold -> with no more holds, copy should become AVAILABLE
            env.holdService.cancelHold(h3.getHoldId(), u3.getId(), false);
            assertEquals(CopyStatus.AVAILABLE, env.bookService.getCopiesForBook(book.getId()).get(0).getStatus(), "Copy should revert to AVAILABLE when queue is empty");
        } finally {
            env.cleanup();
        }
    }

    private static void testSkippingInactiveUsersInHoldQueue() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User u1 = env.authService.register("borrower_active", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U1", "u1@test.com", null);
            User u2 = env.authService.register("bad_member", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U2", "u2@test.com", null);
            User u3 = env.authService.register("good_member", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U3", "u3@test.com", null);
            env.userService.approveUser(u1.getId(), "admin");
            env.userService.approveUser(u2.getId(), "admin");
            env.userService.approveUser(u3.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-0131103627", "C Prog", "K&R", List.of(), sec.getId(), "PH", LocalDate.of(1988, 4, 1), "English", "2nd", 1, "Shelf C-1", BookCondition.GOOD, "admin");

            BorrowRecord loan = env.borrowService.borrowBook(u1.getId(), book.getId());
            HoldRecord h2 = env.holdService.placeHold(u2.getId(), book.getId());
            HoldRecord h3 = env.holdService.placeHold(u3.getId(), book.getId());

            // Bad member gets blocked while waiting
            env.userService.blockUser(u2.getId(), "admin", "Rule violation");

            // U1 returns book -> hold system must skip blocked U2 and fulfill for U3
            env.borrowService.returnBook(loan.getBorrowId(), u1.getId(), false, BookCondition.GOOD, null);

            HoldRecord updatedH2 = env.holdService.getHoldById(h2.getHoldId());
            HoldRecord updatedH3 = env.holdService.getHoldById(h3.getHoldId());

            assertEquals(HoldStatus.CANCELLED, updatedH2.getStatus(), "Blocked user's hold should be marked CANCELLED");
            assertEquals(CopyStatus.RESERVED, env.bookService.getCopiesForBook(book.getId()).get(0).getStatus(), "Copy should be reserved for U3");
            assertEquals(env.bookService.getCopiesForBook(book.getId()).get(0).getCopyId(), updatedH3.getReservedCopyId(), "U3 should receive the reservation");
        } finally {
            env.cleanup();
        }
    }

    private static void testHoldExpiration() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User u1 = env.authService.register("usr_borrower", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U1", "u1@test.com", null);
            User u2 = env.authService.register("usr_holder", "Pass12345".toCharArray(), "Pass12345".toCharArray(), "U2", "u2@test.com", null);
            env.userService.approveUser(u1.getId(), "admin");
            env.userService.approveUser(u2.getId(), "admin");

            CatalogueSection sec = env.catalogueService.createSection("CS", "CS", "admin");
            Book book = env.bookService.createBook("978-0131103627", "C Prog", "K&R", List.of(), sec.getId(), "PH", LocalDate.of(1988, 4, 1), "English", "2nd", 1, "Shelf C-1", BookCondition.GOOD, "admin");

            // u1 borrows the copy
            BorrowRecord loan = env.borrowService.borrowBook(u1.getId(), book.getId());

            // u2 places hold
            HoldRecord h = env.holdService.placeHold(u2.getId(), book.getId());

            // u1 returns book -> copy is reserved for u2
            env.borrowService.returnBook(loan.getBorrowId(), u1.getId(), false, BookCondition.GOOD, null);

            HoldRecord reservedHold = env.holdService.getHoldById(h.getHoldId());
            assertNotNull(reservedHold.getReservedCopyId(), "Copy should be reserved for u2");

            // Manually age the expiry date to yesterday to simulate expiry window passing
            reservedHold.setExpiryDate(LocalDateTime.now().minusDays(1));
            env.holdRepository.save(reservedHold);

            assertTrue(reservedHold.isExpired(LocalDateTime.now()), "Hold should be expired");
            env.holdService.processExpiredHolds();

            HoldRecord after = env.holdService.getHoldById(h.getHoldId());
            assertEquals(HoldStatus.EXPIRED, after.getStatus(), "Hold should be marked EXPIRED after processExpiredHolds");

            // Copy should now be released and available on shelf
            BookCopy copy = env.bookService.getCopiesForBook(book.getId()).get(0);
            assertEquals(CopyStatus.AVAILABLE, copy.getStatus(), "Expired reservation must release copy back to AVAILABLE");
        } finally {
            env.cleanup();
        }
    }
}
