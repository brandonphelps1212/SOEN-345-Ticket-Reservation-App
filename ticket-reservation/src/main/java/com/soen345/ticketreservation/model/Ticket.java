package com.soen345.ticketreservation.model;

/**
 * Represents a single ticket associated with a reservation.
 */
public class Ticket {

    private String ticketId;
    private String eventId;
    private String userId;
    private String reservationId;
    private double price;
    private TicketStatus status;

    public enum TicketStatus {
        CONFIRMED, CANCELLED
    }

    public Ticket() {}

    public Ticket(String ticketId, String eventId, String userId, String reservationId, double price) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.price = price;
        this.status = TicketStatus.CONFIRMED;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Ticket{ticketId='" + ticketId + "', eventId='" + eventId
                + "', userId='" + userId + "', price=" + price + ", status=" + status + "}";
    }
}
