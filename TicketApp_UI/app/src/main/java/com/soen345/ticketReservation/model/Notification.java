package com.soen345.ticketReservation.model;

/**
 * App-level simulated notification record.
 * Stored in Firebase under notifications/{notificationId} to prove that the
 * app generated email/SMS confirmations without using paid third-party APIs.
 */
public class Notification {

    private String notificationId;
    private String userId;
    private String reservationId;
    private String eventId;
    private String type;       // EMAIL_CONFIRMATION or SMS_CONFIRMATION
    private String recipient;
    private String subject;
    private String message;
    private String status;     // SENT_SIMULATED
    private String createdAt;

    public Notification() {} // Required by Firebase

    public Notification(String notificationId, String userId, String reservationId,
                        String eventId, String type, String recipient, String subject,
                        String message, String status, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.type = type;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
