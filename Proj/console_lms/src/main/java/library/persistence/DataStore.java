package library.persistence;

import library.exception.PersistenceException;
import library.model.*;
import library.persistence.json.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust filesystem persistence manager.
 * Uses atomic temporary-file writes, automated backups, and corruption recovery.
 */
public class DataStore {

    private static final Logger LOGGER = Logger.getLogger(DataStore.class.getName());

    private final Path dataDir;
    private final Path usersFile;
    private final Path authorsFile;
    private final Path sectionsFile;
    private final Path booksFile;
    private final Path copiesFile;
    private final Path loansFile;
    private final Path holdsFile;
    private final Path configFile;
    private final Path auditLogsFile;

    public DataStore(Path dataDir) {
        this.dataDir = dataDir;
        this.usersFile = dataDir.resolve("users.json");
        this.authorsFile = dataDir.resolve("authors.json");
        this.sectionsFile = dataDir.resolve("sections.json");
        this.booksFile = dataDir.resolve("books.json");
        this.copiesFile = dataDir.resolve("copies.json");
        this.loansFile = dataDir.resolve("loans.json");
        this.holdsFile = dataDir.resolve("holds.json");
        this.configFile = dataDir.resolve("config.json");
        this.auditLogsFile = dataDir.resolve("audit_logs.json");

        initDirectories();
    }

    private void initDirectories() {
        try {
            Files.createDirectories(dataDir);
            Files.createDirectories(dataDir.resolve("logs"));
        } catch (IOException e) {
            throw new PersistenceException("Failed to initialize data directories at " + dataDir, e);
        }
    }

    // -------------------------------------------------------------------------
    // USERS
    // -------------------------------------------------------------------------
    public synchronized List<User> loadUsers() {
        JsonValue val = readFileWithRecovery(usersFile);
        List<User> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToUser(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveUsers(Collection<User> users) {
        JsonArray array = new JsonArray();
        for (User u : users) {
            array.add(EntityMappers.userToJson(u));
        }
        writeFileAtomically(usersFile, array);
    }

    // -------------------------------------------------------------------------
    // AUTHORS
    // -------------------------------------------------------------------------
    public synchronized List<Author> loadAuthors() {
        JsonValue val = readFileWithRecovery(authorsFile);
        List<Author> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToAuthor(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveAuthors(Collection<Author> authors) {
        JsonArray array = new JsonArray();
        for (Author a : authors) {
            array.add(EntityMappers.authorToJson(a));
        }
        writeFileAtomically(authorsFile, array);
    }

    // -------------------------------------------------------------------------
    // SECTIONS
    // -------------------------------------------------------------------------
    public synchronized List<CatalogueSection> loadSections() {
        JsonValue val = readFileWithRecovery(sectionsFile);
        List<CatalogueSection> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToSection(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveSections(Collection<CatalogueSection> sections) {
        JsonArray array = new JsonArray();
        for (CatalogueSection s : sections) {
            array.add(EntityMappers.sectionToJson(s));
        }
        writeFileAtomically(sectionsFile, array);
    }

    // -------------------------------------------------------------------------
    // BOOKS
    // -------------------------------------------------------------------------
    public synchronized List<Book> loadBooks() {
        JsonValue val = readFileWithRecovery(booksFile);
        List<Book> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToBook(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveBooks(Collection<Book> books) {
        JsonArray array = new JsonArray();
        for (Book b : books) {
            array.add(EntityMappers.bookToJson(b));
        }
        writeFileAtomically(booksFile, array);
    }

    // -------------------------------------------------------------------------
    // COPIES
    // -------------------------------------------------------------------------
    public synchronized List<BookCopy> loadCopies() {
        JsonValue val = readFileWithRecovery(copiesFile);
        List<BookCopy> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToCopy(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveCopies(Collection<BookCopy> copies) {
        JsonArray array = new JsonArray();
        for (BookCopy c : copies) {
            array.add(EntityMappers.copyToJson(c));
        }
        writeFileAtomically(copiesFile, array);
    }

    // -------------------------------------------------------------------------
    // LOANS
    // -------------------------------------------------------------------------
    public synchronized List<BorrowRecord> loadLoans() {
        JsonValue val = readFileWithRecovery(loansFile);
        List<BorrowRecord> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToBorrow(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveLoans(Collection<BorrowRecord> loans) {
        JsonArray array = new JsonArray();
        for (BorrowRecord l : loans) {
            array.add(EntityMappers.borrowToJson(l));
        }
        writeFileAtomically(loansFile, array);
    }

    // -------------------------------------------------------------------------
    // HOLDS
    // -------------------------------------------------------------------------
    public synchronized List<HoldRecord> loadHolds() {
        JsonValue val = readFileWithRecovery(holdsFile);
        List<HoldRecord> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToHold(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveHolds(Collection<HoldRecord> holds) {
        JsonArray array = new JsonArray();
        for (HoldRecord h : holds) {
            array.add(EntityMappers.holdToJson(h));
        }
        writeFileAtomically(holdsFile, array);
    }

    // -------------------------------------------------------------------------
    // CONFIGURATION
    // -------------------------------------------------------------------------
    public synchronized LibraryConfiguration loadConfig() {
        JsonValue val = readFileWithRecovery(configFile);
        if (val != null && val.isObject()) {
            return EntityMappers.jsonToConfig(val.asObject());
        }
        return new LibraryConfiguration();
    }

    public synchronized void saveConfig(LibraryConfiguration config) {
        JsonObject obj = EntityMappers.configToJson(config);
        writeFileAtomically(configFile, obj);
    }

    // -------------------------------------------------------------------------
    // AUDIT LOGS
    // -------------------------------------------------------------------------
    public synchronized List<AuditLogEntry> loadAuditLogs() {
        JsonValue val = readFileWithRecovery(auditLogsFile);
        List<AuditLogEntry> list = new ArrayList<>();
        if (val != null && val.isArray()) {
            for (JsonValue v : val.asArray()) {
                if (v.isObject()) {
                    list.add(EntityMappers.jsonToAuditLog(v.asObject()));
                }
            }
        }
        return list;
    }

    public synchronized void saveAuditLogs(Collection<AuditLogEntry> logs) {
        JsonArray array = new JsonArray();
        for (AuditLogEntry l : logs) {
            array.add(EntityMappers.auditLogToJson(l));
        }
        writeFileAtomically(auditLogsFile, array);
    }

    // -------------------------------------------------------------------------
    // ATOMIC FILE I/O AND DISASTER RECOVERY
    // -------------------------------------------------------------------------
    private JsonValue readFileWithRecovery(Path target) {
        if (!Files.exists(target)) {
            return null;
        }
        try {
            String content = Files.readString(target, StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                return null;
            }
            return JsonParser.parse(content);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Primary file " + target.getFileName() + " corrupted or unreadable. Checking backup...", e);
            Path backup = target.resolveSibling(target.getFileName() + ".bak");
            if (Files.exists(backup)) {
                try {
                    String backupContent = Files.readString(backup, StandardCharsets.UTF_8);
                    LOGGER.info("Successfully recovered from backup: " + backup.getFileName());
                    return JsonParser.parse(backupContent);
                } catch (Exception be) {
                    LOGGER.log(Level.SEVERE, "Failed to recover from backup " + backup.getFileName(), be);
                }
            }
            return null;
        }
    }

    private void writeFileAtomically(Path target, JsonValue value) {
        try {
            Path parent = target.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            // Create backup if target file already exists
            if (Files.exists(target)) {
                Path backup = target.resolveSibling(target.getFileName() + ".bak");
                try {
                    Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Could not create backup for " + target.getFileName(), e);
                }
            }

            // Write to temporary file
            Path tempFile = target.resolveSibling(target.getFileName() + ".tmp");
            String jsonStr = JsonWriter.format(value);
            Files.writeString(tempFile, jsonStr, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.SYNC);

            // Atomically replace target file
            try {
                Files.move(tempFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenceException("Failed to save data atomically to " + target, e);
        }
    }
}
