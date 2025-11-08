package com.game.arkanoid;

import com.game.arkanoid.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple connection test to verify DatabaseConfig + .env setup.
 */
public class DatabaseConnectionTest {

    public static void main(String[] args) {
        System.out.println("🔍 Checking database connection...");

        System.out.println("🧩 Driver version: " +
            org.postgresql.Driver.class.getPackage().getImplementationVersion());

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ Connected successfully to database!");
            System.out.println("🔹 URL: " + conn.getMetaData().getURL());
            System.out.println("🔹 User: " + conn.getMetaData().getUserName());

            // test query
            ResultSet rs = stmt.executeQuery("SELECT NOW()");
            if (rs.next()) {
                System.out.println("🕒 Server time: " + rs.getString(1));
            }

            rs.close();
        } catch (Exception e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }
}
