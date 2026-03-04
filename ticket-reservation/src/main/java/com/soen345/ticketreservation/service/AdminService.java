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
        if (event == null || event.getId() == null)
            throw new IllegalArgumentException("Event or Event ID cannot be null.");

        if (events.containsKey(event.getId()))
            throw new IllegalStateException("Event ID already exists: " + event.getId());

        events.put(event.getId(), event);

        // Save to Firebase
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("title",          event.getTitle());
            data.put("category",       event.getCategory().toString());
            data.put("location",       event.getLocation());
            data.put("eventDate",      event.getEventDate().toString());
            data.put("totalSeats",     event.getTotalSeats());
            data.put("availableSeats", event.getAvailableSeats());
            data.put("price",          event.getPrice());
            data.put("status",         event.getStatus().toString());
            FirebaseService.writeData("events/" + event.getId(), data);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase write failed: " + e.getMessage());
        }

        System.out.println("[ADMIN] Event added: " + event);
        return event;
    }

    // ─────────────────────────────────────────────────────────
    //  EDIT AN EXISTING EVENT
    // ─────────────────────────────────────────────────────────
    /**
     * Updates only the fields that are non-null / non-zero in `updated`.
     */
    public Event editEvent(String eventId, Event updated) {
        Event existing = events.get(eventId);
        if (existing == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return null;
        }

        if (updated.getTitle()     != null) existing.setTitle(updated.getTitle());
        if (updated.getCategory()  != null) existing.setCategory(updated.getCategory());
        if (updated.getLocation()  != null) existing.setLocation(updated.getLocation());
        if (updated.getEventDate() != null) existing.setEventDate(updated.getEventDate());
        if (updated.getPrice()     >  0)    existing.setPrice(updated.getPrice());

        // Update in Firebase
        try {
            Map<String, Object> changes = new HashMap<>();
            if (updated.getTitle()     != null) changes.put("title",     updated.getTitle());
            if (updated.getLocation()  != null) changes.put("location",  updated.getLocation());
            if (updated.getEventDate() != null) changes.put("eventDate", updated.getEventDate().toString());
            if (updated.getPrice()     >  0)    changes.put("price",     updated.getPrice());
            FirebaseService.writeData("events/" + eventId, changes);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase update failed: " + e.getMessage());
        }

        System.out.println("[ADMIN] Event updated: " + existing);
        return existing;
    }

    // ─────────────────────────────────────────────────────────
    //  CANCEL AN EVENT  (also cancels all its reservations)
    // ─────────────────────────────────────────────────────────
    public boolean cancelEvent(String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return false;
        }

        if (event.getStatus() == Event.Status.CANCELLED) {
            System.out.println("[ERROR] Event already cancelled.");
            return false;
        }

        // 1. Cancel the event
        event.setStatus(Event.Status.CANCELLED);

        // 2. Cancel all linked reservations
        int count = 0;
        for (Reservation r : reservations.values()) {
            if (r.getEventId().equals(eventId)
                    && r.getStatus() == Reservation.Status.CONFIRMED) {
                r.setStatus(Reservation.Status.CANCELLED);
                count++;

                // Update each reservation in Firebase
                try {
                    FirebaseService.writeData("reservations/" + r.getId() + "/status", "CANCELLED");
                } catch (Exception e) {
                    System.out.println("[WARNING] Firebase reservation update failed: " + e.getMessage());
                }
            }
        }

        // 3. Update event status in Firebase
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
    public Event        getEvent(String eventId) { return events.get(eventId); }
    public List<Event>  getAllEvents()            { return new ArrayList<>(events.values()); }

    public void addReservationToStore(Reservation r) { reservations.put(r.getId(), r); }
}
