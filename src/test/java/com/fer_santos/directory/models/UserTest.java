package test.java.com.fer_santos.directory.models;

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
    void deleteContact_ValidAlias_DecrementsSizeAndReturnsTrue() {
        user.addContact("Jane", "Doe", "123456789", "jane@example.com", "Janey");
        int sizeBeforeDelete = user.getContactCount();
        
        boolean deleted = user.deleteContact("Janey");
        
        assertTrue(deleted, "deleteContact should return true for existing alias");
        assertEquals(sizeBeforeDelete - 1, user.getContactCount(), "Contact list size should decrease by 1");
    }

    @Test
    void deleteContact_InvalidAlias_DoesNotChangeSizeAndReturnsFalse() {
        user.addContact("Jane", "Doe", "123456789", "jane@example.com", "Janey");
        int sizeBeforeDelete = user.getContactCount();
        
        boolean deleted = user.deleteContact("NonExistent");
        
        assertFalse(deleted, "deleteContact should return false for non-existent alias");
        assertEquals(sizeBeforeDelete, user.getContactCount(), "Contact list size should remain unchanged");
    }
}
