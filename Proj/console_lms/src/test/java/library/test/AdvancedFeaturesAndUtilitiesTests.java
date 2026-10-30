package library.test;

import library.model.*;
import library.reporting.ReportData;
import library.service.*;
import library.util.CollectionUtil;
import library.util.DateUtil;
import library.util.StringUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tests for advanced features: Administrative user creation, book section moving,
 * date-range search, inventory summaries, utility helpers, and enhanced reporting.
 */
public class AdvancedFeaturesAndUtilitiesTests {

    public static void run() {
        testUtilityClasses();
        testAdminUserCreation();
        testMoveBookToSection();
        testAdvancedDateRangeSearch();
        testAuthorAndSectionSearch();
        testEnhancedReportingMetrics();
    }

    private static void testUtilityClasses() {
        // 1. DateUtil
        LocalDate d1 = LocalDate.of(2025, 1, 15);
        LocalDate d2 = LocalDate.of(2025, 2, 15);
        LocalDate d3 = LocalDate.of(2025, 3, 15);

        TestAssert.assertEquals("2025-01-15", DateUtil.formatDate(d1), "DateUtil.formatDate");
        TestAssert.assertEquals(d1, DateUtil.parseDate("2025-01-15"), "DateUtil.parseDate valid");
        TestAssert.assertNull(DateUtil.parseDate("invalid-date"), "DateUtil.parseDate invalid");

        TestAssert.assertTrue(DateUtil.isBetween(d2, d1, d3), "DateUtil.isBetween true");
        TestAssert.assertFalse(DateUtil.isBetween(LocalDate.of(2024, 1, 1), d1, d3), "DateUtil.isBetween before");
        TestAssert.assertFalse(DateUtil.isBetween(LocalDate.of(2026, 1, 1), d1, d3), "DateUtil.isBetween after");
        TestAssert.assertEquals(31L, DateUtil.daysBetween(d1, d2), "DateUtil.daysBetween");

        // 2. StringUtil
        TestAssert.assertTrue(StringUtil.isBlank("   "), "StringUtil.isBlank spaces");
        TestAssert.assertFalse(StringUtil.isBlank("hello"), "StringUtil.isBlank word");
        TestAssert.assertEquals("Hel...", StringUtil.truncate("Hello World", 6), "StringUtil.truncate");
        TestAssert.assertEquals("Hi   ", StringUtil.padRight("Hi", 5), "StringUtil.padRight");
        TestAssert.assertEquals("   Hi", StringUtil.padLeft("Hi", 5), "StringUtil.padLeft");
        TestAssert.assertTrue(StringUtil.containsIgnoreCase("Clean Architecture", "clean"), "StringUtil.containsIgnoreCase");
        TestAssert.assertEquals("j**n@example.com", StringUtil.maskEmail("john@example.com"), "StringUtil.maskEmail");

        // 3. CollectionUtil
        List<String> items = List.of("apple", "banana", "apricot", "cherry");
        Map<Character, List<String>> grouped = CollectionUtil.groupBy(items, s -> s.charAt(0));
        TestAssert.assertEquals(2, grouped.get('a').size(), "CollectionUtil.groupBy 'a'");
        TestAssert.assertEquals(1, grouped.get('b').size(), "CollectionUtil.groupBy 'b'");

        Map<Character, Integer> counts = CollectionUtil.countBy(items, s -> s.charAt(0));
        TestAssert.assertEquals(2, counts.get('a'), "CollectionUtil.countBy 'a'");

        List<String> sub = CollectionUtil.safeSubList(items, 1, 3);
        TestAssert.assertEquals(2, sub.size(), "CollectionUtil.safeSubList size");
        TestAssert.assertEquals("banana", sub.get(0), "CollectionUtil.safeSubList 0");
    }

    private static void testAdminUserCreation() {
        TestEnvironment env = TestEnvironment.create();
        UserService userService = env.userService;

        // Admin creates a pre-approved member directly
        User member = userService.createUser("direct_member", "Pass1234!".toCharArray(), 
                "Direct Member", "direct@example.com", "1234567890", 
                Role.MEMBER, UserState.APPROVED, "admin");

        TestAssert.assertNotNull(member, "Created member is not null");
        TestAssert.assertEquals(UserState.APPROVED, member.getState(), "Admin-created user is pre-approved");
        TestAssert.assertEquals(Role.MEMBER, member.getRole(), "User role is MEMBER");

        // Admin creates another administrator directly
        User admin2 = userService.createUser("sub_admin", "Admin9876!".toCharArray(), 
                "Assistant Admin", "subadmin@example.com", "9876543210", 
                Role.ADMIN, UserState.APPROVED, "admin");

        TestAssert.assertEquals(Role.ADMIN, admin2.getRole(), "User role is ADMIN");
        TestAssert.assertTrue(admin2.canLogin(), "Admin can login");

        // Verify duplicate prevention
        TestAssert.assertThrows(library.exception.DuplicateEntityException.class, () -> {
            userService.createUser("direct_member", "NewPass123!".toCharArray(), 
                    "Other Name", "other@example.com", "", Role.MEMBER, UserState.APPROVED, "admin");
        }, "Duplicate username blocked");

        env.cleanup();
    }

    private static void testMoveBookToSection() {
        TestEnvironment env = TestEnvironment.create();
        BookService bookService = env.bookService;
        CatalogueService catService = env.catalogueService;

        CatalogueSection sec1 = catService.createSection("Science", "Science books", "admin");
        CatalogueSection sec2 = catService.createSection("History", "History books", "admin");

        Book book = bookService.createBook("978-0132350884", "A History of Science", "Desc",
                List.of(), sec1.getId(), "Publisher", LocalDate.of(2020, 5, 10),
                "English", "1st", 2, "Shelf A", BookCondition.GOOD, "admin");

        TestAssert.assertEquals(sec1.getId(), book.getSectionId(), "Initial section is sec1");

        // Move to sec2
        bookService.moveBookToSection(book.getId(), sec2.getId(), "admin");
        Book updated = bookService.getBookById(book.getId());
        TestAssert.assertEquals(sec2.getId(), updated.getSectionId(), "Updated section is sec2");

        // Unassign section
        bookService.moveBookToSection(book.getId(), "", "admin");
        Book unassigned = bookService.getBookById(book.getId());
        TestAssert.assertEquals("", unassigned.getSectionId(), "Section unassigned successfully");

        // Invalid section throws EntityNotFoundException
        TestAssert.assertThrows(library.exception.EntityNotFoundException.class, () -> {
            bookService.moveBookToSection(book.getId(), "NONEXISTENT_SEC", "admin");
        }, "Nonexistent section throws exception");

        env.cleanup();
    }

    private static void testAdvancedDateRangeSearch() {
        TestEnvironment env = TestEnvironment.create();
        BookService bookService = env.bookService;

        Book b1 = bookService.createBook("978-1111111111", "Book 2010", "", List.of(), "", "Pub",
                LocalDate.of(2010, 6, 1), "English", "1st", 1, "Shelf", BookCondition.GOOD, "admin");
        Book b2 = bookService.createBook("978-2222222222", "Book 2015", "", List.of(), "", "Pub",
                LocalDate.of(2015, 6, 1), "English", "1st", 1, "Shelf", BookCondition.GOOD, "admin");
        Book b3 = bookService.createBook("978-3333333333", "Book 2020", "", List.of(), "", "Pub",
                LocalDate.of(2020, 6, 1), "English", "1st", 1, "Shelf", BookCondition.GOOD, "admin");

        // Search with date range 2012 to 2018 -> Should only return b2
        List<BookDetail> results = bookService.searchBooks("", null, null, null,
                LocalDate.of(2012, 1, 1), LocalDate.of(2018, 12, 31), false, BookSort.DATE_NEWEST);

        TestAssert.assertEquals(1, results.size(), "Date range search returned 1 book");
        TestAssert.assertEquals(b2.getId(), results.get(0).getBook().getId(), "Returned book is b2");

        // Search with publication year 2020
        List<BookDetail> yrResults = bookService.searchBooks("", null, null, 2020, null, null, false, null);
        TestAssert.assertEquals(1, yrResults.size(), "Year filter returned 1 book");
        TestAssert.assertEquals(b3.getId(), yrResults.get(0).getBook().getId(), "Returned book is b3");

        env.cleanup();
    }

    private static void testAuthorAndSectionSearch() {
        TestEnvironment env = TestEnvironment.create();
        AuthorService authorService = env.authorService;
        CatalogueService catService = env.catalogueService;

        authorService.createAuthor("Isaac Newton", "Physicist and mathematician", 1643, "admin");
        authorService.createAuthor("Isaac Asimov", "Science fiction author", 1920, "admin");
        authorService.createAuthor("Albert Einstein", "Theoretical physicist", 1879, "admin");

        List<Author> isaacs = authorService.searchAuthors("Isaac");
        TestAssert.assertEquals(2, isaacs.size(), "Author search for 'Isaac' returns 2 authors");

        catService.createSection("Quantum Physics", "Quantum mechanics", "admin");
        catService.createSection("Classical Physics", "Classical mechanics", "admin");
        catService.createSection("World History", "Historical records", "admin");

        List<CatalogueSection> physicsSections = catService.searchSections("Physics");
        TestAssert.assertEquals(2, physicsSections.size(), "Section search for 'Physics' returns 2 sections");

        env.cleanup();
    }

    private static void testEnhancedReportingMetrics() {
        TestEnvironment env = TestEnvironment.create();
        BookService bookService = env.bookService;
        UserService userService = env.userService;
        BorrowService borrowService = env.borrowService;
        HoldService holdService = env.holdService;
        ReportService reportService = env.reportService;

        // Setup users and books
        User user1 = userService.createUser("user1", "Pass1234!".toCharArray(), "User One", "u1@test.com", "", Role.MEMBER, UserState.APPROVED, "admin");
        User user2 = userService.createUser("user2", "Pass1234!".toCharArray(), "User Two", "u2@test.com", "", Role.MEMBER, UserState.APPROVED, "admin");

        Book book = bookService.createBook("978-0131103627", "The C Programming Language", "Desc",
                List.of(), "", "Prentice Hall", LocalDate.of(1978, 2, 22), "English", "1st", 1, "Shelf", BookCondition.GOOD, "admin");

        // Check borrowing report with 0 loans initially -> book is available
        ReportData rep1 = reportService.generateBorrowingReport();
        TestAssert.assertEquals("0", rep1.getSummaryStats().get("Books Currently Unavailable"), "0 unavailable books before checkout");

        // Borrow the single copy
        borrowService.borrowBook(user1.getId(), book.getId());

        // Now book is unavailable
        ReportData rep2 = reportService.generateBorrowingReport();
        TestAssert.assertEquals("1", rep2.getSummaryStats().get("Books Currently Unavailable"), "1 unavailable book after checkout");

        // Place hold with user2
        holdService.placeHold(user2.getId(), book.getId());

        // Hold report should include hold queue length
        ReportData holdRep = reportService.generateHoldReport();
        TestAssert.assertTrue(holdRep.getSummaryStats().containsKey("Queue Length: The C Programming Language"), "Hold report has queue length");
        TestAssert.assertEquals("1 waiting", holdRep.getSummaryStats().get("Queue Length: The C Programming Language"), "Queue length is 1 waiting");

        // Inventory summary
        Map<String, Integer> inv = bookService.getInventorySummary();
        TestAssert.assertEquals(1, inv.get("TOTAL_BOOKS"), "Inventory summary total books");
        TestAssert.assertEquals(1, inv.get("TOTAL_COPIES"), "Inventory summary total copies");
        TestAssert.assertEquals(0, inv.get("AVAILABLE"), "Inventory summary available copies");
        TestAssert.assertEquals(1, inv.get("BORROWED"), "Inventory summary borrowed copies");

        env.cleanup();
    }
}
