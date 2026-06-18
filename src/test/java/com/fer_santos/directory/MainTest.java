package com.fer_santos.directory;

import com.fer_santos.directory.controllers.AuthController;
import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.DatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @BeforeEach
    void setUp() throws SQLException {
        DatabaseManager.setDbUrl("jdbc:sqlite:test_contacts.db");
        DatabaseManager.initialize();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
             stmt.execute("DELETE FROM users");
             stmt.execute("DELETE FROM contacts");
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (id, firstName, lastName, email, password) VALUES (?, ?, ?, ?, ?)")) {
            User testUser = new User("Test", "User", "test@example.com", "password123");
            pstmt.setString(1, testUser.getId());
            pstmt.setString(2, testUser.getName());
            pstmt.setString(3, testUser.getLastName());
            pstmt.setString(4, testUser.getEmail());
            pstmt.setString(5, testUser.getPassword());
            pstmt.executeUpdate();
        }
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUser() {
        User result = AuthController.authenticateUser("test@example.com", "password123");
        assertNotNull(result, "Should return the user for correct credentials");
        assertEquals("Test", result.getName());
    }

    @Test
    void authenticateUser_InvalidPassword_ReturnsNull() {
        User result = AuthController.authenticateUser("test@example.com", "wrongpassword");
        assertNull(result, "Should return null for incorrect password");
    }

    @Test
    void authenticateUser_NonExistentEmail_ReturnsNull() {
        User result = AuthController.authenticateUser("nonexistent@example.com", "password123");
        assertNull(result, "Should return null for non-existent email");
    }

    @Test
    void isEmailRegistered_ExistingEmail_ReturnsTrue() {
        assertTrue(AuthController.isEmailRegistered("test@example.com"), "Should return true for existing email");
    }

    @Test
    void isEmailRegistered_ExistingEmailDifferentCase_ReturnsTrue() {
        // SQLite case sensitivity might make this fail if not explicitly handled, 
        // but let's test it according to our previous implementation or adjust.
        // Usually, in SQLite we'd use COLLATE NOCASE on the column or in the query.
        // If it fails, we will adapt the query in AuthController.
        assertTrue(AuthController.isEmailRegistered("TEST@example.com"), "Should return true for existing email with different case");
    }

    @Test
    void isEmailRegistered_NewEmail_ReturnsFalse() {
        assertFalse(AuthController.isEmailRegistered("new@example.com"), "Should return false for a new email");
    }
}
