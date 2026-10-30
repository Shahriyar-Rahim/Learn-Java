package library.exception;

public class InvalidStateTransitionException extends LibraryException {
    public InvalidStateTransitionException(String entity, String fromState, String toState) {
        super("Invalid state transition for " + entity + ": cannot change from " + fromState + " to " + toState + ".");
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
