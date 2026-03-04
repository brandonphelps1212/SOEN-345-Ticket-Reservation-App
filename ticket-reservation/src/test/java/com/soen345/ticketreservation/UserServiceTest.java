package com.soen345.ticketreservation;

import com.soen345.ticketreservation.model.User;
import com.soen345.ticketreservation.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    @DisplayName("Register by email - success")
    void testRegisterByEmail_success() {
        User user = userService.registerByEmail("Brandon", "brandon@example.com", "password123");
        assertNotNull(user);
        assertEquals("brandon@example.com", user.getEmail());
        assertEquals(User.UserRole.CUSTOMER, user.getRole());
    }

    @Test
    @DisplayName("Register by email - duplicate throws exception")
    void testRegisterByEmail_duplicate() {
        userService.registerByEmail("Brandon", "brandon@example.com", "password123");
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerByEmail("Brandon2", "brandon@example.com", "pass456"));
    }

    @Test
    @DisplayName("Register by email - invalid email throws exception")
    void testRegisterByEmail_invalidEmail() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerByEmail("Brandon", "not-an-email", "password123"));
    }

    @Test
    @DisplayName("Register by phone - success")
    void testRegisterByPhone_success() {
        User user = userService.registerByPhone("Yan", "+15141234567", "securepass");
        assertNotNull(user);
        assertEquals("+15141234567", user.getPhoneNumber());
    }

    @Test
    @DisplayName("Register by phone - invalid phone throws exception")
    void testRegisterByPhone_invalidPhone() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerByPhone("Yan", "abc", "securepass"));
    }

    @Test
    @DisplayName("Find user by email")
    void testFindByEmail() {
        userService.registerByEmail("Brandon", "brandon@example.com", "password123");
        User found = userService.findByEmail("brandon@example.com");
        assertNotNull(found);
        assertEquals("Brandon", found.getName());
    }

    @Test
    @DisplayName("Find user by email - not found returns null")
    void testFindByEmail_notFound() {
        assertNull(userService.findByEmail("unknown@example.com"));
    }

    @Test
    @DisplayName("Password too short throws exception")
    void testRegisterByEmail_shortPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                userService.registerByEmail("Brandon", "brandon@example.com", "abc"));
    }
}
