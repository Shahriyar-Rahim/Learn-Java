package library.service;

import library.exception.DuplicateEntityException;
import library.exception.EntityNotFoundException;
import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.Role;
import library.model.User;
import library.model.UserState;
import library.repository.UserRepository;
import library.security.PasswordHasher;
import library.security.SecurityUtil;
import library.validation.InputValidator;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Handles user authentication, registration, password hashing, and first-run admin provisioning.
 */
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AuthenticationService(UserRepository userRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public boolean isFirstRun() {
        return !userRepository.hasAnyAdmin();
    }

    public synchronized User createFirstAdmin(String username, char[] password, char[] confirmPassword, 
                                             String fullName, String email, String phone, String adminSecret) {
        if (!isFirstRun()) {
            throw new UnauthorizedException("An administrator account already exists. Standard registration required.");
        }
        if (password == null || confirmPassword == null || !Arrays.equals(password, confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
        if (!SecurityUtil.verifyAdminSetupSecret(adminSecret)) {
            throw new UnauthorizedException("Invalid administrator setup secret key.");
        }

        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);
        InputValidator.validateNonEmpty(fullName, "Full Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateEntityException("Username '" + username + "' is already taken.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEntityException("Email '" + email + "' is already registered.");
        }

        String passwordHash = PasswordHasher.hash(password);
        String adminId = userRepository.nextId(Role.ADMIN);
        User admin = new User(
                adminId,
                username.trim(),
                passwordHash,
                fullName.trim(),
                email.trim(),
                phone != null ? phone.trim() : "",
                Role.ADMIN,
                UserState.APPROVED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userRepository.save(admin);
        auditLogService.log("FIRST_ADMIN_SETUP", admin.getUsername(), "USER", "Created initial administrator ID " + adminId, true);
        return admin;
    }

    public synchronized User register(String username, char[] password, char[] confirmPassword, 
                                     String fullName, String email, String phone) {
        if (password == null || confirmPassword == null || !Arrays.equals(password, confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }

        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);
        InputValidator.validateNonEmpty(fullName, "Full Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateEntityException("Username '" + username + "' is already registered.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEntityException("Email '" + email + "' is already registered.");
        }

        String passwordHash = PasswordHasher.hash(password);
        String userId = userRepository.nextId(Role.MEMBER);
        User user = new User(
                userId,
                username.trim(),
                passwordHash,
                fullName.trim(),
                email.trim(),
                phone != null ? phone.trim() : "",
                Role.MEMBER,
                UserState.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        userRepository.save(user);
        auditLogService.log("USER_REGISTER", user.getUsername(), "USER", "Registered new member with PENDING status", true);
        return user;
    }

    public User login(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) {
            throw new ValidationException("Username and password must not be empty.");
        }

        User user = userRepository.findByUsername(username.trim()).orElse(null);
        if (user == null) {
            auditLogService.log("LOGIN_FAILED", username.trim(), "AUTH", "Unknown username attempt", false);
            throw new ValidationException("Invalid username or password.");
        }

        // Check account state before password check to provide specific feedback
        if (user.getState() == UserState.PENDING) {
            auditLogService.log("LOGIN_DENIED", user.getUsername(), "AUTH", "Pending account tried to login", false);
            throw new UnauthorizedException("Your account is pending administrator approval. Please wait for an administrator to activate your account.");
        }
        if (user.getState() == UserState.BLOCKED) {
            auditLogService.log("LOGIN_DENIED", user.getUsername(), "AUTH", "Blocked account tried to login", false);
            throw new UnauthorizedException("Your account has been blocked. Please contact the library administration.");
        }
        if (user.getState() == UserState.ARCHIVED) {
            auditLogService.log("LOGIN_DENIED", user.getUsername(), "AUTH", "Archived account tried to login", false);
            throw new UnauthorizedException("This account is archived and inactive.");
        }
        if (user.getState() == UserState.REJECTED) {
            auditLogService.log("LOGIN_DENIED", user.getUsername(), "AUTH", "Rejected account tried to login", false);
            throw new UnauthorizedException("Your account registration was rejected by the library administration.");
        }

        boolean valid = PasswordHasher.verify(password, user.getPasswordHash());
        if (!valid) {
            auditLogService.log("LOGIN_FAILED", user.getUsername(), "AUTH", "Incorrect password", false);
            throw new ValidationException("Invalid username or password.");
        }

        auditLogService.log("LOGIN_SUCCESS", user.getUsername(), "AUTH", "Logged in with role " + user.getRole(), true);
        return user;
    }

    public synchronized void changePassword(String userId, char[] oldPass, char[] newPass, char[] confirmPass) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));

        if (!PasswordHasher.verify(oldPass, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        if (newPass == null || confirmPass == null || !Arrays.equals(newPass, confirmPass)) {
            throw new ValidationException("New passwords do not match.");
        }
        InputValidator.validatePassword(newPass);

        user.setPasswordHash(PasswordHasher.hash(newPass));
        userRepository.save(user);
        auditLogService.log("PASSWORD_CHANGED", user.getUsername(), "USER", "Password successfully changed", true);
    }
}
