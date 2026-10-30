package library.exception;

/**
 * Base exception for all domain and operational exceptions in the Library Management System.
 */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
