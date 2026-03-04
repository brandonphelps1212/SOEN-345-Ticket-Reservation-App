package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;
import com.soen345.ticketreservation.model.Ticket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles booking, cancellation, and retrieval of reservations.
 * Author: Yan
 */
public class ReservationService {

    private final Map<String, Reservation> reservations = new HashMap<>();
    private final Map<String, Event>       events       = new HashMap<>();

    private final NotificationService notificationService;

    public ReservationService(NotificationService notificationService) {
        this.notificationService = notificationService;
        try {
            FirebaseService.initialize();
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase not connected: " + e.getMessage());
            System.out.println("[INFO] Running with in-memory storage only.");
        }
    }

    public Reservation bookTicket(String userId, String eventId) {
        Event event = events.get(eventId);
        if (event == null) {
            System.out.println("[ERROR] Event not found: " + eventId);
            return null;
        }
        if (!event.hasAvailableSeats()) {
            System.out.println("[ERROR] Event is not available: " + eventId);
            return null;
        }
        String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setEventId(eventId);
        List<Ticket> ticketList = new ArrayList<>();
        ticketList.add(ticket);
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalAmount = event.getTicketPrice();
        Reservation reservation = new Reservation(reservationId, userId, eventId, ticketList, totalAmount);
        event.setAvailableSeats(event.getAvailableSeats() - 1);
        reservations.put(reservationId, reservation);
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("userId",          userId);
            data.put("eventId",         eventId);
            data.put("ticketId",        ticketId);
            data.put("totalAmount",     totalAmount);
            data.put("status",          "CONFIRMED");
            data.put("reservationDate", reservation.getReservationDate().toString());
            FirebaseService.writeData("reservations/" + reservationId, data);
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase write failed: " + e.getMessage());
        }
        notificationService.sendEmailConfirmation("user@example.com", reservation);
        notificationService.sendSMSConfirmation("+15140000000", reservation);
        System.out.println("[SUCCESS] Reservation created: " + reservation);
        return reservation;
    }

    public boolean cancelReservation(String reservationId) {
        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            System.out.println("[ERROR] Reservation not found: " + reservationId);
            return false;
        }
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            System.out.println("[ERROR] Already cancelled.");
            return false;
        }
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Event event = events.get(reservation.getEventId());
        if (event != null) {
            event.setAvailableSeats(event.getAvailableSeats() + 1);
        }
        try {
            FirebaseService.writeData("reservations/" + reservationId + "/status", "CANCELLED");
        } catch (Exception e) {
            System.out.println("[WARNING] Firebase update failed: " + e.getMessage());
        }
        System.out.println("[SUCCESS] Reservation cancelled: " + reservationId);
        return true;
    }

    public Reservation getReservation(String reservationId) {
        return reservations.get(reservationId);
    }

    public void addEventToStore(Event event) {
        events.put(event.getEventId(), event);
    }
}
