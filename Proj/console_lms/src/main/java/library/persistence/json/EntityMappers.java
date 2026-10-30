package library.persistence.json;

import library.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps domain models to and from JsonObject structures.
 */
public final class EntityMappers {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private EntityMappers() {
    }

    // -------------------------------------------------------------
    // USER
    // -------------------------------------------------------------
    public static JsonObject userToJson(User user) {
        JsonObject json = new JsonObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("passwordHash", user.getPasswordHash());
        json.put("fullName", user.getFullName());
        json.put("email", user.getEmail());
        json.put("phone", user.getPhone());
        json.put("role", user.getRole().name());
        json.put("state", user.getState().name());
        json.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_TIME_FMT) : null);
        json.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().format(DATE_TIME_FMT) : null);
        return json;
    }

    public static User jsonToUser(JsonObject json) {
        User user = new User();
        user.setId(json.getString("id", null));
        user.setUsername(json.getString("username", null));
        user.setPasswordHash(json.getString("passwordHash", null));
        user.setFullName(json.getString("fullName", ""));
        user.setEmail(json.getString("email", ""));
        user.setPhone(json.getString("phone", ""));
        String roleStr = json.getString("role", Role.MEMBER.name());
        try {
            user.setRole(Role.valueOf(roleStr));
        } catch (IllegalArgumentException e) {
            user.setRole(Role.MEMBER);
        }
        String stateStr = json.getString("state", UserState.PENDING.name());
        try {
            user.setState(UserState.valueOf(stateStr));
        } catch (IllegalArgumentException e) {
            user.setState(UserState.PENDING);
        }
        user.setCreatedAt(parseDateTime(json.getString("createdAt", null)));
        user.setUpdatedAt(parseDateTime(json.getString("updatedAt", null)));
        return user;
    }

    // -------------------------------------------------------------
    // AUTHOR
    // -------------------------------------------------------------
    public static JsonObject authorToJson(Author author) {
        JsonObject json = new JsonObject();
        json.put("id", author.getId());
        json.put("name", author.getName());
        json.put("biography", author.getBiography());
        if (author.getBirthYear() != null) {
            json.put("birthYear", author.getBirthYear());
        }
        json.put("archived", author.isArchived());
        json.put("createdAt", author.getCreatedAt() != null ? author.getCreatedAt().format(DATE_TIME_FMT) : null);
        return json;
    }

    public static Author jsonToAuthor(JsonObject json) {
        Author author = new Author();
        author.setId(json.getString("id", null));
        author.setName(json.getString("name", ""));
        author.setBiography(json.getString("biography", ""));
        if (json.has("birthYear") && !json.get("birthYear").isNull()) {
            author.setBirthYear(json.getInt("birthYear", 0));
        }
        author.setArchived(json.getBoolean("archived", false));
        author.setCreatedAt(parseDateTime(json.getString("createdAt", null)));
        return author;
    }

    // -------------------------------------------------------------
    // CATALOGUE SECTION
    // -------------------------------------------------------------
    public static JsonObject sectionToJson(CatalogueSection section) {
        JsonObject json = new JsonObject();
        json.put("id", section.getId());
        json.put("name", section.getName());
        json.put("description", section.getDescription());
        json.put("archived", section.isArchived());
        json.put("createdAt", section.getCreatedAt() != null ? section.getCreatedAt().format(DATE_TIME_FMT) : null);
        return json;
    }

    public static CatalogueSection jsonToSection(JsonObject json) {
        CatalogueSection section = new CatalogueSection();
        section.setId(json.getString("id", null));
        section.setName(json.getString("name", ""));
        section.setDescription(json.getString("description", ""));
        section.setArchived(json.getBoolean("archived", false));
        section.setCreatedAt(parseDateTime(json.getString("createdAt", null)));
        return section;
    }

    // -------------------------------------------------------------
    // BOOK
    // -------------------------------------------------------------
    public static JsonObject bookToJson(Book book) {
        JsonObject json = new JsonObject();
        json.put("id", book.getId());
        json.put("isbn", book.getIsbn());
        json.put("title", book.getTitle());
        json.put("description", book.getDescription());
        JsonArray authorsArray = new JsonArray();
        for (String authorId : book.getAuthorIds()) {
            authorsArray.add(authorId);
        }
        json.put("authorIds", authorsArray);
        json.put("sectionId", book.getSectionId());
        json.put("publisher", book.getPublisher());
        json.put("publicationDate", book.getPublicationDate() != null ? book.getPublicationDate().format(DATE_FMT) : null);
        json.put("language", book.getLanguage());
        json.put("edition", book.getEdition());
        json.put("archived", book.isArchived());
        json.put("createdAt", book.getCreatedAt() != null ? book.getCreatedAt().format(DATE_TIME_FMT) : null);
        json.put("updatedAt", book.getUpdatedAt() != null ? book.getUpdatedAt().format(DATE_TIME_FMT) : null);
        return json;
    }

    public static Book jsonToBook(JsonObject json) {
        Book book = new Book();
        book.setId(json.getString("id", null));
        book.setIsbn(json.getString("isbn", ""));
        book.setTitle(json.getString("title", ""));
        book.setDescription(json.getString("description", ""));
        List<String> authors = new ArrayList<>();
        JsonArray arr = json.getArray("authorIds");
        if (arr != null) {
            for (JsonValue v : arr) {
                if (v.isString()) authors.add(v.asString());
            }
        }
        book.setAuthorIds(authors);
        book.setSectionId(json.getString("sectionId", ""));
        book.setPublisher(json.getString("publisher", ""));
        book.setPublicationDate(parseDate(json.getString("publicationDate", null)));
        book.setLanguage(json.getString("language", "English"));
        book.setEdition(json.getString("edition", "1st"));
        book.setArchived(json.getBoolean("archived", false));
        book.setCreatedAt(parseDateTime(json.getString("createdAt", null)));
        book.setUpdatedAt(parseDateTime(json.getString("updatedAt", null)));
        return book;
    }

    // -------------------------------------------------------------
    // BOOK COPY
    // -------------------------------------------------------------
    public static JsonObject copyToJson(BookCopy copy) {
        JsonObject json = new JsonObject();
        json.put("copyId", copy.getCopyId());
        json.put("bookId", copy.getBookId());
        json.put("status", copy.getStatus().name());
        json.put("location", copy.getLocation());
        json.put("condition", copy.getCondition().name());
        json.put("acquisitionDate", copy.getAcquisitionDate() != null ? copy.getAcquisitionDate().format(DATE_FMT) : null);
        json.put("notes", copy.getNotes());
        return json;
    }

    public static BookCopy jsonToCopy(JsonObject json) {
        BookCopy copy = new BookCopy();
        copy.setCopyId(json.getString("copyId", null));
        copy.setBookId(json.getString("bookId", null));
        String statusStr = json.getString("status", CopyStatus.AVAILABLE.name());
        try {
            copy.setStatus(CopyStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            copy.setStatus(CopyStatus.AVAILABLE);
        }
        copy.setLocation(json.getString("location", "Main Stacks"));
        String condStr = json.getString("condition", BookCondition.GOOD.name());
        try {
            copy.setCondition(BookCondition.valueOf(condStr));
        } catch (IllegalArgumentException e) {
            copy.setCondition(BookCondition.GOOD);
        }
        copy.setAcquisitionDate(parseDate(json.getString("acquisitionDate", null)));
        copy.setNotes(json.getString("notes", ""));
        return copy;
    }

    // -------------------------------------------------------------
    // BORROW RECORD
    // -------------------------------------------------------------
    public static JsonObject borrowToJson(BorrowRecord record) {
        JsonObject json = new JsonObject();
        json.put("borrowId", record.getBorrowId());
        json.put("userId", record.getUserId());
        json.put("bookId", record.getBookId());
        json.put("copyId", record.getCopyId());
        json.put("borrowDate", record.getBorrowDate() != null ? record.getBorrowDate().format(DATE_FMT) : null);
        json.put("dueDate", record.getDueDate() != null ? record.getDueDate().format(DATE_FMT) : null);
        json.put("returnDate", record.getReturnDate() != null ? record.getReturnDate().format(DATE_FMT) : null);
        json.put("renewalCount", record.getRenewalCount());
        json.put("status", record.getStatus().name());
        json.put("notes", record.getNotes());
        return json;
    }

    public static BorrowRecord jsonToBorrow(JsonObject json) {
        BorrowRecord record = new BorrowRecord();
        record.setBorrowId(json.getString("borrowId", null));
        record.setUserId(json.getString("userId", null));
        record.setBookId(json.getString("bookId", null));
        record.setCopyId(json.getString("copyId", null));
        record.setBorrowDate(parseDate(json.getString("borrowDate", null)));
        record.setDueDate(parseDate(json.getString("dueDate", null)));
        record.setReturnDate(parseDate(json.getString("returnDate", null)));
        record.setRenewalCount(json.getInt("renewalCount", 0));
        String statusStr = json.getString("status", BorrowStatus.ACTIVE.name());
        try {
            record.setStatus(BorrowStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            record.setStatus(BorrowStatus.ACTIVE);
        }
        record.setNotes(json.getString("notes", ""));
        return record;
    }

    // -------------------------------------------------------------
    // HOLD RECORD
    // -------------------------------------------------------------
    public static JsonObject holdToJson(HoldRecord hold) {
        JsonObject json = new JsonObject();
        json.put("holdId", hold.getHoldId());
        json.put("userId", hold.getUserId());
        json.put("bookId", hold.getBookId());
        json.put("requestDate", hold.getRequestDate() != null ? hold.getRequestDate().format(DATE_TIME_FMT) : null);
        json.put("status", hold.getStatus().name());
        json.put("priority", hold.getPriority());
        json.put("reservedCopyId", hold.getReservedCopyId());
        json.put("fulfillmentDate", hold.getFulfillmentDate() != null ? hold.getFulfillmentDate().format(DATE_TIME_FMT) : null);
        json.put("expiryDate", hold.getExpiryDate() != null ? hold.getExpiryDate().format(DATE_TIME_FMT) : null);
        json.put("notes", hold.getNotes());
        return json;
    }

    public static HoldRecord jsonToHold(JsonObject json) {
        HoldRecord hold = new HoldRecord();
        hold.setHoldId(json.getString("holdId", null));
        hold.setUserId(json.getString("userId", null));
        hold.setBookId(json.getString("bookId", null));
        hold.setRequestDate(parseDateTime(json.getString("requestDate", null)));
        String statusStr = json.getString("status", HoldStatus.ACTIVE.name());
        try {
            hold.setStatus(HoldStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            hold.setStatus(HoldStatus.ACTIVE);
        }
        hold.setPriority(json.getInt("priority", 0));
        hold.setReservedCopyId(json.getString("reservedCopyId", null));
        hold.setFulfillmentDate(parseDateTime(json.getString("fulfillmentDate", null)));
        hold.setExpiryDate(parseDateTime(json.getString("expiryDate", null)));
        hold.setNotes(json.getString("notes", ""));
        return hold;
    }

    // -------------------------------------------------------------
    // CONFIGURATION
    // -------------------------------------------------------------
    public static JsonObject configToJson(LibraryConfiguration config) {
        JsonObject json = new JsonObject();
        json.put("maxActiveLoans", config.getMaxActiveLoans());
        json.put("defaultLoanDurationDays", config.getDefaultLoanDurationDays());
        json.put("maxRenewals", config.getMaxRenewals());
        json.put("renewalPeriodDays", config.getRenewalPeriodDays());
        json.put("maxActiveHolds", config.getMaxActiveHolds());
        json.put("holdExpiryDays", config.getHoldExpiryDays());
        json.put("systemName", config.getSystemName());
        return json;
    }

    public static LibraryConfiguration jsonToConfig(JsonObject json) {
        LibraryConfiguration config = new LibraryConfiguration();
        config.setMaxActiveLoans(json.getInt("maxActiveLoans", 5));
        config.setDefaultLoanDurationDays(json.getInt("defaultLoanDurationDays", 14));
        config.setMaxRenewals(json.getInt("maxRenewals", 2));
        config.setRenewalPeriodDays(json.getInt("renewalPeriodDays", 14));
        config.setMaxActiveHolds(json.getInt("maxActiveHolds", 3));
        config.setHoldExpiryDays(json.getInt("holdExpiryDays", 3));
        config.setSystemName(json.getString("systemName", "City Central Library"));
        return config;
    }

    // -------------------------------------------------------------
    // AUDIT LOG
    // -------------------------------------------------------------
    public static JsonObject auditLogToJson(AuditLogEntry entry) {
        JsonObject json = new JsonObject();
        json.put("id", entry.getId());
        json.put("timestamp", entry.getTimestamp() != null ? entry.getTimestamp().format(DATE_TIME_FMT) : null);
        json.put("action", entry.getAction());
        json.put("performedBy", entry.getPerformedBy());
        json.put("targetEntity", entry.getTargetEntity());
        json.put("details", entry.getDetails());
        json.put("success", entry.isSuccess());
        return json;
    }

    public static AuditLogEntry jsonToAuditLog(JsonObject json) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setId(json.getString("id", null));
        entry.setTimestamp(parseDateTime(json.getString("timestamp", null)));
        entry.setAction(json.getString("action", ""));
        entry.setPerformedBy(json.getString("performedBy", "SYSTEM"));
        entry.setTargetEntity(json.getString("targetEntity", ""));
        entry.setDetails(json.getString("details", ""));
        entry.setSuccess(json.getBoolean("success", true));
        return entry;
    }

    private static LocalDate parseDate(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDate.parse(str, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return LocalDateTime.parse(str, DATE_TIME_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
