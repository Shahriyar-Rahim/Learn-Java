package library.validation;

import library.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Validates domain and input data.
 */
public final class InputValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\s-]{7,20}$");
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:\\d{9}[\\dXx]|\\d{13}|(?:\\d{3}-)?\\d{1,5}-\\d+-\\d+-[\\dXx])$");

    private InputValidator() {
    }

    public static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username cannot be empty.");
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Username must be 3-30 characters long and contain only letters, numbers, or underscores.");
        }
    }

    public static void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (password.length > 128) {
            throw new ValidationException("Password is too long (maximum 128 characters).");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        if (!hasLetter || !hasDigit) {
            throw new ValidationException("Password must contain at least one letter and one number.");
        }
    }

    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email address cannot be empty.");
        }
        if (email.length() > 100) {
            throw new ValidationException("Email address is too long (maximum 100 characters).");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: '" + email + "'.");
        }
    }

    public static void validatePhone(String phone) {
        if (phone != null && !phone.isBlank()) {
            if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
                throw new ValidationException("Invalid phone number format: '" + phone + "'.");
            }
        }
    }

    public static void validateNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
        if (value.length() > 255) {
            throw new ValidationException(fieldName + " is too long (maximum 255 characters).");
        }
    }

    public static void validateISBN(String isbn) {
        if (isbn != null && !isbn.isBlank()) {
            String clean = isbn.trim().replaceAll("[-\\s]", "");
            if (clean.length() != 10 && clean.length() != 13) {
                throw new ValidationException("ISBN must have 10 or 13 digits (excluding hyphens).");
            }
            if (clean.length() == 10) {
                for (int i = 0; i < 9; i++) {
                    if (!Character.isDigit(clean.charAt(i))) {
                        throw new ValidationException("Invalid ISBN-10 format: '" + isbn + "'.");
                    }
                }
                char last = clean.charAt(9);
                if (!Character.isDigit(last) && last != 'X' && last != 'x') {
                    throw new ValidationException("Invalid ISBN-10 format: '" + isbn + "'.");
                }
            } else {
                for (int i = 0; i < 13; i++) {
                    if (!Character.isDigit(clean.charAt(i))) {
                        throw new ValidationException("Invalid ISBN-13 format: '" + isbn + "'.");
                    }
                }
            }
        }
    }

    public static void validatePositive(int number, String fieldName) {
        if (number <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than zero.");
        }
    }

    public static void validateNonNegative(int number, String fieldName) {
        if (number < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validatePublicationDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Publication date cannot be null.");
        }
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new ValidationException("Publication date cannot be in the future.");
        }
        if (date.isBefore(LocalDate.of(1000, 1, 1))) {
            throw new ValidationException("Publication date must be after year 1000.");
        }
    }
}
