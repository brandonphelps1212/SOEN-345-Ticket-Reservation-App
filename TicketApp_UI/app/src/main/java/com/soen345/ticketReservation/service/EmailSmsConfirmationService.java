package com.soen345.ticketReservation.service;

import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.model.Notification;
import com.soen345.ticketReservation.model.Reservation;

import java.util.UUID;

/**
 * Simulated confirmation service for the course requirement.
 * It does not use Twilio, SendGrid, Mailgun, or any external delivery API.
 * It writes proof records to Firebase under notifications/{notificationId}.
 */
public class EmailSmsConfirmationService {

    public static final String TYPE_EMAIL = "EMAIL_CONFIRMATION";
    public static final String TYPE_SMS = "SMS_CONFIRMATION";
    public static final String STATUS_SENT_SIMULATED = "SENT_SIMULATED";

    public interface ConfirmationCallback {
        void onComplete(ConfirmationResult result);
    }

    public static class ConfirmationResult {
        public boolean emailCreated;
        public boolean smsCreated;
        public String emailRecipient;
        public String smsRecipient;
        public String statusMessage;
        public String confirmationMessage;
    }

    private final FirebaseRepository repository;

    public EmailSmsConfirmationService(FirebaseRepository repository) {
        this.repository = repository;
    }

    public void createBookingConfirmations(String userId, String email, String phoneNumber,
                                           Reservation reservation, Event event,
                                           ConfirmationCallback callback) {
        ConfirmationResult result = new ConfirmationResult();
        result.emailRecipient = isBlank(email) ? "Not available" : email.trim();
        result.smsRecipient = isBlank(phoneNumber) ? "Not available" : phoneNumber.trim();
        result.confirmationMessage = buildBookingMessage(reservation, event);

        boolean hasEmail = !isBlank(email);
        boolean hasPhone = !isBlank(phoneNumber);

        if (!hasEmail && !hasPhone) {
            result.statusMessage = "Booking confirmed, but no email or phone number is saved for this user.";
            callback.onComplete(result);
            return;
        }

        int count = (hasEmail ? 1 : 0) + (hasPhone ? 1 : 0);
        final int[] finished = {0};
        final StringBuilder errors = new StringBuilder();

        Runnable finishOne = () -> {
            finished[0]++;
            if (finished[0] == count) {
                result.statusMessage = buildStatusMessage(result, hasEmail, hasPhone, errors.toString());
                callback.onComplete(result);
            }
        };

        if (hasEmail) {
            Notification emailNotification = buildNotification(userId, reservation, event,
                    TYPE_EMAIL, email.trim(), result.confirmationMessage);
            repository.createNotification(emailNotification, new FirebaseRepository.SimpleCallback() {
                @Override public void onSuccess() {
                    result.emailCreated = true;
                    finishOne.run();
                }
                @Override public void onError(String error) {
                    errors.append("Email confirmation failed to save: ").append(error).append(" ");
                    finishOne.run();
                }
            });
        }

        if (hasPhone) {
            Notification smsNotification = buildNotification(userId, reservation, event,
                    TYPE_SMS, phoneNumber.trim(), result.confirmationMessage);
            repository.createNotification(smsNotification, new FirebaseRepository.SimpleCallback() {
                @Override public void onSuccess() {
                    result.smsCreated = true;
                    finishOne.run();
                }
                @Override public void onError(String error) {
                    errors.append("SMS confirmation failed to save: ").append(error).append(" ");
                    finishOne.run();
                }
            });
        }
    }

    private Notification buildNotification(String userId, Reservation reservation, Event event,
                                           String type, String recipient, String message) {
        String notificationId = "NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new Notification(notificationId, userId, reservation.getReservationId(), event.getEventId(),
                type, recipient, "Ticket Reservation Confirmation", message,
                STATUS_SENT_SIMULATED, repository.getCurrentTimestamp());
    }

    private String buildBookingMessage(Reservation reservation, Event event) {
        return "Ticket Reservation Confirmation\n"
                + "Reservation ID: " + safe(reservation.getReservationId()) + "\n"
                + "Ticket ID: " + safe(reservation.getTicketId()) + "\n"
                + "Event: " + safe(event.getTitle()) + "\n"
                + "Event Date: " + safe(event.getEventDate()) + "\n"
                + String.format("Total Amount: $%.2f\n", reservation.getTotalAmount())
                + "Reservation Status: " + safe(reservation.getStatus());
    }

    private String buildStatusMessage(ConfirmationResult result, boolean hadEmail, boolean hadPhone, String errors) {
        if (!isBlank(errors)) return errors.trim();
        if (result.emailCreated && result.smsCreated) return "Email and SMS confirmation sent";
        if (result.emailCreated && !hadPhone) return "Email confirmation sent. SMS skipped because no phone number is saved for this user.";
        if (result.smsCreated && !hadEmail) return "SMS confirmation sent. Email skipped because no email is saved for this user.";
        if (result.emailCreated) return "Email confirmation sent.";
        if (result.smsCreated) return "SMS confirmation sent.";
        return "Booking confirmed, but confirmation records could not be created.";
    }

    private String safe(String value) { return value == null ? "" : value; }
    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
}
