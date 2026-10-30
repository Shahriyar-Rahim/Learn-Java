package library.repository;

import library.model.BookCopy;
import library.model.CopyStatus;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for physical BookCopy inventory.
 */
public class BookCopyRepository {

    private final DataStore dataStore;
    private final Map<String, BookCopy> byId = new ConcurrentHashMap<>();
    private final AtomicInteger globalSequence = new AtomicInteger(1000);

    public BookCopyRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<BookCopy> list = dataStore.loadCopies();
        int maxSeq = 1000;
        for (BookCopy c : list) {
            byId.put(c.getCopyId(), c);
            int num = extractNumber(c.getCopyId());
            if (num > maxSeq) maxSeq = num;
        }
        globalSequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveCopies(byId.values());
    }

    public synchronized String nextCopyId(String bookId) {
        String base = bookId != null ? bookId.replace("BK-", "") : "0";
        return "CPY-" + base + "-" + globalSequence.incrementAndGet();
    }

    public synchronized void save(BookCopy copy) {
        if (copy == null || copy.getCopyId() == null) {
            throw new IllegalArgumentException("Copy and Copy ID cannot be null.");
        }
        byId.put(copy.getCopyId(), copy);
        flush();
    }

    public Optional<BookCopy> findById(String copyId) {
        if (copyId == null) return Optional.empty();
        return Optional.ofNullable(byId.get(copyId));
    }

    public List<BookCopy> findAll() {
        return new ArrayList<>(byId.values());
    }

    public List<BookCopy> findByBookId(String bookId) {
        if (bookId == null) return Collections.emptyList();
        List<BookCopy> list = new ArrayList<>();
        for (BookCopy c : byId.values()) {
            if (bookId.equals(c.getBookId())) {
                list.add(c);
            }
        }
        return list;
    }

    public List<BookCopy> findAvailableByBookId(String bookId) {
        if (bookId == null) return Collections.emptyList();
        List<BookCopy> list = new ArrayList<>();
        for (BookCopy c : byId.values()) {
            if (bookId.equals(c.getBookId()) && c.getStatus() == CopyStatus.AVAILABLE) {
                list.add(c);
            }
        }
        return list;
    }

    public synchronized boolean delete(String copyId) {
        BookCopy removed = byId.remove(copyId);
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
            String[] parts = id.split("-");
            String last = parts[parts.length - 1];
            return Integer.parseInt(last);
        } catch (Exception e) {
            return 0;
        }
    }
}
