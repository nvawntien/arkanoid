package com.game.arkanoid.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class SqlLoader {
    private SqlLoader() {}

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

