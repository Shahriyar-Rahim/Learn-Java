package library.exception;

public class EntityNotFoundException extends LibraryException {
    public EntityNotFoundException(String entityType, String id) {
        super(entityType + " with ID '" + id + "' was not found.");
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
