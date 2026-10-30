package library.repository;

import library.model.Role;
import library.model.User;
import library.model.UserState;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory repository for Users, backed by DataStore.
 */
public class UserRepository {

    private final DataStore dataStore;
    private final Map<String, User> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idByUsername = new ConcurrentHashMap<>();
    private final Map<String, String> idByEmail = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public UserRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        idByUsername.clear();
        idByEmail.clear();
        List<User> list = dataStore.loadUsers();
        int maxSeq = 1000;
        for (User u : list) {
            byId.put(u.getId(), u);
            if (u.getUsername() != null) {
                idByUsername.put(u.getUsername().toLowerCase(), u.getId());
            }
            if (u.getEmail() != null) {
                idByEmail.put(u.getEmail().toLowerCase(), u.getId());
            }
            int idNum = extractNumber(u.getId());
            if (idNum > maxSeq) {
                maxSeq = idNum;
            }
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveUsers(byId.values());
    }

    public synchronized String nextId(Role role) {
        String prefix = (role == Role.ADMIN) ? "ADM-" : "USR-";
        return prefix + sequence.incrementAndGet();
    }

    public synchronized void save(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID cannot be null.");
        }
        User existing = byId.get(user.getId());
        if (existing != null) {
            if (existing.getUsername() != null) idByUsername.remove(existing.getUsername().toLowerCase());
            if (existing.getEmail() != null) idByEmail.remove(existing.getEmail().toLowerCase());
        }
        byId.put(user.getId(), user);
        if (user.getUsername() != null) {
            idByUsername.put(user.getUsername().toLowerCase(), user.getId());
        }
        if (user.getEmail() != null) {
            idByEmail.put(user.getEmail().toLowerCase(), user.getId());
        }
        flush();
    }

    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String id = idByUsername.get(username.trim().toLowerCase());
        return id != null ? Optional.ofNullable(byId.get(id)) : Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = idByEmail.get(email.trim().toLowerCase());
        return id != null ? Optional.ofNullable(byId.get(id)) : Optional.empty();
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return idByUsername.containsKey(username.trim().toLowerCase());
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return idByEmail.containsKey(email.trim().toLowerCase());
    }

    public List<User> findAll() {
        return new ArrayList<>(byId.values());
    }

    public List<User> findAllByState(UserState state) {
        List<User> result = new ArrayList<>();
        for (User u : byId.values()) {
            if (u.getState() == state) {
                result.add(u);
            }
        }
        return result;
    }

    public boolean hasAnyAdmin() {
        for (User u : byId.values()) {
            if (u.getRole() == Role.ADMIN) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean delete(String id) {
        User removed = byId.remove(id);
        if (removed != null) {
            if (removed.getUsername() != null) idByUsername.remove(removed.getUsername().toLowerCase());
            if (removed.getEmail() != null) idByEmail.remove(removed.getEmail().toLowerCase());
            flush();
            return true;
        }
        return false;
    }

    public int count() {
        return byId.size();
    }

    private int extractNumber(String id) {
        if (id == null) return 0;
        try {
            String digits = id.replaceAll("\\D+", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
