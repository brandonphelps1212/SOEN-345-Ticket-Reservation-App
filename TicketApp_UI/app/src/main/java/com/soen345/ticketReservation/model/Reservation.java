package com.soen345.ticketReservation.model;

/**
 * Mirrors the backend Reservation.java model.
 * Written to Firebase under reservations/{reservationId}
 */
public class Reservation {

    private String reservationId;
    private String userId;
    private String eventId;
    private String ticketId;
    private String reservationDate;
    private String status;          // CONFIRMED, CANCELLED
    private double totalAmount;

    public Reservation() {} // Required by Firebase

    public Reservation(String reservationId, String userId, String eventId,
                       String ticketId, double totalAmount, String reservationDate) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.eventId = eventId;
        this.ticketId = ticketId;
        this.totalAmount = totalAmount;
        this.reservationDate = reservationDate;
        this.status = "CONFIRMED";
    }

    public String getReservationId()         { return reservationId; }
    public void setReservationId(String v)   { this.reservationId = v; }

    public String getUserId()                { return userId; }
    public void setUserId(String v)          { this.userId = v; }

    public String getEventId()               { return eventId; }
    public void setEventId(String v)         { this.eventId = v; }

    public String getTicketId()              { return ticketId; }
    public void setTicketId(String v)        { this.ticketId = v; }

    public String getReservationDate()       { return reservationDate; }
    public void setReservationDate(String v) { this.reservationDate = v; }

    public String getStatus()                { return status; }
    public void setStatus(String v)          { this.status = v; }

    public double getTotalAmount()           { return totalAmount; }
    public void setTotalAmount(double v)     { this.totalAmount = v; }
}
