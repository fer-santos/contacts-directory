package com.fer_santos.directory.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static String DB_URL = "jdbc:sqlite:contacts.db";

    public static void setDbUrl(String dbUrl) {
        DB_URL = dbUrl;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id TEXT PRIMARY KEY, " +
                    "firstName TEXT NOT NULL, " +
                    "lastName TEXT, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS contacts (" +
                    "id TEXT PRIMARY KEY, " +
                    "user_id TEXT NOT NULL, " +
                    "firstName TEXT NOT NULL, " +
                    "lastName TEXT, " +
                    "email TEXT, " +
                    "phoneNumber TEXT NOT NULL, " +
                    "alias TEXT, " +
                    "isFavorite INTEGER DEFAULT 0, " +
                    "isTrashed INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(user_id) REFERENCES users(id))");

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }
}
