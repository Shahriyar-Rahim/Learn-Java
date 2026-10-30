package library.configuration;

/**
 * System-wide configuration constants and operational defaults.
 */
public final class AppConfig {

    public static final String APP_NAME = "Pure Java Library Management System";
    public static final String APP_VERSION = "1.0.0";
    
    // Default directory names
    public static final String DEFAULT_DATA_DIR = "data";
    public static final String DEFAULT_REPORTS_DIR = "reports";
    public static final String DEFAULT_LOGS_DIR = "logs";

    // Data store file names
    public static final String USERS_FILE = "users.json";
    public static final String BOOKS_FILE = "books.json";
    public static final String COPIES_FILE = "book_copies.json";
    public static final String AUTHORS_FILE = "authors.json";
    public static final String SECTIONS_FILE = "sections.json";
    public static final String BORROWS_FILE = "borrows.json";
    public static final String HOLDS_FILE = "holds.json";
    public static final String CONFIG_FILE = "configuration.json";
    public static final String AUDIT_LOGS_FILE = "audit_logs.json";

    // Business defaults
    public static final int DEFAULT_MAX_ACTIVE_LOANS = 5;
    public static final int DEFAULT_LOAN_DURATION_DAYS = 14;
    public static final int DEFAULT_MAX_RENEWALS = 2;
    public static final int DEFAULT_RENEWAL_PERIOD_DAYS = 14;
    public static final int DEFAULT_MAX_ACTIVE_HOLDS = 3;
    public static final int DEFAULT_HOLD_EXPIRY_DAYS = 3;
    public static final String DEFAULT_SYSTEM_NAME = "City Central Library";

    // Security constants
    public static final int PBKDF2_ITERATIONS = 65536;
    public static final int PBKDF2_KEY_LENGTH = 256;
    public static final int SALT_BYTES = 16;

    private AppConfig() {
    }
}
