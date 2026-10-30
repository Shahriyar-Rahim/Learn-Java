package library.repository;

import library.model.Book;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for Books.
 */
public class BookRepository {

    private final DataStore dataStore;
    private final Map<String, Book> byId = new ConcurrentHashMap<>();
    private final Map<String, String> idByIsbn = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public BookRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        idByIsbn.clear();
        List<Book> list = dataStore.loadBooks();
        int maxSeq = 1000;
        for (Book b : list) {
            byId.put(b.getId(), b);
            if (b.getIsbn() != null && !b.getIsbn().isBlank()) {
                idByIsbn.put(normalizeIsbn(b.getIsbn()), b.getId());
            }
            int num = extractNumber(b.getId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveBooks(byId.values());
    }

    public synchronized String nextId() {
        return "BK-" + sequence.incrementAndGet();
    }

    public synchronized void save(Book book) {
        if (book == null || book.getId() == null) {
            throw new IllegalArgumentException("Book and Book ID cannot be null.");
        }
        Book existing = byId.get(book.getId());
        if (existing != null && existing.getIsbn() != null && !existing.getIsbn().isBlank()) {
            idByIsbn.remove(normalizeIsbn(existing.getIsbn()));
        }
        byId.put(book.getId(), book);
        if (book.getIsbn() != null && !book.getIsbn().isBlank()) {
            idByIsbn.put(normalizeIsbn(book.getIsbn()), book.getId());
        }
        flush();
    }

    public Optional<Book> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<Book> findByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return Optional.empty();
        String id = idByIsbn.get(normalizeIsbn(isbn));
        return id != null ? Optional.ofNullable(byId.get(id)) : Optional.empty();
    }

    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return false;
        return idByIsbn.containsKey(normalizeIsbn(isbn));
    }

    public List<Book> findAll() {
        return new ArrayList<>(byId.values());
    }

    public List<Book> findBySectionId(String sectionId) {
        if (sectionId == null) return Collections.emptyList();
        List<Book> list = new ArrayList<>();
        for (Book b : byId.values()) {
            if (sectionId.equals(b.getSectionId())) {
                list.add(b);
            }
        }
        return list;
    }

    public List<Book> findByAuthorId(String authorId) {
        if (authorId == null) return Collections.emptyList();
        List<Book> list = new ArrayList<>();
        for (Book b : byId.values()) {
            if (b.getAuthorIds().contains(authorId)) {
                list.add(b);
            }
        }
        return list;
    }

    public synchronized boolean delete(String id) {
        Book removed = byId.remove(id);
        if (removed != null) {
            if (removed.getIsbn() != null && !removed.getIsbn().isBlank()) {
                idByIsbn.remove(normalizeIsbn(removed.getIsbn()));
            }
            flush();
            return true;
        }
        return false;
    }

    public int count() {
        return byId.size();
    }

    private String normalizeIsbn(String isbn) {
        return isbn.trim().replaceAll("[-\\s]", "").toLowerCase();
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
