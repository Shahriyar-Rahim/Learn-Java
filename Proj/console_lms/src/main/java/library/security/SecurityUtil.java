package library.security;

import library.exception.ValidationException;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Security and sanitization utilities.
 */
public final class SecurityUtil {

    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private SecurityUtil() {
    }

    /**
     * Sanitizes a file name, disallowing directory separators, '..', null bytes, and special characters.
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ValidationException("Filename cannot be blank.");
        }
        String clean = filename.trim();
        if (clean.contains("..") || clean.contains("/") || clean.contains("\\") || clean.contains("\0")) {
            throw new ValidationException("Filename contains illegal path traversal characters.");
        }
        if (!SAFE_FILENAME_PATTERN.matcher(clean).matches()) {
            throw new ValidationException("Filename contains invalid characters: '" + filename + "'.");
        }
        return clean;
    }

    /**
     * Resolves a filename against a base directory and verifies that the resulting path
     * stays strictly within the target base directory.
     */
    public static Path resolveSafePath(Path baseDir, String filename) {
        String sanitized = sanitizeFilename(filename);
        Path resolved = baseDir.resolve(sanitized).normalize();
        if (!resolved.startsWith(baseDir.normalize())) {
            throw new SecurityException("Path traversal attempt detected: " + filename);
        }
        return resolved;
    }

    /**
     * Checks an admin registration secret if configured in the environment.
     * If LMS_ADMIN_SECRET is set, the provided secret must match it.
     * If not set, returns true (or requires confirmation password).
     */
    public static boolean verifyAdminSetupSecret(String providedSecret) {
        String requiredSecret = System.getenv("LMS_ADMIN_SECRET");
        if (requiredSecret == null || requiredSecret.isBlank()) {
            // No env var set, check provided secret is non-empty if prompt requested
            return providedSecret != null && !providedSecret.isBlank();
        }
        return requiredSecret.equals(providedSecret);
    }
}
