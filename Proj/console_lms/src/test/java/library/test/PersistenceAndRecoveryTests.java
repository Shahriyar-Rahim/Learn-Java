package library.test;

import library.model.*;
import library.persistence.DataStore;
import library.repository.*;
import library.security.PasswordHasher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static library.test.TestAssert.*;

public class PersistenceAndRecoveryTests {

    public static void run() {
        testPersistenceAcrossApplicationRestarts();
        testCorruptFileBackupRecovery();
    }

    private static void testPersistenceAcrossApplicationRestarts() {
        TestEnvironment env = TestEnvironment.create();
        Path dataDir = env.dataDir;

        try {
            // 1. Populate data
            User admin = env.authService.createFirstAdmin("superadmin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Super Admin", "admin@domain.com", "+1-555-1111", "default");
            User member = env.authService.register("jules", "JulesPass123".toCharArray(), "JulesPass123".toCharArray(), "Jules Verne", "jules@domain.com", null);
            env.userService.approveUser(member.getId(), "superadmin");

            Author author = env.authorService.createAuthor("Arthur Conan Doyle", "Creator of Sherlock Holmes", 1859, "superadmin");
            CatalogueSection section = env.catalogueService.createSection("Mystery", "Detective and mystery fiction", "superadmin");

            Book book = env.bookService.createBook(
                    "978-0141034355",
                    "A Study in Scarlet",
                    "First appearance of Sherlock Holmes",
                    List.of(author.getId()),
                    section.getId(),
                    "Ward Lock & Co",
                    LocalDate.of(1887, 11, 1),
                    "English",
                    "1st",
                    3,
                    "Shelf M-10",
                    BookCondition.GOOD,
                    "superadmin"
            );

            BorrowRecord loan = env.borrowService.borrowBook(member.getId(), book.getId());

            // 2. Simulate Application Termination / Restart
            // Create completely new DataStore and Repositories on the same directory
            DataStore restartedStore = new DataStore(dataDir);
            UserRepository reloadedUsers = new UserRepository(restartedStore);
            AuthorRepository reloadedAuthors = new AuthorRepository(restartedStore);
            SectionRepository reloadedSections = new SectionRepository(restartedStore);
            BookRepository reloadedBooks = new BookRepository(restartedStore);
            BookCopyRepository reloadedCopies = new BookCopyRepository(restartedStore);
            BorrowRepository reloadedLoans = new BorrowRepository(restartedStore);

            // 3. Verify Reloaded Data
            assertEquals(2, reloadedUsers.count(), "Both admin and member must exist after reload");
            User loadedMember = reloadedUsers.findByUsername("jules").orElse(null);
            assertNotNull(loadedMember, "User 'jules' must be found");
            assertEquals(UserState.APPROVED, loadedMember.getState(), "State must remain APPROVED");
            assertTrue(PasswordHasher.verify("JulesPass123".toCharArray(), loadedMember.getPasswordHash()), "Password hash must verify properly");

            assertEquals(1, reloadedAuthors.count(), "Author must survive reload");
            assertEquals("Arthur Conan Doyle", reloadedAuthors.findById(author.getId()).get().getName(), "Author name must match");

            assertEquals(1, reloadedSections.count(), "Section must survive reload");
            assertEquals("Mystery", reloadedSections.findById(section.getId()).get().getName(), "Section name must match");

            assertEquals(1, reloadedBooks.count(), "Book must survive reload");
            Book loadedBook = reloadedBooks.findById(book.getId()).get();
            assertEquals("A Study in Scarlet", loadedBook.getTitle(), "Book title must match");
            assertEquals("978-0141034355", loadedBook.getIsbn(), "ISBN must match");

            assertEquals(3, reloadedCopies.findByBookId(book.getId()).size(), "All 3 copies must survive reload");

            assertEquals(1, reloadedLoans.findAll().size(), "Loan must survive reload");
            BorrowRecord loadedLoan = reloadedLoans.findById(loan.getBorrowId()).get();
            assertEquals(member.getId(), loadedLoan.getUserId(), "Loan borrower ID must match");
            assertEquals(book.getId(), loadedLoan.getBookId(), "Loan book ID must match");
            assertEquals(BorrowStatus.ACTIVE, loadedLoan.getStatus(), "Loan status must still be ACTIVE");
        } finally {
            env.cleanup();
        }
    }

    private static void testCorruptFileBackupRecovery() {
        TestEnvironment env = TestEnvironment.create();
        Path dataDir = env.dataDir;

        try {
            // Save initial user
            User admin = env.authService.createFirstAdmin("recoveryadmin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "rec@admin.com", null, "default");
            env.userRepository.flush();

            // Save second user to create a backup file (.bak)
            User u2 = env.authService.register("user_backup", "UserPass123".toCharArray(), "UserPass123".toCharArray(), "Backup User", "b@admin.com", null);
            env.userRepository.flush();

            Path usersJson = dataDir.resolve("users.json");
            Path usersBak = dataDir.resolve("users.json.bak");
            assertTrue(Files.exists(usersBak), "Backup file users.json.bak should exist");

            // Intentionally corrupt users.json with invalid syntax
            Files.writeString(usersJson, "CORRUPT JSON CONTENT{{{ [[[ invalid", StandardCharsets.UTF_8);

            // Re-instantiate DataStore
            DataStore store = new DataStore(dataDir);
            List<User> recovered = store.loadUsers();

            assertNotNull(recovered, "DataStore must gracefully recover from backup instead of returning null or crashing");
            assertFalse(recovered.isEmpty(), "Recovered list should contain users from backup");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            env.cleanup();
        }
    }
}
