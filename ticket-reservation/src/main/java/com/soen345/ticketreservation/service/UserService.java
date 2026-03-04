package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles user registration and lookup.
 * Brandon's task: registration via email or phone number.
 */
public class UserService {

    // In-memory store — to be replaced with DB repository by Yan
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<String, User> usersByPhone = new HashMap<>();

    /**
     * Registers a new user by email.
     * @throws IllegalArgumentException if email is already in use or invalid
     */
    public User registerByEmail(String name, String email, String password) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        if (usersByEmail.containsKey(email.toLowerCase())) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        String userId = UUID.randomUUID().toString();
        String passwordHash = hashPassword(password);
        User user = new User(userId, name, email.toLowerCase(), null, passwordHash, User.UserRole.CUSTOMER);

        usersByEmail.put(email.toLowerCase(), user);
        return user;
    }

    /**
     * Registers a new user by phone number.
     * @throws IllegalArgumentException if phone is already in use or invalid
     */
    public User registerByPhone(String name, String phoneNumber, String password) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        validatePassword(password);

        if (usersByPhone.containsKey(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already registered: " + phoneNumber);
        }

        String userId = UUID.randomUUID().toString();
        String passwordHash = hashPassword(password);
        User user = new User(userId, name, null, phoneNumber, passwordHash, User.UserRole.CUSTOMER);

        usersByPhone.put(phoneNumber, user);
        return user;
    }

    /**
     * Finds a user by email. Returns null if not found.
     */
    public User findByEmail(String email) {
        if (email == null) return null;
        return usersByEmail.get(email.toLowerCase());
    }

    /**
     * Finds a user by phone number. Returns null if not found.
     */
    public User findByPhone(String phoneNumber) {
        if (phoneNumber == null) return null;
        return usersByPhone.get(phoneNumber);
    }

    // ─── Validation helpers ───────────────────────────────────────────────

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    private void validatePhoneNumber(String phone) {
        if (phone == null || !phone.matches("^\\+?[0-9]{7,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format: " + phone);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    /**
     * Placeholder hash — replace with BCrypt when DB layer is added.
     */
    private String hashPassword(String password) {
        return "hashed_" + password.hashCode();
    }
}