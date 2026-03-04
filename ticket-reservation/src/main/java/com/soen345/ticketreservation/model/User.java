package com.soen345.ticketreservation.model;

/**
 * Represents a registered user (customer) in the system.
 */
public class User {

    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private UserRole role;

    public enum UserRole {
        CUSTOMER, ADMIN
    }

    public User() {}

    public User(String userId, String name, String email, String phoneNumber, String passwordHash, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    @Override
    public String toString() {
        return "User{userId='" + userId + "', name='" + name + "', email='" + email + "', role=" + role + "}";
    }
}
