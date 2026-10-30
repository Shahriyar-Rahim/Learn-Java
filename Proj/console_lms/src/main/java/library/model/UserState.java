package library.model;

public enum UserState {
    PENDING(false, false, false, "Pending Admin Approval"),
    APPROVED(true, true, true, "Approved & Active"),
    BLOCKED(false, false, false, "Blocked by Administrator"),
    SUSPENDED(true, false, false, "Suspended (Read-Only / Returns Only)"),
    ARCHIVED(false, false, false, "Archived"),
    REJECTED(false, false, false, "Registration Rejected");

    private final boolean canLogin;
    private final boolean canBorrow;
    private final boolean canHold;
    private final String description;

    UserState(boolean canLogin, boolean canBorrow, boolean canHold, String description) {
        this.canLogin = canLogin;
        this.canBorrow = canBorrow;
        this.canHold = canHold;
        this.description = description;
    }

    public boolean canLogin() {
        return canLogin;
    }

    public boolean canBorrow() {
        return canBorrow;
    }

    public boolean canHold() {
        return canHold;
    }

    public String getDescription() {
        return description;
    }
}
