package com.fer_santos.directory.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

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
