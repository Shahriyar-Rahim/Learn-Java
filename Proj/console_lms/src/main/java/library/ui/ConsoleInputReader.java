package library.ui;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Resilient console input reader. Handles invalid numbers, whitespace, EOF, dates, and passwords.
 * Guarantees the application will never crash on malformed user input.
 */
public class ConsoleInputReader {

    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleInputReader(InputStream in) {
        this.scanner = new Scanner(in);
    }

    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        if (!scanner.hasNextLine()) {
            return null; // EOF
        }
        return scanner.nextLine().trim();
    }

    public String readNonEmptyString(String prompt, String fieldName) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) return null; // EOF
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  [Error] " + fieldName + " cannot be empty. Please try again.");
        }
    }

    public String readOptionalString(String prompt, String defaultValue) {
        String line = readLine(prompt);
        if (line == null) return defaultValue;
        return line.isEmpty() ? defaultValue : line;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) return -1; // EOF
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("  [Error] Value must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  [Error] Invalid number. Please enter an integer between " + min + " and " + max + ".");
            }
        }
    }

    public Integer readOptionalInt(String prompt, Integer defaultValue) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) return defaultValue;
            if (line.isEmpty()) return defaultValue;
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [Error] Invalid integer. Please enter a valid number or press Enter to skip.");
            }
        }
    }

    public LocalDate readDate(String prompt, boolean allowBlank) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) return null;
            if (line.isEmpty()) {
                if (allowBlank) return null;
                System.out.println("  [Error] Date cannot be empty (format: YYYY-MM-DD).");
                continue;
            }
            try {
                return LocalDate.parse(line, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  [Error] Invalid date format. Please use YYYY-MM-DD (e.g. 2024-05-15).");
            }
        }
    }

    public char[] readPassword(String prompt) {
        if (System.console() != null) {
            System.out.print(prompt);
            System.out.flush();
            char[] pwd = System.console().readPassword();
            return pwd != null ? pwd : new char[0];
        } else {
            String line = readLine(prompt);
            return line != null ? line.toCharArray() : new char[0];
        }
    }

    public boolean readConfirmation(String prompt) {
        String line = readLine(prompt + " (y/N): ");
        if (line == null) return false;
        return line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes");
    }

    public void waitForEnter() {
        System.out.print("\nPress [Enter] to continue...");
        System.out.flush();
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }
}
