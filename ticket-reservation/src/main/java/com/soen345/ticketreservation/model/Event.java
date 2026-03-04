package com.soen345.ticketreservation.model;

import java.time.LocalDateTime;

/**
 * Represents an event available for ticket booking.
 */
public class Event {

    private String eventId;
    private String title;
    private String description;
    private EventCategory category;
    private String location;
    private LocalDateTime dateTime;
    private int totalSeats;
    private int availableSeats;
    private double ticketPrice;
    private EventStatus status;

    public enum EventCategory {
        MOVIE, CONCERT, SPORTS, TRAVEL, OTHER
    }

    public enum EventStatus {
        ACTIVE, CANCELLED, SOLD_OUT
    }

    public Event() {}

    public Event(String eventId, String title, String description, EventCategory category,
                 String location, LocalDateTime dateTime, int totalSeats, double ticketPrice) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.ticketPrice = ticketPrice;
        this.status = EventStatus.ACTIVE;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventCategory getCategory() { return category; }
    public void setCategory(EventCategory category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public boolean hasAvailableSeats() {
        return availableSeats > 0 && status == EventStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return "Event{eventId='" + eventId + "', title='" + title + "', category=" + category
                + ", location='" + location + "', dateTime=" + dateTime
                + ", availableSeats=" + availableSeats + ", status=" + status + "}";
    }
}
