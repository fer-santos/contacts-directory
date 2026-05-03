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
}
