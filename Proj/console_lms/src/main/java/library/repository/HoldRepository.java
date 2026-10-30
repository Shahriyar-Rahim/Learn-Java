package library.repository;

import library.model.HoldRecord;
import library.model.HoldStatus;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for Hold and reservation records.
 */
public class HoldRepository {

    private final DataStore dataStore;
    private final Map<String, HoldRecord> byId = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public HoldRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<HoldRecord> list = dataStore.loadHolds();
        int maxSeq = 1000;
        for (HoldRecord h : list) {
            byId.put(h.getHoldId(), h);
            int num = extractNumber(h.getHoldId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveHolds(byId.values());
    }

    public synchronized String nextId() {
        return "HLD-" + sequence.incrementAndGet();
    }

    public synchronized void save(HoldRecord record) {
        if (record == null || record.getHoldId() == null) {
            throw new IllegalArgumentException("Hold record and Hold ID cannot be null.");
        }
        byId.put(record.getHoldId(), record);
        flush();
    }

    public Optional<HoldRecord> findById(String holdId) {
        if (holdId == null) return Optional.empty();
        return Optional.ofNullable(byId.get(holdId));
    }

    public List<HoldRecord> findAll() {
        return new ArrayList<>(byId.values());
    }

    /**
     * Returns all active holds for a specific book ordered by priority ASC (FIFO).
     */
    public List<HoldRecord> findActiveByBookId(String bookId) {
        if (bookId == null) return Collections.emptyList();
        List<HoldRecord> list = new ArrayList<>();
        for (HoldRecord h : byId.values()) {
            if (bookId.equals(h.getBookId()) && h.getStatus() == HoldStatus.ACTIVE) {
                list.add(h);
            }
        }
        list.sort(Comparator.comparingInt(HoldRecord::getPriority));
        return list;
    }

    public List<HoldRecord> findActiveByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        List<HoldRecord> list = new ArrayList<>();
        for (HoldRecord h : byId.values()) {
            if (userId.equals(h.getUserId()) && h.getStatus() == HoldStatus.ACTIVE) {
                list.add(h);
            }
        }
        list.sort(Comparator.comparing(HoldRecord::getRequestDate));
        return list;
    }

    public List<HoldRecord> findAllByUserId(String userId) {
        if (userId == null) return Collections.emptyList();
        List<HoldRecord> list = new ArrayList<>();
        for (HoldRecord h : byId.values()) {
            if (userId.equals(h.getUserId())) {
                list.add(h);
            }
        }
        list.sort((a, b) -> b.getRequestDate().compareTo(a.getRequestDate()));
        return list;
    }

    public Optional<HoldRecord> findActiveByUserIdAndBookId(String userId, String bookId) {
        if (userId == null || bookId == null) return Optional.empty();
        for (HoldRecord h : byId.values()) {
            if (userId.equals(h.getUserId()) && bookId.equals(h.getBookId()) && h.getStatus() == HoldStatus.ACTIVE) {
                return Optional.of(h);
            }
        }
        return Optional.empty();
    }

    public Optional<HoldRecord> findReservedByCopyId(String copyId) {
        if (copyId == null) return Optional.empty();
        for (HoldRecord h : byId.values()) {
            if (copyId.equals(h.getReservedCopyId()) && h.getStatus() == HoldStatus.ACTIVE) {
                return Optional.of(h);
            }
        }
        return Optional.empty();
    }

    public int countActiveHoldsByUser(String userId) {
        if (userId == null) return 0;
        int count = 0;
        for (HoldRecord h : byId.values()) {
            if (userId.equals(h.getUserId()) && h.getStatus() == HoldStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getNextPriority(String bookId) {
        List<HoldRecord> active = findActiveByBookId(bookId);
        int max = 0;
        for (HoldRecord h : active) {
            if (h.getPriority() > max) max = h.getPriority();
        }
        return max + 1;
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
