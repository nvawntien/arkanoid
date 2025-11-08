package com.game.arkanoid.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Minimal SHA-256 password hasher. Use username as salt.
 */
public final class PasswordHasher {

    private PasswordHasher() {}

    public static String hash(String username, String plain) {
        String salted = username + ":" + plain;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(salted.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String username, String plain, String expectedHash) {
        return hash(username, plain).equals(expectedHash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16))
              .append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}

