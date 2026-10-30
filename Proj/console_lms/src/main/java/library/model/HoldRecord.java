package library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a reservation / hold request on a book.
 */
public class HoldRecord {
    private String holdId;
    private String userId;
    private String bookId;
    private LocalDateTime requestDate;
    private HoldStatus status;
    private int priority;
    private String reservedCopyId;
    private LocalDateTime fulfillmentDate;
    private LocalDateTime expiryDate;
    private String notes;

    public HoldRecord() {
    }

    public HoldRecord(String holdId, String userId, String bookId, LocalDateTime requestDate, 
                      HoldStatus status, int priority, String reservedCopyId, 
                      LocalDateTime fulfillmentDate, LocalDateTime expiryDate, String notes) {
        this.holdId = holdId;
        this.userId = userId;
        this.bookId = bookId;
        this.requestDate = requestDate != null ? requestDate : LocalDateTime.now();
        this.status = status != null ? status : HoldStatus.ACTIVE;
        this.priority = priority;
        this.reservedCopyId = reservedCopyId;
        this.fulfillmentDate = fulfillmentDate;
        this.expiryDate = expiryDate;
        this.notes = notes != null ? notes : "";
    }

    public String getHoldId() {
        return holdId;
    }

    public void setHoldId(String holdId) {
        this.holdId = holdId;
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

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public HoldStatus getStatus() {
        return status;
    }

    public void setStatus(HoldStatus status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getReservedCopyId() {
        return reservedCopyId;
    }

    public void setReservedCopyId(String reservedCopyId) {
        this.reservedCopyId = reservedCopyId;
    }

    public LocalDateTime getFulfillmentDate() {
        return fulfillmentDate;
    }

    public void setFulfillmentDate(LocalDateTime fulfillmentDate) {
        this.fulfillmentDate = fulfillmentDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return status == HoldStatus.ACTIVE;
    }

    public boolean isExpired(LocalDateTime referenceDateTime) {
        return isActive() && expiryDate != null && referenceDateTime.isAfter(expiryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoldRecord that = (HoldRecord) o;
        return Objects.equals(holdId, that.holdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holdId);
    }

    @Override
    public String toString() {
        return "HoldRecord{" +
                "holdId='" + holdId + '\'' +
                ", userId='" + userId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
