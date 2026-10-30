package library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an author in the library catalogue.
 */
public class Author {
    private String id;
    private String name;
    private String biography;
    private Integer birthYear;
    private boolean archived;
    private LocalDateTime createdAt;

    public Author() {
    }

    public Author(String id, String name, String biography, Integer birthYear, boolean archived, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.biography = biography != null ? biography : "";
        this.birthYear = birthYear;
        this.archived = archived;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(Integer birthYear) {
        this.birthYear = birthYear;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Author{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", birthYear=" + birthYear +
                ", archived=" + archived +
                '}';
    }
}
