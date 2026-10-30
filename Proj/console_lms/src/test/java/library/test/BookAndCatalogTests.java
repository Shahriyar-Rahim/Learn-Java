package library.test;

import library.exception.DuplicateEntityException;
import library.exception.ValidationException;
import library.model.*;

import java.time.LocalDate;
import java.util.List;

import static library.test.TestAssert.*;

public class BookAndCatalogTests {

    public static void run() {
        testAuthorManagement();
        testSectionManagement();
        testBookAndCopiesManagement();
        testSearchAndFilter();
    }

    private static void testAuthorManagement() {
        TestEnvironment env = TestEnvironment.create();
        try {
            Author a1 = env.authorService.createAuthor("Robert C. Martin", "Uncle Bob, Clean Code author", 1952, "admin");
            assertNotNull(a1.getId(), "Author ID must be generated");
            assertEquals("Robert C. Martin", a1.getName(), "Author name must match");

            // Duplicate author name
            assertThrows(DuplicateEntityException.class, () -> {
                env.authorService.createAuthor("Robert C. Martin", "Duplicate", 1952, "admin");
            }, "Duplicate author name should be rejected");

            // Update author
            env.authorService.updateAuthor(a1.getId(), "Robert Cecil Martin", "Updated bio", 1952, "admin");
            Author updated = env.authorService.getAuthorById(a1.getId());
            assertEquals("Robert Cecil Martin", updated.getName(), "Name should be updated");

            // Archive and restore
            env.authorService.archiveAuthor(a1.getId(), "admin");
            assertTrue(env.authorService.getAuthorById(a1.getId()).isArchived(), "Author should be archived");
            env.authorService.restoreAuthor(a1.getId(), "admin");
            assertFalse(env.authorService.getAuthorById(a1.getId()).isArchived(), "Author should be active");
        } finally {
            env.cleanup();
        }
    }

    private static void testSectionManagement() {
        TestEnvironment env = TestEnvironment.create();
        try {
            CatalogueSection s = env.catalogueService.createSection("Computer Science", "CS books", "admin");
            assertNotNull(s.getId(), "Section ID must be generated");

            // Duplicate section name
            assertThrows(DuplicateEntityException.class, () -> {
                env.catalogueService.createSection("Computer Science", "Another CS", "admin");
            }, "Duplicate section name should throw DuplicateEntityException");

            // Update section
            env.catalogueService.updateSection(s.getId(), "Computing Sciences", "Updated", "admin");
            assertEquals("Computing Sciences", env.catalogueService.getSectionById(s.getId()).getName(), "Section name should be updated");
        } finally {
            env.cleanup();
        }
    }

    private static void testBookAndCopiesManagement() {
        TestEnvironment env = TestEnvironment.create();
        try {
            Author author = env.authorService.createAuthor("Joshua Bloch", "Java architect", 1961, "admin");
            CatalogueSection section = env.catalogueService.createSection("Programming", "Programming texts", "admin");

            // Create book with 3 copies
            Book book = env.bookService.createBook(
                    "978-0134685991",
                    "Effective Java",
                    "Best practices for Java",
                    List.of(author.getId()),
                    section.getId(),
                    "Addison-Wesley",
                    LocalDate.of(2018, 1, 15),
                    "English",
                    "3rd",
                    3,
                    "Shelf A-1",
                    BookCondition.NEW,
                    "admin"
            );

            assertNotNull(book.getId(), "Book ID must be generated");
            BookDetail detail = env.bookService.getBookDetail(book.getId());
            assertEquals(3, detail.getTotalCopies(), "Total copies should be 3");
            assertEquals(3, detail.getAvailableCopies(), "Available copies should be 3");

            // Duplicate ISBN rejected
            assertThrows(DuplicateEntityException.class, () -> {
                env.bookService.createBook(
                        "978-0134685991",
                        "Duplicate Effective Java",
                        "Duplicate",
                        List.of(author.getId()),
                        section.getId(),
                        "Addison-Wesley",
                        LocalDate.of(2018, 1, 15),
                        "English",
                        "3rd",
                        1,
                        "Shelf A-1",
                        BookCondition.NEW,
                        "admin"
                );
            }, "Duplicate ISBN must be rejected");

            // Add more copies
            env.bookService.addCopies(book.getId(), 2, "Shelf A-2", BookCondition.GOOD, "admin");
            BookDetail detail2 = env.bookService.getBookDetail(book.getId());
            assertEquals(5, detail2.getTotalCopies(), "Total copies should now be 5");

            // Author and section cannot be deleted while referenced by active book
            assertThrows(ValidationException.class, () -> {
                env.authorService.deleteAuthor(author.getId(), "admin");
            }, "Cannot delete author referenced by books");

            assertThrows(ValidationException.class, () -> {
                env.catalogueService.deleteSection(section.getId(), "admin");
            }, "Cannot delete section referenced by books");

            // Archive and restore book
            env.bookService.archiveBook(book.getId(), "admin");
            assertTrue(env.bookService.getBookById(book.getId()).isArchived(), "Book should be archived");
            env.bookService.restoreBook(book.getId(), "admin");
            assertFalse(env.bookService.getBookById(book.getId()).isArchived(), "Book should be restored");
        } finally {
            env.cleanup();
        }
    }

    private static void testSearchAndFilter() {
        TestEnvironment env = TestEnvironment.create();
        try {
            Author a1 = env.authorService.createAuthor("Martin Fowler", "Architecture", 1963, "admin");
            Author a2 = env.authorService.createAuthor("Kent Beck", "TDD", 1961, "admin");

            CatalogueSection s1 = env.catalogueService.createSection("Software Architecture", "Architecture", "admin");
            CatalogueSection s2 = env.catalogueService.createSection("Agile Methods", "Agile", "admin");

            env.bookService.createBook("978-0201485677", "Refactoring", "Code improvements", List.of(a1.getId(), a2.getId()), s1.getId(), "AW", LocalDate.of(1999, 7, 8), "English", "1st", 2, "Shelf B-1", BookCondition.GOOD, "admin");
            env.bookService.createBook("978-0321127426", "Patterns of Enterprise Application Architecture", "PEAA patterns", List.of(a1.getId()), s1.getId(), "AW", LocalDate.of(2002, 11, 5), "English", "1st", 1, "Shelf B-2", BookCondition.GOOD, "admin");
            env.bookService.createBook("978-0321146533", "Test Driven Development", "By Example", List.of(a2.getId()), s2.getId(), "AW", LocalDate.of(2002, 11, 8), "English", "1st", 0, "Shelf B-3", BookCondition.GOOD, "admin");

            // Search by keyword
            List<BookDetail> res1 = env.bookService.searchBooks("Refactoring", null, null, null, null, BookSort.TITLE_ASC);
            assertEquals(1, res1.size(), "Should find 1 book with 'Refactoring'");

            // Search by author
            List<BookDetail> res2 = env.bookService.searchBooks(null, null, a1.getId(), null, null, BookSort.TITLE_ASC);
            assertEquals(2, res2.size(), "Martin Fowler should have 2 books");

            // Filter available only
            List<BookDetail> res3 = env.bookService.searchBooks(null, null, null, null, true, BookSort.TITLE_ASC);
            assertEquals(2, res3.size(), "Only 2 books have available copies (TDD has 0 copies)");

            // Sort Z-A
            List<BookDetail> res4 = env.bookService.searchBooks(null, null, null, null, null, BookSort.TITLE_DESC);
            assertEquals("Test Driven Development", res4.get(0).getBook().getTitle(), "First in Z-A should be TDD");
        } finally {
            env.cleanup();
        }
    }
}
