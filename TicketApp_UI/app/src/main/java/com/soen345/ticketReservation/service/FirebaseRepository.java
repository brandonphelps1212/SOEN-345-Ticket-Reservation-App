package com.soen345.ticketReservation.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soen345.ticketReservation.model.Event;
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

import androidx.annotation.NonNull;

/**
 * Central repository for all Firebase Realtime Database operations.
 * Connects to the same database the Java backend uses.
 *
 * Collections:
 *   users/{userId}
 *   events/{eventId}
 *   reservations/{reservationId}
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

    /**
     * Register a new user with email.
     * Mirrors UserService.registerByEmail()
     */
    public void registerByEmail(String name, String email, String password,
                                UserCallback callback) {
        // Validate
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

        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = "hashed_" + password.hashCode();

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name.trim());
        userData.put("email", email.toLowerCase());
        userData.put("passwordHash", passwordHash);
        userData.put("role", "CUSTOMER");
        userData.put("createdAt", nowTimestamp());

        db.child("users").child(userId).setValue(userData)
            .addOnSuccessListener(unused -> {
                User user = new User(userId, name.trim(), email.toLowerCase(),
                        null, passwordHash, "CUSTOMER");
                callback.onSuccess(user);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Register a new user with phone number.
     * Mirrors UserService.registerByPhone()
     */
    public void registerByPhone(String name, String phone, String password,
                                UserCallback callback) {
        if (name == null || name.trim().isEmpty()) {
            callback.onError("Name cannot be empty.");
            return;
        }
        if (phone == null || !phone.matches("^\\+?[0-9]{7,15}$")) {
            callback.onError("Invalid phone number format.");
            return;
        }
        if (password == null || password.length() < 6) {
            callback.onError("Password must be at least 6 characters.");
            return;
        }

        String userId = "u-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = "hashed_" + password.hashCode();

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name.trim());
        userData.put("phone", phone);
        userData.put("passwordHash", passwordHash);
        userData.put("role", "CUSTOMER");
        userData.put("createdAt", nowTimestamp());

        db.child("users").child(userId).setValue(userData)
            .addOnSuccessListener(unused -> {
                User user = new User(userId, name.trim(), null,
                        phone, passwordHash, "CUSTOMER");
                callback.onSuccess(user);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ── EVENTS ───────────────────────────────────────────────────────────────

    /**
     * Fetch all ACTIVE events.
     * Mirrors EventService.getAllAvailableEvents()
     */
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

    /**
     * Fetch a single event by ID.
     * Mirrors EventService.getEventById()
     */
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

    // ── RESERVATIONS ─────────────────────────────────────────────────────────

    /**
     * Book a ticket for an event.
     * Mirrors ReservationService.bookTicket()
     *
     * Writes to:
     *   reservations/{reservationId}
     * And decrements:
     *   events/{eventId}/availableSeats
     */
    public void bookTicket(String userId, Event event, ReservationCallback callback) {
        if (!event.hasAvailableSeats()) {
            callback.onError("No seats available for this event.");
            return;
        }

        String ticketId     = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        double totalAmount  = event.getPrice();
        String now          = nowTimestamp();

        // Build reservation map (matches backend Firebase schema exactly)
        Map<String, Object> resData = new HashMap<>();
        resData.put("reservationId",   reservationId);
        resData.put("userId",          userId);
        resData.put("eventId",         event.getEventId());
        resData.put("ticketId",        ticketId);
        resData.put("totalAmount",     totalAmount);
        resData.put("status",          "CONFIRMED");
        resData.put("reservationDate", now);

        // Write reservation then decrement seat count atomically-ish
        db.child("reservations").child(reservationId).setValue(resData)
            .addOnSuccessListener(unused -> {
                // Decrement available seats
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

    // ── RESERVATIONS (continued) ─────────────────────────────────────────────

    /**
     * Fetch all reservations for a given user.
     * Reads from reservations/ and filters by userId.
     */
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

    /**
     * Cancel a reservation.
     * Mirrors ReservationService.cancelReservation():
     *   - Sets status to CANCELLED in reservations/{id}
     *   - Increments availableSeats back in events/{eventId}
     */
    public void cancelReservation(Reservation reservation, SimpleCallback callback) {
        if ("CANCELLED".equals(reservation.getStatus())) {
            callback.onError("This reservation is already cancelled.");
            return;
        }
        db.child("reservations").child(reservation.getReservationId())
                .child("status").setValue("CANCELLED")
                .addOnSuccessListener(unused -> {
                    // Give the seat back
                    db.child("events").child(reservation.getEventId())
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
                                    // Reservation was cancelled even if seat increment fails
                                    callback.onSuccess();
                                }
                            });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface ReservationsCallback {
        void onSuccess(List<Reservation> reservations);
        void onError(String error);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String nowTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }
}
