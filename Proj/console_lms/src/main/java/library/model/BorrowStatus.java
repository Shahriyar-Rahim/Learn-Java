package library.model;

public enum BorrowStatus {
    ACTIVE("Active loan"),
    RETURNED("Returned safely"),
    OVERDUE("Overdue for return"),
    LOST("Marked lost by borrower"),
    CANCELLED("Loan cancelled");

    private final String description;

    BorrowStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
