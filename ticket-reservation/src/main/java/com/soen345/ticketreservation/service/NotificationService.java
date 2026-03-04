package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Reservation;

/**
 * Mock notification service — prints to console instead of sending real messages.
 * Author: Yan
 */
public class NotificationService {

    // ─────────────────────────────────────────────────────────
    //  EMAIL  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendEmailConfirmation(String email, Reservation reservation) {
        System.out.println("========================================");
        System.out.println("[EMAIL STUB] To         : " + email);
        System.out.println("[EMAIL STUB] Subject    : Booking Confirmed!");
        System.out.println("[EMAIL STUB] Res ID     : " + reservation.getReservationId());
        System.out.println("[EMAIL STUB] Event ID   : " + reservation.getEventId());
        System.out.println("[EMAIL STUB] Tickets    : " + (reservation.getTickets() != null ? reservation.getTickets().size() : 0));
        System.out.println("[EMAIL STUB] Total      : $" + reservation.getTotalAmount());
        System.out.println("[EMAIL STUB] Status     : " + reservation.getStatus());
        System.out.println("[EMAIL STUB] Date       : " + reservation.getReservationDate());
        System.out.println("========================================");
    }

    // ─────────────────────────────────────────────────────────
    //  SMS  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendSMSConfirmation(String phoneNumber, Reservation reservation) {
        System.out.println("========================================");
        System.out.println("[SMS STUB] To     : " + phoneNumber);
        System.out.println("[SMS STUB] Res ID : " + reservation.getReservationId());
        System.out.println("[SMS STUB] Event  : " + reservation.getEventId());
        System.out.println("[SMS STUB] Total  : $" + reservation.getTotalAmount());
        System.out.println("========================================");
    }

    // ─────────────────────────────────────────────────────────
    //  CANCELLATION  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendCancellationNotification(String email, String phone, Reservation reservation) {
        System.out.println("[EMAIL STUB] Cancellation → " + email
                + " | Res: " + reservation.getReservationId());
        System.out.println("[SMS STUB]   Cancellation → " + phone
                + " | Res: " + reservation.getReservationId());
    }
}
