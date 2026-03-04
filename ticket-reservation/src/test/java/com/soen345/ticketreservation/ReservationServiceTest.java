package com.soen345.ticketreservation;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReservationService.
 * Author: Yan
 */
class ReservationServiceTest {

    private ReservationService reservationService;
    private Event              sampleEvent;

    @BeforeEach
    void setUp() {
        // Use real NotificationService (it just prints, no external calls)
        reservationService = new ReservationService(new NotificationService());

        // A sample active event with 2 seats
        sampleEvent = new Event(
                "e-test-01",
                "Test Concert",
                Event.Category.CONCERT,
                "Montreal",
                LocalDateTime.now().plusDays(10),
                2,
                50.00
        );
        reservationService.addEventToStore(sampleEvent);
    }

    // ── bookTicket ────────────────────────────────────────────

    @Test
    @DisplayName("Should book a ticket when seats are available")
    void testBookTicket_Success() {
        Reservation r = reservationService.bookTicket("u-001", "e-test-01");

        assertNotNull(r);
        assertEquals("u-001",                     r.getUserId());
        assertEquals("e-test-01",                 r.getEventId());
        assertEquals(Reservation.Status.CONFIRMED, r.getStatus());
        assertEquals(1, sampleEvent.getAvailableSeats());
    }

    @Test
    @DisplayName("Should return null for a non-existent event")
    void testBookTicket_EventNotFound() {
        assertNull(reservationService.bookTicket("u-001", "e-fake"));
    }

    @Test
    @DisplayName("Should return null when event is fully booked")
    void testBookTicket_NoSeats() {
        reservationService.bookTicket("u-001", "e-test-01");
        reservationService.bookTicket("u-002", "e-test-01");
        assertNull(reservationService.bookTicket("u-003", "e-test-01"));
    }

    @Test
    @DisplayName("Should return null for a cancelled event")
    void testBookTicket_CancelledEvent() {
        sampleEvent.setStatus(Event.Status.CANCELLED);
        assertNull(reservationService.bookTicket("u-001", "e-test-01"));
    }

    // ── cancelReservation ─────────────────────────────────────

    @Test
    @DisplayName("Should cancel a confirmed reservation and return seat")
    void testCancelReservation_Success() {
        Reservation r = reservationService.bookTicket("u-001", "e-test-01");
        assertTrue(reservationService.cancelReservation(r.getId()));
        assertEquals(Reservation.Status.CANCELLED, r.getStatus());
        assertEquals(2, sampleEvent.getAvailableSeats()); // seat returned
    }

    @Test
    @DisplayName("Should return false for unknown reservation ID")
    void testCancelReservation_NotFound() {
        assertFalse(reservationService.cancelReservation("RES-FAKE"));
    }

    @Test
    @DisplayName("Should return false on double-cancel")
    void testCancelReservation_AlreadyCancelled() {
        Reservation r = reservationService.bookTicket("u-001", "e-test-01");
        reservationService.cancelReservation(r.getId());
        assertFalse(reservationService.cancelReservation(r.getId()));
    }

    // ── getReservation ────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve reservation by ID")
    void testGetReservation_Found() {
        Reservation booked = reservationService.bookTicket("u-001", "e-test-01");
        assertEquals(booked.getId(), reservationService.getReservation(booked.getId()).getId());
    }

    @Test
    @DisplayName("Should return null for unknown ID")
    void testGetReservation_NotFound() {
        assertNull(reservationService.getReservation("RES-UNKNOWN"));
    }
}
