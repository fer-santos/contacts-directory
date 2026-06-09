package com.fer_santos.directory.models;

import com.fer_santos.directory.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("John", "Doe", "john@example.com", "1234");
    }

    @Test
    void addContact_IncrementsSize() {
        int initialSize = user.getContactCount();
        user.addContact("Jane", "Doe", "123456789", "jane@example.com", "Janey");
        assertEquals(initialSize + 1, user.getContactCount(), "Contact list size should increase by 1");
    }

    @Test
    void deleteContact_ValidIndex_DecrementsSizeAndReturnsTrue() {
        user.addContact("Jane", "Doe", "123456789", "jane@example.com", "Janey");
        int sizeBeforeDelete = user.getContactCount();
        
        boolean deleted = user.deleteContact(0);
        
        assertTrue(deleted, "deleteContact should return true for existing index");
        assertEquals(sizeBeforeDelete - 1, user.getContactCount(), "Contact list size should decrease by 1");
    }

    @Test
    void deleteContact_InvalidIndex_DoesNotChangeSizeAndReturnsFalse() {
        user.addContact("Jane", "Doe", "123456789", "jane@example.com", "Janey");
        int sizeBeforeDelete = user.getContactCount();
        
        boolean deleted = user.deleteContact(5);
        
        assertFalse(deleted, "deleteContact should return false for out of bounds index");
        assertEquals(sizeBeforeDelete, user.getContactCount(), "Contact list size should remain unchanged");
    }

    @Test
    void addContact_InvalidPhoneNumber_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            user.addContact("Jane", "Doe", "invalid-phone", "jane@example.com", "Janey");
        }, "Should throw IllegalArgumentException for non-numeric phone numbers");
    }

    @Test
    void createUser_InvalidData_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new User("", "Doe", "test@mail.com", "pass"), "Empty name should throw exception");
        assertThrows(IllegalArgumentException.class, () -> new User("John", "Doe", "invalid-email", "pass"), "Invalid email should throw exception");
        assertThrows(IllegalArgumentException.class, () -> new User("John", "Doe", "test@mail.com", ""), "Empty password should throw exception");
    }

    @Test
    void createUser_OptionalLastName_Works() {
        User userNoLast = new User("John", "", "john@test.com", "pass");
        assertEquals("", userNoLast.getLastName(), "LastName should be allowed to be empty");
    }
}
