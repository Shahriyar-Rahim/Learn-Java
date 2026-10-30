package library.service;

import library.exception.EntityNotFoundException;
import library.exception.InvalidStateTransitionException;
import library.exception.ValidationException;
import library.model.Role;
import library.model.User;
import library.model.UserState;
import library.repository.BorrowRepository;
import library.repository.HoldRepository;
import library.repository.UserRepository;
import library.validation.InputValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles user management operations, state transitions, and search.
 */
public class UserService {

    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final HoldRepository holdRepository;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, BorrowRepository borrowRepository, 
                       HoldRepository holdRepository, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.borrowRepository = borrowRepository;
        this.holdRepository = holdRepository;
        this.auditLogService = auditLogService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepository.findAllByState(UserState.PENDING);
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username '" + username + "' not found."));
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) {
            return getAllUsers();
        }
        String q = query.trim().toLowerCase();
        List<User> list = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (u.getUsername().toLowerCase().contains(q) ||
                u.getFullName().toLowerCase().contains(q) ||
                u.getEmail().toLowerCase().contains(q) ||
                u.getId().toLowerCase().contains(q)) {
                list.add(u);
            }
        }
        return list;
    }

    public synchronized void approveUser(String userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getState() != UserState.PENDING && user.getState() != UserState.REJECTED) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.APPROVED.name());
        }
        user.setState(UserState.APPROVED);
        userRepository.save(user);
        auditLogService.log("USER_APPROVED", adminUsername, "USER", "Approved user " + user.getUsername() + " (" + userId + ")", true);
    }

    public synchronized void rejectUser(String userId, String adminUsername, String reason) {
        User user = getUserById(userId);
        if (user.getState() != UserState.PENDING) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.REJECTED.name());
        }
        user.setState(UserState.REJECTED);
        userRepository.save(user);
        auditLogService.log("USER_REJECTED", adminUsername, "USER", "Rejected user " + user.getUsername() + ". Reason: " + reason, true);
    }

    public synchronized void blockUser(String userId, String adminUsername, String reason) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Administrator accounts cannot be blocked.");
        }
        if (user.getState() == UserState.ARCHIVED || user.getState() == UserState.REJECTED) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.BLOCKED.name());
        }
        user.setState(UserState.BLOCKED);
        userRepository.save(user);
        auditLogService.log("USER_BLOCKED", adminUsername, "USER", "Blocked user " + user.getUsername() + ". Reason: " + reason, true);
    }

    public synchronized void unblockUser(String userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getState() != UserState.BLOCKED) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.APPROVED.name());
        }
        user.setState(UserState.APPROVED);
        userRepository.save(user);
        auditLogService.log("USER_UNBLOCKED", adminUsername, "USER", "Unblocked user " + user.getUsername(), true);
    }

    public synchronized void suspendUser(String userId, String adminUsername, String reason) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Administrator accounts cannot be suspended.");
        }
        if (user.getState() != UserState.APPROVED) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.SUSPENDED.name());
        }
        user.setState(UserState.SUSPENDED);
        userRepository.save(user);
        auditLogService.log("USER_SUSPENDED", adminUsername, "USER", "Suspended user " + user.getUsername() + ". Reason: " + reason, true);
    }

    public synchronized void restoreUser(String userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getState() != UserState.SUSPENDED && user.getState() != UserState.ARCHIVED) {
            throw new InvalidStateTransitionException("User " + userId, user.getState().name(), UserState.APPROVED.name());
        }
        user.setState(UserState.APPROVED);
        userRepository.save(user);
        auditLogService.log("USER_RESTORED", adminUsername, "USER", "Restored user " + user.getUsername() + " to APPROVED", true);
    }

    public synchronized void archiveUser(String userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Administrator accounts cannot be archived.");
        }
        // Safety check: Cannot archive if user has active loans
        int activeLoans = borrowRepository.countActiveLoansByUser(userId);
        if (activeLoans > 0) {
            throw new ValidationException("Cannot archive user " + user.getUsername() + ": User currently has " + activeLoans + " active book loan(s). All books must be returned first.");
        }
        // Safety check: Cannot archive if active holds exist
        int activeHolds = holdRepository.countActiveHoldsByUser(userId);
        if (activeHolds > 0) {
            throw new ValidationException("Cannot archive user " + user.getUsername() + ": User has " + activeHolds + " active hold(s). Cancel all holds before archiving.");
        }

        user.setState(UserState.ARCHIVED);
        userRepository.save(user);
        auditLogService.log("USER_ARCHIVED", adminUsername, "USER", "Archived user " + user.getUsername(), true);
    }

    public synchronized void deleteUser(String userId, String adminUsername) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("Administrator accounts cannot be deleted.");
        }
        // Never destroy data that is required for active loans or historical reports
        int activeLoans = borrowRepository.countActiveLoansByUser(userId);
        if (activeLoans > 0) {
            throw new ValidationException("Cannot delete user: User currently has active borrowed books.");
        }
        if (!borrowRepository.findAllByUserId(userId).isEmpty()) {
            throw new ValidationException("Cannot delete user: User has historical borrowing records required for audit and reporting. Use 'Archive User' instead.");
        }
        if (!holdRepository.findAllByUserId(userId).isEmpty()) {
            throw new ValidationException("Cannot delete user: User has hold history. Use 'Archive User' instead.");
        }

        userRepository.delete(userId);
        auditLogService.log("USER_DELETED", adminUsername, "USER", "Permanently deleted user " + user.getUsername() + " (" + userId + ")", true);
    }

    public synchronized void updateUserProfile(String userId, String fullName, String email, String phone) {
        User user = getUserById(userId);
        InputValidator.validateNonEmpty(fullName, "Full Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);

        // Check if email changed and is taken by another user
        if (!user.getEmail().equalsIgnoreCase(email.trim()) && userRepository.existsByEmail(email.trim())) {
            throw new ValidationException("Email '" + email + "' is already in use by another user.");
        }

        user.setFullName(fullName.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        userRepository.save(user);
        auditLogService.log("PROFILE_UPDATED", user.getUsername(), "USER", "Updated profile details", true);
    }

    public synchronized User createUser(String username, char[] password, String fullName, 
                                       String email, String phone, Role role, 
                                       UserState initialState, String adminUsername) {
        InputValidator.validateUsername(username);
        InputValidator.validatePassword(password);
        InputValidator.validateNonEmpty(fullName, "Full Name");
        InputValidator.validateEmail(email);
        InputValidator.validatePhone(phone);

        if (userRepository.existsByUsername(username)) {
            throw new library.exception.DuplicateEntityException("Username '" + username + "' is already registered.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new library.exception.DuplicateEntityException("Email '" + email + "' is already registered.");
        }

        Role actualRole = role != null ? role : Role.MEMBER;
        UserState state = initialState != null ? initialState : UserState.APPROVED;
        String passwordHash = library.security.PasswordHasher.hash(password);
        String userId = userRepository.nextId(actualRole);
        User user = new User(
                userId,
                username.trim(),
                passwordHash,
                fullName.trim(),
                email.trim(),
                phone != null ? phone.trim() : "",
                actualRole,
                state,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        userRepository.save(user);
        auditLogService.log("USER_CREATED_BY_ADMIN", adminUsername, "USER", 
                "Admin created user " + user.getUsername() + " (" + userId + ") with role " + actualRole + " and state " + state, true);
        return user;
    }
}
