package library.repository;

import library.model.AuditLogEntry;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for AuditLogEntry records.
 */
public class AuditLogRepository {

    private final DataStore dataStore;
    private final Map<String, AuditLogEntry> byId = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public AuditLogRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<AuditLogEntry> list = dataStore.loadAuditLogs();
        int maxSeq = 1000;
        for (AuditLogEntry l : list) {
            byId.put(l.getId(), l);
            int num = extractNumber(l.getId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveAuditLogs(byId.values());
    }

    public synchronized String nextId() {
        return "LOG-" + sequence.incrementAndGet();
    }

    public synchronized void save(AuditLogEntry entry) {
        if (entry == null || entry.getId() == null) {
            throw new IllegalArgumentException("Audit log entry and ID cannot be null.");
        }
        byId.put(entry.getId(), entry);
        flush();
    }

    public List<AuditLogEntry> findAll() {
        List<AuditLogEntry> list = new ArrayList<>(byId.values());
        list.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return list;
    }

    public List<AuditLogEntry> findRecent(int limit) {
        List<AuditLogEntry> all = findAll();
        if (all.size() <= limit) return all;
        return all.subList(0, limit);
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
