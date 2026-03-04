package com.soen345.ticketreservation.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a reservation made by a user for an event.
 */
public class Reservation {

    private String reservationId;
    private String userId;
    private String eventId;
    private List<Ticket> tickets;
    private LocalDateTime reservationDate;
    private ReservationStatus status;
    private double totalAmount;

    public enum ReservationStatus {
        CONFIRMED, CANCELLED
    }

    public Reservation() {}

    public Reservation(String reservationId, String userId, String eventId,
                       List<Ticket> tickets, double totalAmount) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.eventId = eventId;
        this.tickets = tickets;
        this.totalAmount = totalAmount;
        this.reservationDate = LocalDateTime.now();
        this.status = ReservationStatus.CONFIRMED;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public LocalDateTime getReservationDate() { return reservationDate; }
    public void setReservationDate(LocalDateTime reservationDate) { this.reservationDate = reservationDate; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    @Override
    public String toString() {
        return "Reservation{reservationId='" + reservationId + "', userId='" + userId
                + "', eventId='" + eventId + "', tickets=" + (tickets != null ? tickets.size() : 0)
                + ", totalAmount=" + totalAmount + ", status=" + status + "}";
    }
}
