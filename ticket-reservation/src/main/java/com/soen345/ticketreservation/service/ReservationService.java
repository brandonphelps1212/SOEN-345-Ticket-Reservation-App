package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles booking, cancellation, and retrieval of reservations.
 *
 * Currently uses in-memory Maps for logic.
 * Firebase writes are included — they will activate once
 * FirebaseService.initialize() connects successfully.
 *
 * Author: Yan
 */
public class ReservationService {

    // In-memory stores (source of truth for tests; Firebase mirrors this)
    private final Map<String, Reservation> reservations = new HashMap<>();
    private final Map<String, Event>       events       = new HashMap<>();

    private final NotificationService notificationService;

    public ReservationService(NotificationService notificationService) {
        this.notificationService = notificationService;

        // Connect to Firebase when the service starts
        try {
            FirebaseService.initialize();
        } catch (IOException e) {
            System.out.println("[WARNING] Firebase not connected: " + e.getMessage());
            System.out.println("[INFO] Running with in-memory storage only.");
        }
    }

    // ─────────────────────────────────────────────────────────
    //  BOOK A TICKET
    // ─────────────────────────────────────────────────────────
    /**
     * Books a ticket for a user on a given event.
     * @return the created Reservation, or null if booking failed
     */
    public Reservation bookTicket(String userId, String eventId) {

        // 1. Find the event
        Event event = events.get(eventId);
        if (event == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return null;
        }

        // 2. Check event is still active
        if (event.getStatus() == Event.Status.CANCELLED) {
            System.out.println("[ERROR] Cannot book a cancelled event.");
            return null;
        }

        // 3. Check seats available
        if (event.getAvailableSeats() <= 0) {
            System.out.println("[ERROR] No seats available for: " + eventId);
            return null;
        }

        // 4. Generate IDs
        String ticketId       = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String reservationId  = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5. Create reservation object
        Reservation reservation = new Reservation(reservationId, userId, eventId, ticketId);

        // 6. Decrement available seats
        event.setAvailableSeats(event.getAvailableSeats() - 1);

        // 7. Save in memory
        reservations.put(reservationId, reservation);

        // 8. Save to Firebase
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userId",    userId);
            data.put("eventId",   eventId);
            data.put("ticketId",  ticketId);
            data.put("status",    "CONFIRMED");
            data.put("createdAt", reservation.getCreatedAt().toString());
            FirebaseService.writeData("reservations/" + reservationId, data);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase write failed (still saved in memory): " + e.getMessage());
        }

        // 9. Send confirmation notifications
        notificationService.sendEmailConfirmation("user@example.com", reservation);
        notificationService.sendSMSConfirmation("+15140000000", reservation);

        System.out.println("[SUCCESS] Reservation created: " + reservation);
        return reservation;
    }

    // ─────────────────────────────────────────────────────────
    //  CANCEL A RESERVATION
    // ─────────────────────────────────────────────────────────
    /**
     * Cancels an existing reservation and frees the seat back to the event.
     * @return true if successful
     */
    public boolean cancelReservation(String reservationId) {

        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            System.out.println("[ERROR] Reservation not found: " + reservationId);
            return false;
        }

        if (reservation.getStatus() == Reservation.Status.CANCELLED) {
            System.out.println("[ERROR] Already cancelled.");
            return false;
        }

        // Update status in memory
        reservation.setStatus(Reservation.Status.CANCELLED);

        // Return seat to event
        Event event = events.get(reservation.getEventId());
        if (event != null) {
            event.setAvailableSeats(event.getAvailableSeats() + 1);
        }

        // Update in Firebase
        try {
            FirebaseService.writeData("reservations/" + reservationId + "/status", "CANCELLED");
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase update failed: " + e.getMessage());
        }

        System.out.println("[SUCCESS] Reservation cancelled: " + reservationId);
        return true;
    }

    // ─────────────────────────────────────────────────────────
    //  GET A RESERVATION
    // ─────────────────────────────────────────────────────────
    public Reservation getReservation(String reservationId) {
        return reservations.get(reservationId);
    }

    // ─────────────────────────────────────────────────────────
    //  HELPER — used by tests and other services
    // ─────────────────────────────────────────────────────────
    public void addEventToStore(Event event) {
        events.put(event.getId(), event);
    }
}
