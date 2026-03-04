package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin operations: add, edit, and cancel events.
 * Author: Yan
 */
public class AdminService {

    private final Map<String, Event>       events       = new HashMap<>();
    private final Map<String, Reservation> reservations = new HashMap<>();

    // ─────────────────────────────────────────────────────────
    //  ADD A NEW EVENT
    // ─────────────────────────────────────────────────────────
    public Event addEvent(Event event) {
        if (event == null || event.getEventId() == null)
            throw new IllegalArgumentException("Event or Event ID cannot be null.");

        if (events.containsKey(event.getEventId()))
            throw new IllegalStateException("Event ID already exists: " + event.getEventId());

        events.put(event.getEventId(), event);

        // Save to Firebase
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("title",          event.getTitle());
            data.put("category",       event.getCategory().toString());
            data.put("location",       event.getLocation());
            data.put("dateTime",       event.getDateTime().toString());
            data.put("totalSeats",     event.getTotalSeats());
            data.put("availableSeats", event.getAvailableSeats());
            data.put("ticketPrice",    event.getTicketPrice());
            data.put("status",         event.getStatus().toString());
            FirebaseService.writeData("events/" + event.getEventId(), data);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase write failed: " + e.getMessage());
        }

        System.out.println("[ADMIN] Event added: " + event);
        return event;
    }

    // ─────────────────────────────────────────────────────────
    //  EDIT AN EXISTING EVENT
    // ─────────────────────────────────────────────────────────
    public Event editEvent(String eventId, Event updated) {
        Event existing = events.get(eventId);
        if (existing == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return null;
        }

        // Apply only non-null / non-zero fields
        if (updated.getTitle()       != null) existing.setTitle(updated.getTitle());
        if (updated.getCategory()    != null) existing.setCategory(updated.getCategory());
        if (updated.getLocation()    != null) existing.setLocation(updated.getLocation());
        if (updated.getDateTime()    != null) existing.setDateTime(updated.getDateTime());
        if (updated.getTicketPrice() >  0)    existing.setTicketPrice(updated.getTicketPrice());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());

        // Update Firebase
        try {
            Map<String, Object> changes = new HashMap<>();
            if (updated.getTitle()       != null) changes.put("title",       updated.getTitle());
            if (updated.getLocation()    != null) changes.put("location",    updated.getLocation());
            if (updated.getDateTime()    != null) changes.put("dateTime",    updated.getDateTime().toString());
            if (updated.getTicketPrice() >  0)    changes.put("ticketPrice", updated.getTicketPrice());
            FirebaseService.writeData("events/" + eventId, changes);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase update failed: " + e.getMessage());
        }

        System.out.println("[ADMIN] Event updated: " + existing);
        return existing;
    }

    // ─────────────────────────────────────────────────────────
    //  CANCEL AN EVENT  (also cancels all linked reservations)
    // ─────────────────────────────────────────────────────────
    public boolean cancelEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return false;
        }

        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            System.out.println("[ERROR] Event already cancelled.");
            return false;
        }

        // 1. Cancel the event
        event.setStatus(Event.EventStatus.CANCELLED);

        // 2. Cancel all linked reservations
        int count = 0;
        for (Reservation r : reservations.values()) {
            if (r.getEventId().equals(eventId)
                    && r.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
                r.setStatus(Reservation.ReservationStatus.CANCELLED);
                count++;

                try {
                    FirebaseService.writeData(
                        "reservations/" + r.getReservationId() + "/status", "CANCELLED");
                } catch (Exception e) {
                    System.out.println("[WARNING] Firebase reservation update failed: " + e.getMessage());
                }
            }
        }

        // 3. Update event in Firebase
        try {
            FirebaseService.writeData("events/" + eventId + "/status", "CANCELLED");
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase event cancel failed: " + e.getMessage());
        }

        System.out.println("[ADMIN] Event cancelled: " + eventId + " | Reservations cancelled: " + count);
        return true;
    }

    // ─────────────────────────────────────────────────────────
    //  UTILITY
    // ─────────────────────────────────────────────────────────
    public Event       getEvent(String eventId) { return events.get(eventId); }
    public List<Event> getAllEvents()            { return new ArrayList<>(events.values()); }

    public void addReservationToStore(Reservation r) {
        reservations.put(r.getReservationId(), r);
    }
}
