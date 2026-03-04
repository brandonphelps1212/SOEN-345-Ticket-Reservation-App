package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Reservation;

/**
 * Mock notification service — prints to console instead of sending real messages.
 *
 * HOW TO UPGRADE LATER:
 *   Email → replace sendEmailConfirmation() body with JavaMail API calls
 *   SMS   → replace sendSMSConfirmation() body with Twilio SDK calls
 *
 * Author: Yan
 */
public class NotificationService {

    // ─────────────────────────────────────────────────────────
    //  EMAIL  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendEmailConfirmation(String email, Reservation reservation) {
        System.out.println("========================================");
        System.out.println("[EMAIL STUB] To      : " + email);
        System.out.println("[EMAIL STUB] Subject : Booking Confirmed!");
        System.out.println("[EMAIL STUB] Res ID  : " + reservation.getId());
        System.out.println("[EMAIL STUB] Event   : " + reservation.getEventId());
        System.out.println("[EMAIL STUB] Ticket  : " + reservation.getTicketId());
        System.out.println("[EMAIL STUB] Status  : " + reservation.getStatus());
        System.out.println("========================================");

        // Real JavaMail code goes here later
    }

    // ─────────────────────────────────────────────────────────
    //  SMS  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendSMSConfirmation(String phoneNumber, Reservation reservation) {
        System.out.println("========================================");
        System.out.println("[SMS STUB] To     : " + phoneNumber);
        System.out.println("[SMS STUB] Res ID : " + reservation.getId());
        System.out.println("[SMS STUB] Event  : " + reservation.getEventId());
        System.out.println("========================================");

        // Real Twilio code goes here later
    }

    // ─────────────────────────────────────────────────────────
    //  CANCELLATION  (stub)
    // ─────────────────────────────────────────────────────────
    public void sendCancellationNotification(String email, String phone, Reservation reservation) {
        System.out.println("[EMAIL STUB] Cancellation notice → " + email
                + " | Res: " + reservation.getId());
        System.out.println("[SMS STUB]   Cancellation notice → " + phone
                + " | Res: " + reservation.getId());
    }
}
