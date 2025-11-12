package com.game.arkanoid.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for loading SQL files from the classpath.
 * <p>
 * Provides a simple method to read the contents of a SQL resource file into a String.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * String sql = SqlLoader.load("/sql/schema.sql");
 * </pre>
 * </p>
 */
public final class SqlLoader {

    /** Private constructor to prevent instantiation. */
    private SqlLoader() {}

    /**
     * Load the contents of a SQL file from the classpath.
     *
     * @param resourcePath the path to the SQL resource, starting with '/'
     * @return the full contents of the SQL file as a String
     * @throws IllegalArgumentException if the resource is not found
     * @throws RuntimeException if an I/O error occurs while reading the file
     */
    public static String load(String resourcePath) {
        InputStream in = SqlLoader.class.getResourceAsStream(resourcePath);
        if (in == null) throw new IllegalArgumentException("Missing SQL resource: " + resourcePath);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL: " + resourcePath, e);
        }
    }
}
