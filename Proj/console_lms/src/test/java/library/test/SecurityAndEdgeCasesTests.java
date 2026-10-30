package library.test;

import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.AuditLogEntry;
import library.model.Role;
import library.model.User;
import library.model.UserState;
import library.security.SecurityUtil;
import library.security.UserSession;
import library.validation.InputValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static library.test.TestAssert.*;

public class SecurityAndEdgeCasesTests {

    public static void run() {
        testPathTraversalDefense();
        testPasswordSecurityNoPlaintext();
        testRoleAuthorizationAndSelfAccess();
        testEdgeCaseInputHandling();
    }

    private static void testPathTraversalDefense() {
        TestEnvironment env = TestEnvironment.create();
        Path base = env.reportsDir;

        // Path traversal with ../
        assertThrows(ValidationException.class, () -> {
            SecurityUtil.sanitizeFilename("../../../etc/passwd");
        }, "Path traversal with ../ must be blocked");

        // Path traversal with backslashes
        assertThrows(ValidationException.class, () -> {
            SecurityUtil.sanitizeFilename("..\\..\\windows\\system32");
        }, "Path traversal with backslashes must be blocked");

        // Forward slashes
        assertThrows(ValidationException.class, () -> {
            SecurityUtil.sanitizeFilename("sub/folder/file.txt");
        }, "Subdirectory separators must be blocked in simple filenames");

        // Null bytes
        assertThrows(ValidationException.class, () -> {
            SecurityUtil.sanitizeFilename("report\0.txt");
        }, "Null bytes in filenames must be blocked");

        // Valid safe filename
        String safe = SecurityUtil.sanitizeFilename("monthly_report_2026-10-03.csv");
        assertEquals("monthly_report_2026-10-03.csv", safe, "Valid filename should be accepted");

        Path resolved = SecurityUtil.resolveSafePath(base, safe);
        assertTrue(resolved.startsWith(base), "Resolved safe path must stay inside base directory");
    }

    private static void testPasswordSecurityNoPlaintext() {
        TestEnvironment env = TestEnvironment.create();
        try {
            String secretPassword = "SuperSecretPassword123!";
            env.authService.createFirstAdmin("secadmin", secretPassword.toCharArray(), secretPassword.toCharArray(), "Sec Admin", "sec@sec.com", null, "default");
            env.userRepository.flush();

            // Read raw users.json from disk
            Path usersFile = env.dataDir.resolve("users.json");
            String rawJson = Files.readString(usersFile);

            // Plaintext password must NEVER exist in storage
            assertFalse(rawJson.contains(secretPassword), "users.json must NEVER contain plaintext passwords!");
            assertTrue(rawJson.contains("PBKDF2$65536$"), "users.json must contain PBKDF2 hash");

            // Audit logs must NEVER log passwords or password hashes
            List<AuditLogEntry> logs = env.auditLogService.getAllLogs();
            for (AuditLogEntry l : logs) {
                assertFalse(l.getDetails().contains(secretPassword), "Audit log must not contain plaintext password");
                assertFalse(l.getDetails().contains("PBKDF2$"), "Audit log must not contain password hash");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            env.cleanup();
        }
    }

    private static void testRoleAuthorizationAndSelfAccess() {
        UserSession session = new UserSession();

        // Not logged in checks
        assertThrows(UnauthorizedException.class, session::requireLoggedIn, "Not logged in should throw UnauthorizedException");
        assertThrows(UnauthorizedException.class, session::requireAdmin, "Not logged in requireAdmin should throw UnauthorizedException");

        User member = new User("USR-1001", "member1", "hash", "Member", "m@test.com", "", Role.MEMBER, UserState.APPROVED, null, null);
        session.login(member);

        // Member calling requireAdmin must fail
        assertThrows(UnauthorizedException.class, session::requireAdmin, "Normal member calling requireAdmin must throw UnauthorizedException");

        // Member accessing another user's personal ID must fail
        assertThrows(UnauthorizedException.class, () -> {
            session.requireSelfOrAdmin("USR-9999");
        }, "Member accessing other user ID must throw UnauthorizedException");

        // Member accessing own ID succeeds
        session.requireSelfOrAdmin("USR-1001");

        // Admin can access any user's ID
        User admin = new User("ADM-1001", "admin1", "hash", "Admin", "a@test.com", "", Role.ADMIN, UserState.APPROVED, null, null);
        session.login(admin);
        session.requireAdmin();
        session.requireSelfOrAdmin("USR-9999"); // admin can access anyone
    }

    private static void testEdgeCaseInputHandling() {
        // Empty inputs
        assertThrows(ValidationException.class, () -> InputValidator.validateNonEmpty("", "Field"), "Empty string must fail validation");
        assertThrows(ValidationException.class, () -> InputValidator.validateNonEmpty("   ", "Field"), "Whitespace string must fail validation");
        assertThrows(ValidationException.class, () -> InputValidator.validateNonEmpty(null, "Field"), "Null string must fail validation");

        // Giant string
        String giant = "a".repeat(1000);
        assertThrows(ValidationException.class, () -> InputValidator.validateNonEmpty(giant, "Field"), "Excessively long input must fail validation");

        // Invalid numbers
        assertThrows(ValidationException.class, () -> InputValidator.validatePositive(0, "Count"), "Zero must not be positive");
        assertThrows(ValidationException.class, () -> InputValidator.validatePositive(-5, "Count"), "Negative must not be positive");

        // Future dates
        assertThrows(ValidationException.class, () -> {
            InputValidator.validatePublicationDate(LocalDate.now().plusYears(10));
        }, "Future publication date must fail validation");

        // Ancient dates before year 1000
        assertThrows(ValidationException.class, () -> {
            InputValidator.validatePublicationDate(LocalDate.of(500, 1, 1));
        }, "Date before year 1000 must fail validation");
    }
}
