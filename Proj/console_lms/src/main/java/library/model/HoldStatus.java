package library.model;

public enum HoldStatus {
    ACTIVE("Active in queue"),
    FULFILLED("Fulfilled and borrowed"),
    CANCELLED("Cancelled by user or administrator"),
    EXPIRED("Expired waiting for pickup");

    private final String description;

    HoldStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
