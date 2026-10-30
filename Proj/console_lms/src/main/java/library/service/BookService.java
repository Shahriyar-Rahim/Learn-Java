package library.service;

import library.exception.DuplicateEntityException;
import library.exception.EntityNotFoundException;
import library.exception.ValidationException;
import library.model.*;
import library.repository.*;
import library.validation.InputValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Handles books catalogue management, copies inventory, and rich search/filtering.
 */
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final AuthorRepository authorRepository;
    private final SectionRepository sectionRepository;
    private final BorrowRepository borrowRepository;
    private final AuditLogService auditLogService;

    public BookService(BookRepository bookRepository, BookCopyRepository copyRepository,
                       AuthorRepository authorRepository, SectionRepository sectionRepository,
                       BorrowRepository borrowRepository, AuditLogService auditLogService) {
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.authorRepository = authorRepository;
        this.sectionRepository = sectionRepository;
        this.borrowRepository = borrowRepository;
        this.auditLogService = auditLogService;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book", id));
    }

    public BookDetail getBookDetail(String bookId) {
        Book book = getBookById(bookId);
        List<Author> authors = new ArrayList<>();
        for (String aid : book.getAuthorIds()) {
            authorRepository.findById(aid).ifPresent(authors::add);
        }
        CatalogueSection section = sectionRepository.findById(book.getSectionId()).orElse(null);
        List<BookCopy> copies = copyRepository.findByBookId(bookId);
        return new BookDetail(book, authors, section, copies);
    }

    public List<BookDetail> getAllBookDetails() {
        List<BookDetail> list = new ArrayList<>();
        for (Book b : bookRepository.findAll()) {
            list.add(getBookDetail(b.getId()));
        }
        return list;
    }

    public synchronized Book createBook(String isbn, String title, String description,
                                       List<String> authorIds, String sectionId,
                                       String publisher, LocalDate publicationDate,
                                       String language, String edition,
                                       int initialCopies, String initialLocation,
                                       BookCondition initialCondition,
                                       String adminUsername) {
        InputValidator.validateNonEmpty(title, "Book Title");
        InputValidator.validateISBN(isbn);
        if (publicationDate != null) {
            InputValidator.validatePublicationDate(publicationDate);
        }

        if (isbn != null && !isbn.isBlank() && bookRepository.existsByIsbn(isbn)) {
            throw new DuplicateEntityException("A book with ISBN '" + isbn + "' already exists.");
        }

        // Validate section if provided
        if (sectionId != null && !sectionId.isBlank()) {
            if (sectionRepository.findById(sectionId).isEmpty()) {
                throw new EntityNotFoundException("CatalogueSection", sectionId);
            }
        }

        // Validate authors if provided
        if (authorIds != null) {
            for (String aid : authorIds) {
                if (authorRepository.findById(aid).isEmpty()) {
                    throw new EntityNotFoundException("Author", aid);
                }
            }
        }

        String bookId = bookRepository.nextId();
        Book book = new Book(
                bookId,
                isbn != null ? isbn.trim() : "",
                title.trim(),
                description != null ? description.trim() : "",
                authorIds,
                sectionId != null ? sectionId.trim() : "",
                publisher != null ? publisher.trim() : "",
                publicationDate,
                language != null && !language.isBlank() ? language.trim() : "English",
                edition != null && !edition.isBlank() ? edition.trim() : "1st",
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        bookRepository.save(book);

        // Add initial copies if specified
        if (initialCopies > 0) {
            addCopies(bookId, initialCopies, initialLocation, initialCondition, adminUsername);
        }

        auditLogService.log("BOOK_CREATED", adminUsername, "BOOK", "Created book '" + book.getTitle() + "' (" + bookId + ") with " + initialCopies + " copies", true);
        return book;
    }

    public synchronized void updateBook(String bookId, String isbn, String title, String description,
                                       List<String> authorIds, String sectionId,
                                       String publisher, LocalDate publicationDate,
                                       String language, String edition,
                                       String adminUsername) {
        Book book = getBookById(bookId);
        InputValidator.validateNonEmpty(title, "Book Title");
        InputValidator.validateISBN(isbn);
        if (publicationDate != null) {
            InputValidator.validatePublicationDate(publicationDate);
        }

        if (isbn != null && !isbn.isBlank()) {
            bookRepository.findByIsbn(isbn).ifPresent(existing -> {
                if (!existing.getId().equals(bookId)) {
                    throw new DuplicateEntityException("Another book with ISBN '" + isbn + "' already exists.");
                }
            });
        }

        if (sectionId != null && !sectionId.isBlank()) {
            if (sectionRepository.findById(sectionId).isEmpty()) {
                throw new EntityNotFoundException("CatalogueSection", sectionId);
            }
        }

        if (authorIds != null) {
            for (String aid : authorIds) {
                if (authorRepository.findById(aid).isEmpty()) {
                    throw new EntityNotFoundException("Author", aid);
                }
            }
        }

        book.setIsbn(isbn != null ? isbn.trim() : "");
        book.setTitle(title.trim());
        book.setDescription(description != null ? description.trim() : "");
        book.setAuthorIds(authorIds);
        book.setSectionId(sectionId != null ? sectionId.trim() : "");
        book.setPublisher(publisher != null ? publisher.trim() : "");
        book.setPublicationDate(publicationDate);
        book.setLanguage(language != null && !language.isBlank() ? language.trim() : "English");
        book.setEdition(edition != null && !edition.isBlank() ? edition.trim() : "1st");

        bookRepository.save(book);
        auditLogService.log("BOOK_UPDATED", adminUsername, "BOOK", "Updated book '" + book.getTitle() + "' (" + bookId + ")", true);
    }

    public synchronized void archiveBook(String bookId, String adminUsername) {
        Book book = getBookById(bookId);
        book.setArchived(true);
        bookRepository.save(book);
        auditLogService.log("BOOK_ARCHIVED", adminUsername, "BOOK", "Archived book '" + book.getTitle() + "' (" + bookId + ")", true);
    }

    public synchronized void restoreBook(String bookId, String adminUsername) {
        Book book = getBookById(bookId);
        book.setArchived(false);
        bookRepository.save(book);
        auditLogService.log("BOOK_RESTORED", adminUsername, "BOOK", "Restored book '" + book.getTitle() + "' (" + bookId + ")", true);
    }

    public synchronized void deleteBook(String bookId, String adminUsername) {
        Book book = getBookById(bookId);
        List<BookCopy> copies = copyRepository.findByBookId(bookId);
        for (BookCopy c : copies) {
            if (borrowRepository.findActiveByCopyId(c.getCopyId()).isPresent()) {
                throw new ValidationException("Cannot delete book '" + book.getTitle() + "': Active loans exist for its copies.");
            }
        }
        if (!borrowRepository.findActiveByBookId(bookId).isEmpty()) {
            throw new ValidationException("Cannot delete book '" + book.getTitle() + "': Active loans exist. Archive instead.");
        }
        // Remove copies
        for (BookCopy c : copies) {
            copyRepository.delete(c.getCopyId());
        }
        bookRepository.delete(bookId);
        auditLogService.log("BOOK_DELETED", adminUsername, "BOOK", "Permanently deleted book '" + book.getTitle() + "' (" + bookId + ")", true);
    }

    // -------------------------------------------------------------
    // COPIES INVENTORY
    // -------------------------------------------------------------
    public synchronized List<BookCopy> addCopies(String bookId, int count, String location,
                                                 BookCondition condition, String adminUsername) {
        getBookById(bookId); // verify exists
        InputValidator.validatePositive(count, "Copy count");

        List<BookCopy> added = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String copyId = copyRepository.nextCopyId(bookId);
            BookCopy copy = new BookCopy(
                    copyId,
                    bookId,
                    CopyStatus.AVAILABLE,
                    location != null && !location.isBlank() ? location.trim() : "Main Stacks",
                    condition != null ? condition : BookCondition.GOOD,
                    LocalDate.now(),
                    ""
            );
            copyRepository.save(copy);
            added.add(copy);
        }

        auditLogService.log("COPIES_ADDED", adminUsername, "BOOK_COPY", "Added " + count + " copies to book " + bookId, true);
        return added;
    }

    public synchronized void updateCopyStatus(String copyId, CopyStatus status, BookCondition condition,
                                             String location, String notes, String adminUsername) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("BookCopy", copyId));

        if (copy.getStatus() == CopyStatus.BORROWED && status != CopyStatus.BORROWED) {
            if (borrowRepository.findActiveByCopyId(copyId).isPresent()) {
                throw new ValidationException("Cannot change status of copy " + copyId + " while it is actively borrowed. Return the book first.");
            }
        }

        if (status != null) copy.setStatus(status);
        if (condition != null) copy.setCondition(condition);
        if (location != null && !location.isBlank()) copy.setLocation(location.trim());
        if (notes != null) copy.setNotes(notes.trim());

        copyRepository.save(copy);
        auditLogService.log("COPY_UPDATED", adminUsername, "BOOK_COPY", "Updated copy " + copyId + " to status " + copy.getStatus(), true);
    }

    public synchronized void removeCopy(String copyId, String adminUsername) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("BookCopy", copyId));

        if (borrowRepository.findActiveByCopyId(copyId).isPresent()) {
            throw new ValidationException("Cannot remove copy " + copyId + " because it is currently checked out.");
        }

        copyRepository.delete(copyId);
        auditLogService.log("COPY_REMOVED", adminUsername, "BOOK_COPY", "Removed copy " + copyId + " from book " + copy.getBookId(), true);
    }

    public List<BookCopy> getCopiesForBook(String bookId) {
        return copyRepository.findByBookId(bookId);
    }

    public record ShelfSummary(String shelfName, int distinctBooks, int totalCopies, int availableCopies) {}
    public record BookOnShelf(Book book, int totalOnShelf, int availableOnShelf) {}

    public List<String> getAllShelves() {
        Set<String> shelves = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (BookCopy c : copyRepository.findAll()) {
            if (c.getLocation() != null && !c.getLocation().isBlank()) {
                shelves.add(c.getLocation().trim());
            }
        }
        return new ArrayList<>(shelves);
    }

    public List<ShelfSummary> getShelvesSummary() {
        Map<String, Set<String>> booksByShelf = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Integer> copiesByShelf = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Integer> availByShelf = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (BookCopy c : copyRepository.findAll()) {
            String loc = (c.getLocation() != null && !c.getLocation().isBlank()) ? c.getLocation().trim() : "Main Stacks";
            booksByShelf.computeIfAbsent(loc, k -> new HashSet<>()).add(c.getBookId());
            copiesByShelf.merge(loc, 1, Integer::sum);
            if (c.getStatus() == CopyStatus.AVAILABLE) {
                availByShelf.merge(loc, 1, Integer::sum);
            }
        }

        List<ShelfSummary> list = new ArrayList<>();
        for (String shelf : booksByShelf.keySet()) {
            list.add(new ShelfSummary(
                shelf, 
                booksByShelf.get(shelf).size(), 
                copiesByShelf.getOrDefault(shelf, 0),
                availByShelf.getOrDefault(shelf, 0)
            ));
        }
        return list;
    }

    public List<BookOnShelf> getBooksOnShelf(String shelfLocation) {
        if (shelfLocation == null || shelfLocation.isBlank()) {
            return Collections.emptyList();
        }
        String target = shelfLocation.trim().toLowerCase();
        Map<String, int[]> countsByBookId = new LinkedHashMap<>();
        for (BookCopy c : copyRepository.findAll()) {
            if (c.getLocation() != null && c.getLocation().trim().equalsIgnoreCase(target)) {
                int[] counts = countsByBookId.computeIfAbsent(c.getBookId(), k -> new int[2]);
                counts[0]++;
                if (c.getStatus() == CopyStatus.AVAILABLE) {
                    counts[1]++;
                }
            }
        }
        List<BookOnShelf> result = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : countsByBookId.entrySet()) {
            bookRepository.findById(entry.getKey()).ifPresent(b -> {
                result.add(new BookOnShelf(b, entry.getValue()[0], entry.getValue()[1]));
            });
        }
        return result;
    }

    public BookCopy getCopyById(String copyId) {
        return copyRepository.findById(copyId)
                .orElseThrow(() -> new EntityNotFoundException("BookCopy", copyId));
    }

    public synchronized void moveBookToSection(String bookId, String newSectionId, String adminUsername) {
        Book book = getBookById(bookId);
        if (newSectionId != null && !newSectionId.isBlank()) {
            if (sectionRepository.findById(newSectionId).isEmpty()) {
                throw new EntityNotFoundException("CatalogueSection", newSectionId);
            }
            book.setSectionId(newSectionId.trim());
        } else {
            book.setSectionId("");
        }
        bookRepository.save(book);
        auditLogService.log("BOOK_SECTION_CHANGED", adminUsername, "BOOK", 
                "Moved book '" + book.getTitle() + "' (" + bookId + ") to section " + (newSectionId != null ? newSectionId : "None"), true);
    }

    public Map<String, Integer> getInventorySummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        List<BookCopy> copies = copyRepository.findAll();
        int available = 0, borrowed = 0, reserved = 0, lost = 0, damaged = 0, archived = 0;
        for (BookCopy c : copies) {
            switch (c.getStatus()) {
                case AVAILABLE -> available++;
                case BORROWED -> borrowed++;
                case RESERVED -> reserved++;
                case LOST -> lost++;
                case DAMAGED -> damaged++;
                case ARCHIVED -> archived++;
            }
        }
        summary.put("TOTAL_BOOKS", bookRepository.findAll().size());
        summary.put("TOTAL_COPIES", copies.size());
        summary.put("AVAILABLE", available);
        summary.put("BORROWED", borrowed);
        summary.put("RESERVED", reserved);
        summary.put("LOST", lost);
        summary.put("DAMAGED", damaged);
        summary.put("ARCHIVED", archived);
        return summary;
    }

    // -------------------------------------------------------------
    // SEARCH & FILTERING
    // -------------------------------------------------------------
    public List<BookDetail> searchBooks(String query, String sectionId, String authorId,
                                        Integer pubYear, Boolean onlyAvailable, BookSort sort) {
        return searchBooks(query, sectionId, authorId, pubYear, null, null, onlyAvailable, sort);
    }

    public List<BookDetail> searchBooks(String query, String sectionId, String authorId,
                                        Integer pubYear, LocalDate fromDate, LocalDate toDate,
                                        Boolean onlyAvailable, BookSort sort) {
        List<BookDetail> results = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (Book book : bookRepository.findAll()) {
            // Section filter
            if (sectionId != null && !sectionId.isBlank() && !sectionId.equals(book.getSectionId())) {
                continue;
            }

            // Author filter
            if (authorId != null && !authorId.isBlank() && !book.getAuthorIds().contains(authorId)) {
                continue;
            }

            // Publication year filter
            if (pubYear != null) {
                if (book.getPublicationDate() == null || book.getPublicationDate().getYear() != pubYear) {
                    continue;
                }
            }

            // Publication date range filter
            if (fromDate != null || toDate != null) {
                LocalDate pDate = book.getPublicationDate();
                if (pDate == null) continue;
                if (fromDate != null && pDate.isBefore(fromDate)) continue;
                if (toDate != null && pDate.isAfter(toDate)) continue;
            }

            BookDetail detail = getBookDetail(book.getId());

            // Availability filter
            if (Boolean.TRUE.equals(onlyAvailable) && detail.getAvailableCopies() == 0) {
                continue;
            }

            // Query keyword search
            if (!q.isEmpty()) {
                boolean match = book.getTitle().toLowerCase().contains(q) ||
                                book.getIsbn().toLowerCase().contains(q) ||
                                book.getId().toLowerCase().contains(q) ||
                                book.getDescription().toLowerCase().contains(q) ||
                                book.getPublisher().toLowerCase().contains(q) ||
                                detail.getAuthorNames().toLowerCase().contains(q) ||
                                detail.getSectionName().toLowerCase().contains(q);
                if (!match) continue;
            }

            results.add(detail);
        }

        // Sorting
        if (sort != null) {
            switch (sort) {
                case TITLE_ASC -> results.sort(Comparator.comparing(d -> d.getBook().getTitle(), String.CASE_INSENSITIVE_ORDER));
                case TITLE_DESC -> results.sort(Comparator.comparing((BookDetail d) -> d.getBook().getTitle(), String.CASE_INSENSITIVE_ORDER).reversed());
                case DATE_NEWEST -> results.sort((a, b) -> {
                    LocalDate da = a.getBook().getPublicationDate();
                    LocalDate db = b.getBook().getPublicationDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return db.compareTo(da);
                });
                case DATE_OLDEST -> results.sort((a, b) -> {
                    LocalDate da = a.getBook().getPublicationDate();
                    LocalDate db = b.getBook().getPublicationDate();
                    if (da == null && db == null) return 0;
                    if (da == null) return 1;
                    if (db == null) return -1;
                    return da.compareTo(db);
                });
                case AUTHOR -> results.sort(Comparator.comparing(BookDetail::getAuthorNames, String.CASE_INSENSITIVE_ORDER));
                case SECTION -> results.sort(Comparator.comparing(BookDetail::getSectionName, String.CASE_INSENSITIVE_ORDER));
                case AVAILABILITY -> results.sort(Comparator.comparingInt(BookDetail::getAvailableCopies).reversed());
            }
        }

        return results;
    }
}
