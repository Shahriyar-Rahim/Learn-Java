package library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a borrowing transaction / loan record.
 */
public class BorrowRecord {
    private String borrowId;
    private String userId;
    private String bookId;
    private String copyId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private int renewalCount;
    private BorrowStatus status;
    private String notes;

    public BorrowRecord() {
    }

    public BorrowRecord(String borrowId, String userId, String bookId, String copyId, 
                        LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate, 
                        int renewalCount, BorrowStatus status, String notes) {
        this.borrowId = borrowId;
        this.userId = userId;
        this.bookId = bookId;
        this.copyId = copyId;
        this.borrowDate = borrowDate != null ? borrowDate : LocalDate.now();
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.renewalCount = renewalCount;
        this.status = status != null ? status : BorrowStatus.ACTIVE;
        this.notes = notes != null ? notes : "";
    }

    public String getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(String borrowId) {
        this.borrowId = borrowId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(int renewalCount) {
        this.renewalCount = renewalCount;
    }

    public BorrowStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return status == BorrowStatus.ACTIVE;
    }

    public boolean isOverdue(LocalDate referenceDate) {
        return isActive() && dueDate != null && referenceDate.isAfter(dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecord that = (BorrowRecord) o;
        return Objects.equals(borrowId, that.borrowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(borrowId);
    }

    @Override
    public String toString() {
        return "BorrowRecord{" +
                "borrowId='" + borrowId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", copyId='" + copyId + '\'' +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
