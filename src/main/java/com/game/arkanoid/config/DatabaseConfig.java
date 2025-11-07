package com.game.arkanoid.config;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Singleton database configuration using environment variables from .env.
 */
public final class DatabaseConfig {

    private static Connection connection;

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = getEnv("URL");
            String username = getEnv("USERNAME");
            String password = getEnv("PASSWORD");

            Objects.requireNonNull(url, "Environment variable URL is required");
            Objects.requireNonNull(username, "Environment variable USERNAME is required");
            Objects.requireNonNull(password, "Environment variable PASSWORD is required");

            url = url.replaceFirst("^postgresql", "jdbc:postgresql");
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // cant import dotenv directly due to modularity issues; use reflection to load if present
    private static String getEnv(String key) {
        String val = System.getenv(key);
        if (val != null) return val;
        // Try to load via java-dotenv reflectively if present on the class/module path
        try {
            Class<?> dotenvClass = Class.forName("io.github.cdimascio.dotenv.Dotenv");
            try {
                // Preferred: Dotenv.load()
                Method load = dotenvClass.getMethod("load");
                Object dotenv = load.invoke(null);
                Method get = dotenvClass.getMethod("get", String.class);
                Object result = get.invoke(dotenv, key);
                return result != null ? result.toString() : null;
            } catch (NoSuchMethodException ignored) {
                // Fallback: Dotenv.configure().ignoreIfMissing().load()
                Method configure = dotenvClass.getMethod("configure");
                Object builder = configure.invoke(null);
                try {
                    Method ignoreIfMissing = builder.getClass().getMethod("ignoreIfMissing");
                    builder = ignoreIfMissing.invoke(builder);
                } catch (NoSuchMethodException ex) {
                    // ignore; optional
                }
                Method load = builder.getClass().getMethod("load");
                Object dotenv = load.invoke(builder);
                Method get = dotenvClass.getMethod("get", String.class);
                Object result = get.invoke(dotenv, key);
                return result != null ? result.toString() : null;
            }
        } catch (Throwable ignored) {
            return null;
        }
    }
}
