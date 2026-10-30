package library.model;

public enum CopyStatus {
    AVAILABLE("Available for checkout"),
    BORROWED("Currently checked out"),
    RESERVED("Reserved for hold pickup"),
    LOST("Reported lost"),
    DAMAGED("Damaged / Under repair"),
    ARCHIVED("Archived / Retired");

    private final String description;

    CopyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isBorrowable() {
        return this == AVAILABLE || this == RESERVED;
    }
}
