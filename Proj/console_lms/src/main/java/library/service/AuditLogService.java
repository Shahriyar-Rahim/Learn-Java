package library.service;

import library.model.AuditLogEntry;
import library.repository.AuditLogRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.*;

/**
 * Service for system audit logging and standard Java logging.
 * Guarantees no sensitive data (passwords, hashes, secrets) is ever recorded.
 */
public class AuditLogService {

    private static final Logger LOGGER = Logger.getLogger("LibrarySystem");
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, Path logDir) {
        this.auditLogRepository = auditLogRepository;
        setupFileLogger(logDir);
    }

    private void setupFileLogger(Path logDir) {
        try {
            Files.createDirectories(logDir);
            Path logFile = logDir.resolve("library.log");
            FileHandler fileHandler = new FileHandler(logFile.toString(), 5 * 1024 * 1024, 3, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                private static final String FORMAT = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(FORMAT,
                            lr.getMillis(),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage()
                    );
                }
            });
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // keep console clean for CLI UI
        } catch (IOException e) {
            System.err.println("Warning: Could not configure file logger: " + e.getMessage());
        }
    }

    public synchronized void log(String action, String performedBy, String targetEntity, String details, boolean success) {
        String logId = auditLogRepository.nextId();
        AuditLogEntry entry = new AuditLogEntry(
                logId,
                LocalDateTime.now(),
                action,
                performedBy,
                targetEntity,
                details,
                success
        );
        auditLogRepository.save(entry);

        Level level = success ? Level.INFO : Level.WARNING;
        LOGGER.log(level, entry.toString());
    }

    public List<AuditLogEntry> getRecentLogs(int limit) {
        return auditLogRepository.findRecent(limit);
    }

    public List<AuditLogEntry> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
