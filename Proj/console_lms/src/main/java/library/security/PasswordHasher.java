package library.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Robust cryptographic password hashing using PBKDF2WithHmacSHA256.
 * Zero external libraries required.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 65536;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32; // 256 bits
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    /**
     * Hashes a plaintext password using a cryptographically secure random salt and PBKDF2.
     * Overwrites the provided char array with zeroes after hashing for security.
     */
    public static String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        try {
            byte[] salt = new byte[SALT_BYTES];
            SECURE_RANDOM.nextBytes(salt);

            byte[] hash = pbkdf2(password, salt, DEFAULT_ITERATIONS, HASH_BYTES);

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);

            return "PBKDF2$" + DEFAULT_ITERATIONS + "$" + saltB64 + "$" + hashB64;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Cryptographic algorithm unavailable for password hashing", e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /**
     * Verifies a plaintext password against a stored formatted PBKDF2 hash.
     * Uses constant-time comparison to prevent timing attacks.
     * Overwrites the provided char array with zeroes after verification.
     */
    public static boolean verify(char[] password, String storedHash) {
        if (password == null || password.length == 0 || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4 || !parts[0].equals("PBKDF2")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(password, salt, iterations, expectedHash.length);

            return MessageDigest.isEqual(actualHash, expectedHash);
        } catch (Exception e) {
            return false;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int outputBytes) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, outputBytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
}
