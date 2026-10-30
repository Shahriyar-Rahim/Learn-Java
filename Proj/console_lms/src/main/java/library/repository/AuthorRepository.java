package library.repository;

import library.model.Author;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for Authors.
 */
public class AuthorRepository {

    private final DataStore dataStore;
    private final Map<String, Author> byId = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public AuthorRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<Author> list = dataStore.loadAuthors();
        int maxSeq = 1000;
        for (Author a : list) {
            byId.put(a.getId(), a);
            int num = extractNumber(a.getId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveAuthors(byId.values());
    }

    public synchronized String nextId() {
        return "AUT-" + sequence.incrementAndGet();
    }

    public synchronized void save(Author author) {
        if (author == null || author.getId() == null) {
            throw new IllegalArgumentException("Author and Author ID cannot be null.");
        }
        byId.put(author.getId(), author);
        flush();
    }

    public Optional<Author> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<Author> findByName(String name) {
        if (name == null) return Optional.empty();
        String trimName = name.trim().toLowerCase();
        for (Author a : byId.values()) {
            if (a.getName() != null && a.getName().trim().equalsIgnoreCase(trimName)) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public List<Author> findAll() {
        return new ArrayList<>(byId.values());
    }

    public List<Author> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String lower = keyword.trim().toLowerCase();
        List<Author> matches = new ArrayList<>();
        for (Author a : byId.values()) {
            if (a.getName() != null && a.getName().toLowerCase().contains(lower)) {
                matches.add(a);
            }
        }
        return matches;
    }

    public synchronized boolean delete(String id) {
        Author removed = byId.remove(id);
        if (removed != null) {
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
