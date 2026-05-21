package test.java.com.fer_santos.directory;

import com.fer_santos.directory.Main;
import com.fer_santos.directory.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private ArrayList<User> usersList;

    @BeforeEach
    void setUp() {
        usersList = new ArrayList<>();
        usersList.add(new User("Test", "User", "test@example.com", "password123"));
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsUser() {
        User result = Main.authenticateUser("test@example.com", "password123", usersList);
        assertNotNull(result, "Should return the user for correct credentials");
        assertEquals("Test", result.getName());
    }

    @Test
    void authenticateUser_InvalidPassword_ReturnsNull() {
        User result = Main.authenticateUser("test@example.com", "wrongpassword", usersList);
        assertNull(result, "Should return null for incorrect password");
    }

    @Test
    void authenticateUser_NonExistentEmail_ReturnsNull() {
        User result = Main.authenticateUser("nonexistent@example.com", "password123", usersList);
        assertNull(result, "Should return null for non-existent email");
    }

    @Test
    void isEmailRegistered_ExistingEmail_ReturnsTrue() {
        assertTrue(Main.isEmailRegistered("test@example.com", usersList), "Should return true for existing email");
    }

    @Test
    void isEmailRegistered_ExistingEmailDifferentCase_ReturnsTrue() {
        assertTrue(Main.isEmailRegistered("TEST@example.com", usersList), "Should return true for existing email with different case");
    }

    @Test
    void isEmailRegistered_NewEmail_ReturnsFalse() {
        assertFalse(Main.isEmailRegistered("new@example.com", usersList), "Should return false for a new email");
    }
}
