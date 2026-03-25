package com.soen345.ticketReservation.model;

/**
 * Mirrors the backend User.java model exactly.
 * Fields match the Firebase schema under users/{userId}
 */
public class User {

    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String role; // "CUSTOMER" or "ADMIN"

    public User() {} // Required by Firebase

    public User(String userId, String name, String email,
                String phoneNumber, String passwordHash, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId()           { return userId; }
    public void setUserId(String v)     { this.userId = v; }

    public String getName()             { return name; }
    public void setName(String v)       { this.name = v; }

    public String getEmail()            { return email; }
    public void setEmail(String v)      { this.email = v; }

    public String getPhoneNumber()      { return phoneNumber; }
    public void setPhoneNumber(String v){ this.phoneNumber = v; }

    public String getPasswordHash()     { return passwordHash; }
    public void setPasswordHash(String v){ this.passwordHash = v; }

    public String getRole()             { return role; }
    public void setRole(String v)       { this.role = v; }
}
