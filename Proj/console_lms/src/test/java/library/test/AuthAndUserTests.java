package library.test;

import library.exception.DuplicateEntityException;
import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.Role;
import library.model.User;
import library.model.UserState;
import library.security.PasswordHasher;

import static library.test.TestAssert.*;

public class AuthAndUserTests {

    public static void run() {
        testPasswordHasher();
        testFirstRunAdminCreation();
        testUserRegistrationAndValidation();
        testAuthenticationAndStates();
        testUserAdminTransitions();
        testUserDeletionSafety();
    }

    private static void testPasswordHasher() {
        char[] pass = "SecretPass123".toCharArray();
        String hash = PasswordHasher.hash(pass);
        assertNotNull(hash, "Password hash must not be null");
        assertTrue(hash.startsWith("PBKDF2$65536$"), "Hash format must start with PBKDF2 algorithm specifier");

        assertTrue(PasswordHasher.verify("SecretPass123".toCharArray(), hash), "Password verification with correct password must succeed");
        assertFalse(PasswordHasher.verify("WrongPass123".toCharArray(), hash), "Password verification with wrong password must fail");
        assertFalse(PasswordHasher.verify("".toCharArray(), hash), "Empty password check must fail");
        assertFalse(PasswordHasher.verify(null, hash), "Null password check must fail");
    }

    private static void testFirstRunAdminCreation() {
        TestEnvironment env = TestEnvironment.create();
        try {
            assertTrue(env.authService.isFirstRun(), "System must report first run when no admins exist");

            // Successful admin setup
            User admin = env.authService.createFirstAdmin(
                    "rootadmin",
                    "AdminPass999".toCharArray(),
                    "AdminPass999".toCharArray(),
                    "System Administrator",
                    "admin@library.org",
                    "+1-555-0100",
                    "default"
            );

            assertNotNull(admin, "Created admin must not be null");
            assertEquals(Role.ADMIN, admin.getRole(), "Role must be ADMIN");
            assertEquals(UserState.APPROVED, admin.getState(), "Admin state must be APPROVED");
            assertFalse(env.authService.isFirstRun(), "First run flag must be false after admin creation");

            // Attempting to create first admin a second time must fail
            assertThrows(UnauthorizedException.class, () -> {
                env.authService.createFirstAdmin(
                        "secondadmin",
                        "AdminPass999".toCharArray(),
                        "AdminPass999".toCharArray(),
                        "Second Admin",
                        "admin2@library.org",
                        null,
                        "default"
                );
            }, "Cannot create first admin twice");
        } finally {
            env.cleanup();
        }
    }

    private static void testUserRegistrationAndValidation() {
        TestEnvironment env = TestEnvironment.create();
        try {
            // Setup admin first
            env.authService.createFirstAdmin("admin", "Admin123!".toCharArray(), "Admin123!".toCharArray(), "Admin", "admin@test.com", null, "default");

            // Valid registration
            User u1 = env.authService.register(
                    "johndoe",
                    "SecurePass1".toCharArray(),
                    "SecurePass1".toCharArray(),
                    "John Doe",
                    "john@test.com",
                    "123-456-7890"
            );
            assertNotNull(u1.getId(), "User ID must be generated");
            assertEquals(UserState.PENDING, u1.getState(), "New user state must be PENDING");

            // Duplicate username
            assertThrows(DuplicateEntityException.class, () -> {
                env.authService.register("johndoe", "SecurePass1".toCharArray(), "SecurePass1".toCharArray(), "John Clone", "john2@test.com", null);
            }, "Duplicate username should throw DuplicateEntityException");

            // Duplicate email
            assertThrows(DuplicateEntityException.class, () -> {
                env.authService.register("johndoe2", "SecurePass1".toCharArray(), "SecurePass1".toCharArray(), "John Clone", "john@test.com", null);
            }, "Duplicate email should throw DuplicateEntityException");

            // Password mismatch
            assertThrows(ValidationException.class, () -> {
                env.authService.register("alice", "Password123".toCharArray(), "DifferentPass1".toCharArray(), "Alice", "alice@test.com", null);
            }, "Mismatched passwords must throw ValidationException");

            // Weak password (no digits)
            assertThrows(ValidationException.class, () -> {
                env.authService.register("alice", "PasswordOnly".toCharArray(), "PasswordOnly".toCharArray(), "Alice", "alice@test.com", null);
            }, "Password without digits must throw ValidationException");

            // Weak password (too short)
            assertThrows(ValidationException.class, () -> {
                env.authService.register("alice", "Pass1".toCharArray(), "Pass1".toCharArray(), "Alice", "alice@test.com", null);
            }, "Short password must throw ValidationException");
        } finally {
            env.cleanup();
        }
    }

    private static void testAuthenticationAndStates() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User member = env.authService.register("bob", "BobPass123".toCharArray(), "BobPass123".toCharArray(), "Bob Member", "bob@test.com", null);

            // Pending member cannot login
            assertThrows(UnauthorizedException.class, () -> {
                env.authService.login("bob", "BobPass123".toCharArray());
            }, "Pending account should not be allowed to log in");

            // Admin approves member
            env.userService.approveUser(member.getId(), "admin");

            // Now approved member logs in successfully
            User loggedIn = env.authService.login("bob", "BobPass123".toCharArray());
            assertEquals("bob", loggedIn.getUsername(), "Logged in username must match");

            // Wrong password fails
            assertThrows(ValidationException.class, () -> {
                env.authService.login("bob", "WrongPass123".toCharArray());
            }, "Wrong password must throw ValidationException");

            // Unknown user fails
            assertThrows(ValidationException.class, () -> {
                env.authService.login("unknown_user", "AnyPass123".toCharArray());
            }, "Unknown username must throw ValidationException");

            // Blocked member cannot login
            env.userService.blockUser(member.getId(), "admin", "Violation of policy");
            assertThrows(UnauthorizedException.class, () -> {
                env.authService.login("bob", "BobPass123".toCharArray());
            }, "Blocked user must not be able to log in");

            // Unblock member
            env.userService.unblockUser(member.getId(), "admin");
            User unblockedUser = env.authService.login("bob", "BobPass123".toCharArray());
            assertNotNull(unblockedUser, "Unblocked user should be able to log in");
        } finally {
            env.cleanup();
        }
    }

    private static void testUserAdminTransitions() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User m = env.authService.register("user_trans", "TestPass123".toCharArray(), "TestPass123".toCharArray(), "Transition User", "trans@test.com", null);

            // Reject user
            env.userService.rejectUser(m.getId(), "admin", "Incomplete credentials");
            assertEquals(UserState.REJECTED, env.userService.getUserById(m.getId()).getState(), "State should be REJECTED");

            // Approve after review
            env.userService.approveUser(m.getId(), "admin");
            assertEquals(UserState.APPROVED, env.userService.getUserById(m.getId()).getState(), "State should be APPROVED");

            // Suspend user
            env.userService.suspendUser(m.getId(), "admin", "Overdue items");
            assertEquals(UserState.SUSPENDED, env.userService.getUserById(m.getId()).getState(), "State should be SUSPENDED");

            // Restore user
            env.userService.restoreUser(m.getId(), "admin");
            assertEquals(UserState.APPROVED, env.userService.getUserById(m.getId()).getState(), "State should be restored to APPROVED");

            // Archive user
            env.userService.archiveUser(m.getId(), "admin");
            assertEquals(UserState.ARCHIVED, env.userService.getUserById(m.getId()).getState(), "State should be ARCHIVED");

            // Restore from archived
            env.userService.restoreUser(m.getId(), "admin");
            assertEquals(UserState.APPROVED, env.userService.getUserById(m.getId()).getState(), "State should be restored from ARCHIVED to APPROVED");
        } finally {
            env.cleanup();
        }
    }

    private static void testUserDeletionSafety() {
        TestEnvironment env = TestEnvironment.create();
        try {
            env.authService.createFirstAdmin("admin", "AdminPass123".toCharArray(), "AdminPass123".toCharArray(), "Admin", "admin@test.com", null, "default");
            User u = env.authService.register("clean_user", "UserPass123".toCharArray(), "UserPass123".toCharArray(), "Clean User", "clean@test.com", null);

            // Clean user with no history can be deleted
            env.userService.deleteUser(u.getId(), "admin");
            assertFalse(env.userRepository.findById(u.getId()).isPresent(), "User should be removed from repository");

            // Admins cannot be deleted or blocked
            User admin = env.userRepository.findByUsername("admin").get();
            assertThrows(ValidationException.class, () -> {
                env.userService.deleteUser(admin.getId(), "admin");
            }, "Admin account cannot be deleted");
            assertThrows(ValidationException.class, () -> {
                env.userService.blockUser(admin.getId(), "admin", "test");
            }, "Admin account cannot be blocked");
        } finally {
            env.cleanup();
        }
    }
}
