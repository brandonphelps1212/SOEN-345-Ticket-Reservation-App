package com.soen345.ticketReservation.service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.model.Notification;
import com.soen345.ticketReservation.model.Reservation;
import com.soen345.ticketReservation.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Central repository for all Firebase Realtime Database operations.
 *
 * Collections:
 *   users/{userId}
 *   events/{eventId}
 *   reservations/{reservationId}
 *   notifications/{notificationId}
 */
public class FirebaseRepository {

    // ── Callbacks ────────────────────────────────────────────────────────────

    public interface EventsCallback {
        void onSuccess(List<Event> events);
        void onError(String error);
    }

    public interface EventCallback {
        void onSuccess(Event event);
        void onError(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface ReservationCallback {
        void onSuccess(Reservation reservation);
        void onError(String error);
    }

    public interface ReservationsCallback {
        void onSuccess(List<Reservation> reservations);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    // ── Database reference ───────────────────────────────────────────────────

    private final DatabaseReference db;

    public FirebaseRepository() {
        db = FirebaseDatabase.getInstance().getReference();
    }

    // ── USERS ────────────────────────────────────────────────────────────────

    /** Register a new CUSTOMER user with email. */
    public void registerByEmail(String name, String email, String password,
                                UserCallback callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Name cannot be empty.");
            return;
        }
        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            callback.onError("Invalid email format.");
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters.");
            return;
        }

        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = hashPassword(password);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name.trim());
        userData.put("email", cleanEmail);
        userData.put("phoneNumber", null);
        userData.put("passwordHash", passwordHash);
        userData.put("role", "CUSTOMER");
        userData.put("createdAt", nowTimestamp());

        db.child("users").child(userId).setValue(userData)
            .addOnSuccessListener(unused -> callback.onSuccess(
                    new User(userId, name.trim(), cleanEmail, null, passwordHash, "CUSTOMER")))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Register a new CUSTOMER user with phone number. */
    public void registerByPhone(String name, String phone, String password,
                                UserCallback callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Name cannot be empty.");
            return;
        }
        if (phone == null || !phone.trim().matches("^\\+?[0-9]{7,15}$")) {
            callback.onError("Invalid phone number format.");
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters.");
            return;
        }

        String cleanPhone = phone.trim();
        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = hashPassword(password);

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name.trim());
        userData.put("email", null);
        userData.put("phoneNumber", cleanPhone);
        userData.put("phone", cleanPhone); // Backward compatibility with older project data.
        userData.put("passwordHash", passwordHash);
        userData.put("role", "CUSTOMER");
        userData.put("createdAt", nowTimestamp());

        db.child("users").child(userId).setValue(userData)
            .addOnSuccessListener(unused -> callback.onSuccess(
                    new User(userId, name.trim(), null, cleanPhone, passwordHash, "CUSTOMER")))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Simple login for existing Firebase users.
     * Identifier can be either email or phone number.
     * Password matching uses the same existing app logic: "hashed_" + password.hashCode().
     */
    public void login(String identifier, String password, UserCallback callback) {
        if (identifier == null || identifier.trim().isEmpty()) {
            callback.onError("Enter your email or phone number.");
            return;
        }
        if (password == null || password.isEmpty()) {
            callback.onError("Enter your password.");
            return;
        }

        String cleanIdentifier = identifier.trim();
        String expectedHash = hashPassword(password);

        db.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = userFromSnapshot(child);
                    if (user == null) continue;

                    boolean emailMatches = user.getEmail() != null
                            && user.getEmail().equalsIgnoreCase(cleanIdentifier);
                    boolean phoneMatches = user.getPhoneNumber() != null
                            && user.getPhoneNumber().equals(cleanIdentifier);
                    boolean passwordMatches = expectedHash.equals(user.getPasswordHash());

                    if ((emailMatches || phoneMatches) && passwordMatches) {
                        callback.onSuccess(user);
                        return;
                    }
                }
                callback.onError("Invalid login information.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // ── EVENTS ───────────────────────────────────────────────────────────────

    /** Fetch all ACTIVE events for customers. */
    public void getAllAvailableEvents(EventsCallback callback) {
        db.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event event = child.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(child.getKey());
                        if ("ACTIVE".equals(event.getStatus())) {
                            events.add(event);
                        }
                    }
                }
                callback.onSuccess(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /** Admin: fetch all events, including ACTIVE, CANCELLED, and SOLD_OUT. */
    public void getAllEventsForAdmin(EventsCallback callback) {
        db.child("events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event event = child.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(child.getKey());
                        events.add(event);
                    }
                }
                callback.onSuccess(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /** Fetch a single event by ID. */
    public void getEventById(String eventId, EventCallback callback) {
        db.child("events").child(eventId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(snapshot.getKey());
                        callback.onSuccess(event);
                    } else {
                        callback.onError("Event not found.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
    }

    /** Admin: add a new event using the exact Firebase field names required. */
    public void addEvent(Event event, SimpleCallback callback) {
        if (event == null) {
            callback.onError("Event cannot be empty.");
            return;
        }

        String eventId = event.getEventId();
        if (eventId == null || eventId.trim().isEmpty()) {
            eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            event.setEventId(eventId);
        }

        db.child("events").child(eventId).setValue(eventToMap(event))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Admin: update an existing event using the exact Firebase field names required. */
    public void updateEvent(Event event, SimpleCallback callback) {
        if (event == null || event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            callback.onError("Event ID is required.");
            return;
        }

        db.child("events").child(event.getEventId()).setValue(eventToMap(event))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Admin: cancel an event by changing its status to CANCELLED. */
    public void cancelEvent(String eventId, SimpleCallback callback) {
        if (eventId == null || eventId.trim().isEmpty()) {
            callback.onError("Event ID is required.");
            return;
        }

        db.child("events").child(eventId).child("status").setValue("CANCELLED")
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── RESERVATIONS ─────────────────────────────────────────────────────────

    /**
     * Book a ticket for an event.
     * Writes to reservations/{reservationId} and decrements events/{eventId}/availableSeats.
     */
    public void bookTicket(String userId, Event event, ReservationCallback callback) {
        if (event == null || !event.hasAvailableSeats()) {
            callback.onError("No seats available for this event.");
            return;
        }

        String ticketId     = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        double totalAmount  = event.getPrice();
        String now          = nowTimestamp();

        Map<String, Object> resData = new HashMap<>();
        resData.put("reservationId",   reservationId);
        resData.put("userId",          userId);
        resData.put("eventId",         event.getEventId());
        resData.put("ticketId",        ticketId);
        resData.put("totalAmount",     totalAmount);
        resData.put("status",          "CONFIRMED");
        resData.put("reservationDate", now);

        db.child("reservations").child(reservationId).setValue(resData)
            .addOnSuccessListener(unused -> {
                int newSeats = event.getAvailableSeats() - 1;
                db.child("events").child(event.getEventId())
                    .child("availableSeats").setValue(newSeats);

                Reservation reservation = new Reservation(
                        reservationId, userId, event.getEventId(),
                        ticketId, totalAmount, now);
                callback.onSuccess(reservation);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /** Fetch all reservations for a given user. */
    public void getReservationsForUser(String userId, ReservationsCallback callback) {
        db.child("reservations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Reservation> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Reservation r = child.getValue(Reservation.class);
                    if (r != null && userId.equals(r.getUserId())) {
                        r.setReservationId(child.getKey());
                        list.add(r);
                    }
                }
                callback.onSuccess(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /** Cancel a reservation and increment the event's availableSeats. */
    public void cancelReservation(Reservation reservation, SimpleCallback callback) {
        if (reservation == null) {
            callback.onError("Reservation cannot be empty.");
            return;
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            callback.onError("This reservation is already cancelled.");
            return;
        }

        db.child("reservations").child(reservation.getReservationId())
                .child("status").setValue("CANCELLED")
                .addOnSuccessListener(unused -> db.child("events").child(reservation.getEventId())
                        .child("availableSeats")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Long current = snapshot.getValue(Long.class);
                                int seats = current != null ? current.intValue() : 0;
                                db.child("events").child(reservation.getEventId())
                                        .child("availableSeats").setValue(seats + 1);
                                callback.onSuccess();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                callback.onSuccess();
                            }
                        }))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── NOTIFICATIONS ────────────────────────────────────────────────────────

    /**
     * Creates a simulated email/SMS confirmation record.
     * This is not a real delivery API. It writes proof records to Firebase for the course requirement.
     */
    public void createNotification(Notification notification, SimpleCallback callback) {
        if (notification == null) {
            callback.onError("Notification cannot be empty.");
            return;
        }

        String notificationId = notification.getNotificationId();
        if (notificationId == null || notificationId.trim().isEmpty()) {
            notificationId = "NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            notification.setNotificationId(notificationId);
        }

        db.child("notifications").child(notificationId).setValue(notificationToMap(notification))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public String getCurrentTimestamp() {
        return nowTimestamp();
    }

    private String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private String hashPassword(String password) {
        return "hashed_" + password.hashCode();
    }

    private User userFromSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return null;

        String userId = getString(snapshot, "userId");
        if (userId == null || userId.trim().isEmpty()) {
            userId = snapshot.getKey();
        }

        String name = getString(snapshot, "name");
        String email = getString(snapshot, "email");
        String phoneNumber = firstNonBlank(
                getString(snapshot, "phoneNumber"),
                getString(snapshot, "phone"));
        String passwordHash = getString(snapshot, "passwordHash");
        String role = firstNonBlank(getString(snapshot, "role"), "CUSTOMER");

        return new User(userId, name, email, phoneNumber, passwordHash, role);
    }

    private String getString(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) return first;
        if (second != null && !second.trim().isEmpty()) return second;
        return null;
    }

    private Map<String, Object> eventToMap(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", event.getEventId());
        map.put("title", event.getTitle());
        map.put("description", event.getDescription());
        map.put("category", event.getCategory());
        map.put("location", event.getLocation());
        map.put("eventDate", event.getEventDate());
        map.put("totalSeats", event.getTotalSeats());
        map.put("availableSeats", event.getAvailableSeats());
        map.put("price", event.getPrice());
        map.put("status", event.getStatus());
        return map;
    }

    private Map<String, Object> notificationToMap(Notification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("notificationId", notification.getNotificationId());
        map.put("userId", notification.getUserId());
        map.put("reservationId", notification.getReservationId());
        map.put("eventId", notification.getEventId());
        map.put("type", notification.getType());
        map.put("recipient", notification.getRecipient());
        map.put("subject", notification.getSubject());
        map.put("message", notification.getMessage());
        map.put("status", notification.getStatus());
        map.put("createdAt", notification.getCreatedAt());
        return map;
    }
}
