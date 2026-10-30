package library.security;

import library.exception.UnauthorizedException;
import library.model.Role;
import library.model.User;

/**
 * Manages the current authenticated user session.
 */
public class UserSession {
    private User currentUser;

    public UserSession() {
    }

    public synchronized void login(User user) {
        this.currentUser = user;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public synchronized void requireLoggedIn() {
        if (!isLoggedIn()) {
            throw new UnauthorizedException("Authentication required. Please log in first.");
        }
    }

    public synchronized void requireAdmin() {
        requireLoggedIn();
        if (!isAdmin()) {
            throw new UnauthorizedException("Administrative privileges required for this operation.");
        }
    }

    public synchronized void requireSelfOrAdmin(String targetUserId) {
        requireLoggedIn();
        if (!isAdmin() && (currentUser.getId() == null || !currentUser.getId().equals(targetUserId))) {
            throw new UnauthorizedException("Access denied: You can only access your own account details.");
        }
    }
}
