package com.game.arkanoid.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Minimal SHA-256 password hasher for the Arkanoid game.
 * <p>
 * Provides hashing and verification methods using a normalized username as salt.
 * The username is normalized to lowercase and trimmed before hashing.
 * </p>
 * <p>
 * Usage:
 * <ul>
 *     <li>Call {@link #hash(String, String)} to generate a password hash.</li>
 *     <li>Call {@link #verify(String, String, String)} to check a password against a stored hash.</li>
 * </ul>
 * </p>
 */
public final class PasswordHasher {

    /** Private constructor to prevent instantiation. */
    private PasswordHasher() {}

    /**
     * Normalize a username to a canonical form.
     * <p>
     * Trims whitespace and converts to lowercase using {@link Locale#ROOT}.
     * This is used as part of the salt for password hashing.
     * </p>
     *
     * @param username the original username
     * @return the normalized username
     */
    public static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Generate a SHA-256 hash of the password, salted with the normalized username.
     *
     * @param username the username to use as salt
     * @param plain the plaintext password
     * @return the hexadecimal string representation of the SHA-256 hash
     * @throws IllegalStateException if SHA-256 algorithm is not available
     */
    public static String hash(String username, String plain) {
        String salted = normalize(username) + ":" + (plain == null ? "" : plain);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Verify a plaintext password against an expected SHA-256 hash.
     *
     * @param username the username used as salt
     * @param plain the plaintext password
     * @param expectedHash the expected hash value
     * @return true if the password matches the expected hash, false otherwise
     */
    public static boolean verify(String username, String plain, String expectedHash) {
        return hash(username, plain).equals(expectedHash);
    }

    /**
     * Convert a byte array to a lowercase hexadecimal string.
     *
     * @param bytes the byte array to convert
     * @return the hexadecimal string
     */
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16))
              .append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}
