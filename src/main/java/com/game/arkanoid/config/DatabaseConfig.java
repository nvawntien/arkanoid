package com.game.arkanoid.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Simplified DatabaseConfig
 * - Reads full JDBC URL from .env.
 */
public final class DatabaseConfig {

    private static Connection connection;
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {

            String url = getEnv("URL");
            Objects.requireNonNull(url, "Missing environment variable: URL");

            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC driver not found in classpath", e);
            }

            System.out.println("🔗 Connecting to: " + url);
            connection = DriverManager.getConnection(url);
            System.out.println("✅ Connected successfully to database");
        }
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getEnv(String key) {
        String val = System.getenv(key);
        if (val == null) val = dotenv.get(key);
        return val;
    }
}
