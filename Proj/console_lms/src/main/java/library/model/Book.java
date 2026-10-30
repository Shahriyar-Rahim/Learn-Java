package library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a title/work in the library catalogue.
 */
public class Book {
    private String id;
    private String isbn;
    private String title;
    private String description;
    private List<String> authorIds = new ArrayList<>();
    private String sectionId;
    private String publisher;
    private LocalDate publicationDate;
    private String language;
    private String edition;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Book() {
    }

    public Book(String id, String isbn, String title, String description, 
                List<String> authorIds, String sectionId, String publisher, 
                LocalDate publicationDate, String language, String edition, 
                boolean archived, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.isbn = isbn != null ? isbn : "";
        this.title = title;
        this.description = description != null ? description : "";
        this.authorIds = authorIds != null ? new ArrayList<>(authorIds) : new ArrayList<>();
        this.sectionId = sectionId != null ? sectionId : "";
        this.publisher = publisher != null ? publisher : "";
        this.publicationDate = publicationDate;
        this.language = language != null ? language : "English";
        this.edition = edition != null ? edition : "1st";
        this.archived = archived;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
        this.updatedAt = LocalDateTime.now();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public List<String> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(List<String> authorIds) {
        this.authorIds = authorIds != null ? new ArrayList<>(authorIds) : new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public void addAuthorId(String authorId) {
        if (authorId != null && !this.authorIds.contains(authorId)) {
            this.authorIds.add(authorId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", archived=" + archived +
                '}';
    }
}
