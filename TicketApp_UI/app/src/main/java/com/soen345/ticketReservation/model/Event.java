package com.soen345.ticketReservation.model;

/**
 * Mirrors the backend Event.java model exactly.
 * Fields match the Firebase schema under events/{eventId}
 */
public class Event {

    private String eventId;
    private String title;
    private String description;
    private String category;   // MOVIE, CONCERT, SPORTS, TRAVEL, OTHER
    private String location;
    private String eventDate;  // ISO string e.g. "2026-06-15T20:00:00"
    private int totalSeats;
    private int availableSeats;
    private double price;
    private String status;     // ACTIVE, CANCELLED, SOLD_OUT

    public Event() {} // Required by Firebase

    public String getEventId()              { return eventId; }
    public void setEventId(String v)        { this.eventId = v; }

    public String getTitle()                { return title; }
    public void setTitle(String v)          { this.title = v; }

    public String getDescription()          { return description; }
    public void setDescription(String v)    { this.description = v; }

    public String getCategory()             { return category; }
    public void setCategory(String v)       { this.category = v; }

    public String getLocation()             { return location; }
    public void setLocation(String v)       { this.location = v; }

    public String getEventDate()            { return eventDate; }
    public void setEventDate(String v)      { this.eventDate = v; }

    public int getTotalSeats()              { return totalSeats; }
    public void setTotalSeats(int v)        { this.totalSeats = v; }

    public int getAvailableSeats()          { return availableSeats; }
    public void setAvailableSeats(int v)    { this.availableSeats = v; }

    public double getPrice()                { return price; }
    public void setPrice(double v)          { this.price = v; }

    public String getStatus()               { return status; }
    public void setStatus(String v)         { this.status = v; }

    public boolean hasAvailableSeats() {
        return availableSeats > 0 && "ACTIVE".equals(status);
    }

    /** Formatted price string for display */
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
}
