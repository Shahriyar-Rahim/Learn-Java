package library.repository;

import library.model.CatalogueSection;
import library.persistence.DataStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe repository for CatalogueSections.
 */
public class SectionRepository {

    private final DataStore dataStore;
    private final Map<String, CatalogueSection> byId = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    public SectionRepository(DataStore dataStore) {
        this.dataStore = dataStore;
        load();
    }

    private synchronized void load() {
        byId.clear();
        List<CatalogueSection> list = dataStore.loadSections();
        int maxSeq = 1000;
        for (CatalogueSection s : list) {
            byId.put(s.getId(), s);
            int num = extractNumber(s.getId());
            if (num > maxSeq) maxSeq = num;
        }
        sequence.set(maxSeq);
    }

    public synchronized void flush() {
        dataStore.saveSections(byId.values());
    }

    public synchronized String nextId() {
        return "SEC-" + sequence.incrementAndGet();
    }

    public synchronized void save(CatalogueSection section) {
        if (section == null || section.getId() == null) {
            throw new IllegalArgumentException("Section and Section ID cannot be null.");
        }
        byId.put(section.getId(), section);
        flush();
    }

    public Optional<CatalogueSection> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<CatalogueSection> findByName(String name) {
        if (name == null) return Optional.empty();
        String trimName = name.trim().toLowerCase();
        for (CatalogueSection s : byId.values()) {
            if (s.getName() != null && s.getName().trim().equalsIgnoreCase(trimName)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public List<CatalogueSection> findAll() {
        return new ArrayList<>(byId.values());
    }

    public synchronized boolean delete(String id) {
        CatalogueSection removed = byId.remove(id);
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
