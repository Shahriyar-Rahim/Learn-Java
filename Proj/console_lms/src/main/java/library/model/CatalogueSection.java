package library.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a classification section / category in the library catalogue.
 */
public class CatalogueSection {
    private String id;
    private String name;
    private String description;
    private boolean archived;
    private LocalDateTime createdAt;

    public CatalogueSection() {
    }

    public CatalogueSection(String id, String name, String description, boolean archived, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        CatalogueSection that = (CatalogueSection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CatalogueSection{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", archived=" + archived +
                '}';
    }
}
