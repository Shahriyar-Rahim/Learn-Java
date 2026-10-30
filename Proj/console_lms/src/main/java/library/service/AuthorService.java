package library.service;

import library.exception.DuplicateEntityException;
import library.exception.EntityNotFoundException;
import library.exception.ValidationException;
import library.model.Author;
import library.model.Book;
import library.repository.AuthorRepository;
import library.repository.BookRepository;
import library.validation.InputValidator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles author catalog operations and relations.
 */
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuditLogService auditLogService;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository, AuditLogService auditLogService) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.auditLogService = auditLogService;
    }

    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Author getAuthorById(String id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author", id));
    }

    public List<Author> searchAuthors(String keyword) {
        return authorRepository.searchByName(keyword);
    }

    public synchronized Author createAuthor(String name, String biography, Integer birthYear, String adminUsername) {
        InputValidator.validateNonEmpty(name, "Author Name");
        if (birthYear != null && (birthYear < 0 || birthYear > LocalDateTime.now().getYear())) {
            throw new ValidationException("Invalid author birth year: " + birthYear);
        }

        if (authorRepository.findByName(name).isPresent()) {
            throw new DuplicateEntityException("An author with name '" + name.trim() + "' already exists.");
        }

        String id = authorRepository.nextId();
        Author author = new Author(
                id,
                name.trim(),
                biography != null ? biography.trim() : "",
                birthYear,
                false,
                LocalDateTime.now()
        );

        authorRepository.save(author);
        auditLogService.log("AUTHOR_CREATED", adminUsername, "AUTHOR", "Created author " + author.getName() + " (" + id + ")", true);
        return author;
    }

    public synchronized void updateAuthor(String authorId, String name, String biography, Integer birthYear, String adminUsername) {
        Author author = getAuthorById(authorId);
        InputValidator.validateNonEmpty(name, "Author Name");
        if (birthYear != null && (birthYear < 0 || birthYear > LocalDateTime.now().getYear())) {
            throw new ValidationException("Invalid author birth year: " + birthYear);
        }

        authorRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(authorId)) {
                throw new DuplicateEntityException("Another author with name '" + name.trim() + "' already exists.");
            }
        });

        author.setName(name.trim());
        author.setBiography(biography != null ? biography.trim() : "");
        author.setBirthYear(birthYear);
        authorRepository.save(author);
        auditLogService.log("AUTHOR_UPDATED", adminUsername, "AUTHOR", "Updated author " + author.getName() + " (" + authorId + ")", true);
    }

    public synchronized void archiveAuthor(String authorId, String adminUsername) {
        Author author = getAuthorById(authorId);
        author.setArchived(true);
        authorRepository.save(author);
        auditLogService.log("AUTHOR_ARCHIVED", adminUsername, "AUTHOR", "Archived author " + author.getName() + " (" + authorId + ")", true);
    }

    public synchronized void restoreAuthor(String authorId, String adminUsername) {
        Author author = getAuthorById(authorId);
        author.setArchived(false);
        authorRepository.save(author);
        auditLogService.log("AUTHOR_RESTORED", adminUsername, "AUTHOR", "Restored author " + author.getName() + " (" + authorId + ")", true);
    }

    public synchronized void deleteAuthor(String authorId, String adminUsername) {
        Author author = getAuthorById(authorId);
        List<Book> books = bookRepository.findByAuthorId(authorId);
        if (!books.isEmpty()) {
            throw new ValidationException("Cannot delete author '" + author.getName() + "': " + books.size() + " book(s) in the catalogue still reference this author. Archive the author instead.");
        }
        authorRepository.delete(authorId);
        auditLogService.log("AUTHOR_DELETED", adminUsername, "AUTHOR", "Deleted author " + author.getName() + " (" + authorId + ")", true);
    }

    public List<Book> getBooksByAuthor(String authorId) {
        getAuthorById(authorId); // ensure author exists
        return bookRepository.findByAuthorId(authorId);
    }
}
