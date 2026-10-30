package library.repository;

import library.model.BorrowRecord;
import library.model.BorrowStatus;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for borrowing / loan records.
 */
public class BorrowRepository {

    private final DataStore dataStore;
    private final Map<String, BorrowRecord> byId = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public BorrowRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<BorrowRecord> list = dataStore.loadLoans();
        int maxSeq = 1000;
        for (BorrowRecord l : list) {
            byId.put(l.getBorrowId(), l);
            int num = extractNumber(l.getBorrowId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveLoans(byId.values());
    }

    public synchronized String nextId() {
        return "BRW-" + sequence.incrementAndGet();
    }

    public synchronized void save(BorrowRecord record) {
        if (record == null || record.getBorrowId() == null) {
            throw new IllegalArgumentException("Loan record and Borrow ID cannot be null.");
        }
        byId.put(record.getBorrowId(), record);
        flush();
    }

    public Optional<BorrowRecord> findById(String borrowId) {
        if (borrowId == null) return Optional.empty();
        return Optional.ofNullable(byId.get(borrowId));
    }

    public List<BorrowRecord> findAll() {
        return new ArrayList<>(byId.values());
    }

    public List<BorrowRecord> findActiveByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        List<BorrowRecord> list = new ArrayList<>();
        for (BorrowRecord r : byId.values()) {
            if (userId.equals(r.getUserId()) && r.getStatus() == BorrowStatus.ACTIVE) {
                list.add(r);
            }
        }
        return list;
    }

    public List<BorrowRecord> findAllByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        List<BorrowRecord> list = new ArrayList<>();
        for (BorrowRecord r : byId.values()) {
            if (userId.equals(r.getUserId())) {
                list.add(r);
            }
        }
        // sort most recent first
        list.sort((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()));
        return list;
    }

    public List<BorrowRecord> findActiveByBookId(String bookId) {
        if (bookId == null) return Collections.emptyList();
        List<BorrowRecord> list = new ArrayList<>();
        for (BorrowRecord r : byId.values()) {
            if (bookId.equals(r.getBookId()) && r.getStatus() == BorrowStatus.ACTIVE) {
                list.add(r);
            }
        }
        return list;
    }

    public Optional<BorrowRecord> findActiveByCopyId(String copyId) {
        if (copyId == null) return Optional.empty();
        for (BorrowRecord r : byId.values()) {
            if (copyId.equals(r.getCopyId()) && r.getStatus() == BorrowStatus.ACTIVE) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public Optional<BorrowRecord> findActiveByUserIdAndBookId(String userId, String bookId) {
        if (userId == null || bookId == null) return Optional.empty();
        for (BorrowRecord r : byId.values()) {
            if (userId.equals(r.getUserId()) && bookId.equals(r.getBookId()) && r.getStatus() == BorrowStatus.ACTIVE) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    public int countActiveLoansByUser(String userId) {
        if (userId == null) return 0;
        int count = 0;
        for (BorrowRecord r : byId.values()) {
            if (userId.equals(r.getUserId()) && r.getStatus() == BorrowStatus.ACTIVE) {
                count++;
            }
        }
        return count;
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
