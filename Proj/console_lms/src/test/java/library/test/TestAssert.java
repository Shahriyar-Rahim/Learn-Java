package library.test;

import java.util.Objects;

/**
 * Lightweight, zero-dependency testing assertion framework.
 */
public final class TestAssert {

    private TestAssert() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: Expected TRUE, got FALSE. " + message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError("Assertion Failed: Expected FALSE, got TRUE. " + message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("Assertion Failed: Expected <" + expected + ">, but got <" + actual + ">. " + message);
        }
    }

    public static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Assertion Failed: Expected non-null object. " + message);
        }
    }

    public static void assertNull(Object obj, String message) {
        if (obj != null) {
            throw new AssertionError("Assertion Failed: Expected null, but got <" + obj + ">. " + message);
        }
    }

    @FunctionalInterface
    public interface Executable {
        void execute() throws Throwable;
    }

    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
        try {
            executable.execute();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return expectedType.cast(actual);
            }
            throw new AssertionError("Assertion Failed: Expected exception " + expectedType.getName() + " but caught " + actual.getClass().getName() + ": " + actual.getMessage() + ". " + message);
        }
        throw new AssertionError("Assertion Failed: Expected exception " + expectedType.getName() + " was not thrown. " + message);
    }
}
