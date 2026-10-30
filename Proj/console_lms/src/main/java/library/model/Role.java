package library.model;

public enum Role {
    ADMIN,
    MEMBER;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
