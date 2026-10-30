package library.service;

import library.exception.DuplicateEntityException;
import library.exception.EntityNotFoundException;
import library.exception.ValidationException;
import library.model.Book;
import library.model.CatalogueSection;
import library.repository.BookRepository;
import library.repository.SectionRepository;
import library.validation.InputValidator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles catalogue sections/categories management and integrity checks.
 */
public class CatalogueService {

    private final SectionRepository sectionRepository;
    private final BookRepository bookRepository;
    private final AuditLogService auditLogService;

    public CatalogueService(SectionRepository sectionRepository, BookRepository bookRepository, AuditLogService auditLogService) {
        this.sectionRepository = sectionRepository;
        this.bookRepository = bookRepository;
        this.auditLogService = auditLogService;
    }

    public List<CatalogueSection> getAllSections() {
        return sectionRepository.findAll();
    }

    public CatalogueSection getSectionById(String id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CatalogueSection", id));
    }

    public synchronized CatalogueSection createSection(String name, String description, String adminUsername) {
        InputValidator.validateNonEmpty(name, "Section Name");
        if (sectionRepository.findByName(name).isPresent()) {
            throw new DuplicateEntityException("Catalogue section '" + name.trim() + "' already exists.");
        }

        String id = sectionRepository.nextId();
        CatalogueSection section = new CatalogueSection(
                id,
                name.trim(),
                description != null ? description.trim() : "",
                false,
                LocalDateTime.now()
        );

        sectionRepository.save(section);
        auditLogService.log("SECTION_CREATED", adminUsername, "CATALOGUE", "Created section " + section.getName() + " (" + id + ")", true);
        return section;
    }

    public synchronized void updateSection(String sectionId, String name, String description, String adminUsername) {
        CatalogueSection section = getSectionById(sectionId);
        InputValidator.validateNonEmpty(name, "Section Name");

        sectionRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(sectionId)) {
                throw new DuplicateEntityException("Another catalogue section with name '" + name.trim() + "' already exists.");
            }
        });

        section.setName(name.trim());
        section.setDescription(description != null ? description.trim() : "");
        sectionRepository.save(section);
        auditLogService.log("SECTION_UPDATED", adminUsername, "CATALOGUE", "Updated section " + section.getName() + " (" + sectionId + ")", true);
    }

    public synchronized void archiveSection(String sectionId, String adminUsername) {
        CatalogueSection section = getSectionById(sectionId);
        section.setArchived(true);
        sectionRepository.save(section);
        auditLogService.log("SECTION_ARCHIVED", adminUsername, "CATALOGUE", "Archived section " + section.getName() + " (" + sectionId + ")", true);
    }

    public synchronized void restoreSection(String sectionId, String adminUsername) {
        CatalogueSection section = getSectionById(sectionId);
        section.setArchived(false);
        sectionRepository.save(section);
        auditLogService.log("SECTION_RESTORED", adminUsername, "CATALOGUE", "Restored section " + section.getName() + " (" + sectionId + ")", true);
    }

    public synchronized void deleteSection(String sectionId, String adminUsername) {
        CatalogueSection section = getSectionById(sectionId);
        List<Book> books = bookRepository.findBySectionId(sectionId);
        if (!books.isEmpty()) {
            throw new ValidationException("Cannot delete section '" + section.getName() + "': " + books.size() + " book(s) in the catalogue belong to this section. Reassign or archive the books first.");
        }
        sectionRepository.delete(sectionId);
        auditLogService.log("SECTION_DELETED", adminUsername, "CATALOGUE", "Deleted section " + section.getName() + " (" + sectionId + ")", true);
    }

    public List<Book> getBooksBySection(String sectionId) {
        getSectionById(sectionId); // ensure section exists
        return bookRepository.findBySectionId(sectionId);
    }

    public List<CatalogueSection> searchSections(String query) {
        if (query == null || query.isBlank()) {
            return getAllSections();
        }
        String q = query.trim().toLowerCase();
        List<CatalogueSection> results = new java.util.ArrayList<>();
        for (CatalogueSection s : sectionRepository.findAll()) {
            if (s.getName().toLowerCase().contains(q) || s.getDescription().toLowerCase().contains(q) || s.getId().toLowerCase().contains(q)) {
                results.add(s);
            }
        }
        return results;
    }
}
