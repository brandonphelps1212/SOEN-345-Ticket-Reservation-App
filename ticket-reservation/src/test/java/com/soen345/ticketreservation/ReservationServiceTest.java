package com.soen345.ticketreservation;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;
import com.soen345.ticketreservation.service.NotificationService;
import com.soen345.ticketreservation.service.ReservationService;
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
        reservationService = new ReservationService(new NotificationService());

        sampleEvent = new Event(
                "e-test-01",
                "Test Concert",
                "A great concert",
                Event.EventCategory.CONCERT,
                "Montreal",
                LocalDateTime.now().plusDays(10),
                2,        // totalSeats
                50.00     // ticketPrice
        );
        reservationService.addEventToStore(sampleEvent);
    }

    // ── bookTicket ────────────────────────────────────────────

    @Test
    @DisplayName("Should book a ticket when seats are available")
    void testBookTicket_Success() {
        Reservation r = reservationService.bookTicket("u-001", "e-test-01");

        assertNotNull(r);
        assertEquals("u-001",                                  r.getUserId());
        assertEquals("e-test-01",                              r.getEventId());
        assertEquals(Reservation.ReservationStatus.CONFIRMED,  r.getStatus());
        assertEquals(1, sampleEvent.getAvailableSeats());
        assertEquals(50.00, r.getTotalAmount());
        assertNotNull(r.getTickets());
        assertEquals(1, r.getTickets().size());
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
        sampleEvent.setStatus(Event.EventStatus.CANCELLED);
        assertNull(reservationService.bookTicket("u-001", "e-test-01"));
    }

    // ── cancelReservation ─────────────────────────────────────

    @Test
    @DisplayName("Should cancel a confirmed reservation and return seat")
    void testCancelReservation_Success() {
        Reservation r = reservationService.bookTicket("u-001", "e-test-01");
        assertNotNull(r);

        assertTrue(reservationService.cancelReservation(r.getReservationId()));
        assertEquals(Reservation.ReservationStatus.CANCELLED, r.getStatus());
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
        reservationService.cancelReservation(r.getReservationId());
        assertFalse(reservationService.cancelReservation(r.getReservationId()));
    }

    // ── getReservation ────────────────────────────────────────

    @Test
    @DisplayName("Should retrieve reservation by ID")
    void testGetReservation_Found() {
        Reservation booked = reservationService.bookTicket("u-001", "e-test-01");
        Reservation found  = reservationService.getReservation(booked.getReservationId());
        assertNotNull(found);
        assertEquals(booked.getReservationId(), found.getReservationId());
    }

    @Test
    @DisplayName("Should return null for unknown ID")
    void testGetReservation_NotFound() {
        assertNull(reservationService.getReservation("RES-UNKNOWN"));
    }
}
