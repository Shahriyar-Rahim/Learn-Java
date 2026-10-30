package library.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite view of a Book with its associated Authors, Section, and Copies.
 */
public class BookDetail {
    private final Book book;
    private final List<Author> authors;
    private final CatalogueSection section;
    private final List<BookCopy> copies;

    public BookDetail(Book book, List<Author> authors, CatalogueSection section, List<BookCopy> copies) {
        this.book = book;
        this.authors = authors != null ? authors : new ArrayList<>();
        this.section = section;
        this.copies = copies != null ? copies : new ArrayList<>();
    }

    public Book getBook() {
        return book;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public CatalogueSection getSection() {
        return section;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public int getTotalCopies() {
        return copies.size();
    }

    public int getAvailableCopies() {
        int count = 0;
        for (BookCopy c : copies) {
            if (c.getStatus() == CopyStatus.AVAILABLE) count++;
        }
        return count;
    }

    public int getBorrowedCopies() {
        int count = 0;
        for (BookCopy c : copies) {
            if (c.getStatus() == CopyStatus.BORROWED) count++;
        }
        return count;
    }

    public int getReservedCopies() {
        int count = 0;
        for (BookCopy c : copies) {
            if (c.getStatus() == CopyStatus.RESERVED) count++;
        }
        return count;
    }

    public String getAuthorNames() {
        if (authors.isEmpty()) return "Unknown Author";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            sb.append(authors.get(i).getName());
            if (i < authors.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public String getSectionName() {
        return section != null ? section.getName() : "Unassigned";
    }
}
