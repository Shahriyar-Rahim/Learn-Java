package library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a specific physical inventory copy of a Book.
 */
public class BookCopy {
    private String copyId;
    private String bookId;
    private CopyStatus status;
    private String location;
    private BookCondition condition;
    private LocalDate acquisitionDate;
    private String notes;

    public BookCopy() {
    }

    public BookCopy(String copyId, String bookId, CopyStatus status, String location, 
                    BookCondition condition, LocalDate acquisitionDate, String notes) {
        this.copyId = copyId;
        this.bookId = bookId;
        this.status = status != null ? status : CopyStatus.AVAILABLE;
        this.location = location != null ? location : "Main Stacks";
        this.condition = condition != null ? condition : BookCondition.GOOD;
        this.acquisitionDate = acquisitionDate != null ? acquisitionDate : LocalDate.now();
        this.notes = notes != null ? notes : "";
    }

    public String getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BookCondition getCondition() {
        return condition;
    }

    public void setCondition(BookCondition condition) {
        this.condition = condition;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isAvailable() {
        return status == CopyStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCopy bookCopy = (BookCopy) o;
        return Objects.equals(copyId, bookCopy.copyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(copyId);
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId='" + copyId + '\'' +
                ", bookId='" + bookId + '\'' +
                ", status=" + status +
                ", location='" + location + '\'' +
                ", condition=" + condition +
                '}';
    }
}
