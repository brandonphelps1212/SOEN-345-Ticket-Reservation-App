package com.soen345.ticketreservation;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.Reservation;
import com.soen345.ticketreservation.model.Ticket;
import com.soen345.ticketreservation.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AdminService.
 * Author: Yan
 */
class AdminServiceTest {

    private AdminService adminService;
    private Event        sampleEvent;

    @BeforeEach
    void setUp() {
        adminService = new AdminService();

        sampleEvent = new Event(
                "e-admin-01",
                "Admin Test Event",
                "A test event",
                Event.EventCategory.MOVIE,
                "Quebec City",
                LocalDateTime.now().plusDays(5),
                100,
                25.00
        );
    }

    // ── addEvent ──────────────────────────────────────────────

    @Test
    @DisplayName("Should add a new event successfully")
    void testAddEvent_Success() {
        Event saved = adminService.addEvent(sampleEvent);
        assertNotNull(saved);
        assertEquals("e-admin-01",           saved.getEventId());
        assertEquals(Event.EventStatus.ACTIVE, saved.getStatus());
    }

    @Test
    @DisplayName("Should throw on duplicate event ID")
    void testAddEvent_DuplicateId() {
        adminService.addEvent(sampleEvent);
        assertThrows(IllegalStateException.class, () -> adminService.addEvent(sampleEvent));
    }

    @Test
    @DisplayName("Should throw when event is null")
    void testAddEvent_NullEvent() {
        assertThrows(IllegalArgumentException.class, () -> adminService.addEvent(null));
    }

    // ── editEvent ─────────────────────────────────────────────

    @Test
    @DisplayName("Should update title and location")
    void testEditEvent_Success() {
        adminService.addEvent(sampleEvent);

        Event updates = new Event();
        updates.setTitle("New Title");
        updates.setLocation("Laval");

        Event result = adminService.editEvent("e-admin-01", updates);
        assertNotNull(result);
        assertEquals("New Title",              result.getTitle());
        assertEquals("Laval",                  result.getLocation());
        assertEquals(Event.EventCategory.MOVIE, result.getCategory()); // unchanged
    }

    @Test
    @DisplayName("Should return null for unknown event ID")
    void testEditEvent_NotFound() {
        Event updates = new Event();
        updates.setTitle("Whatever");
        assertNull(adminService.editEvent("e-fake", updates));
    }

    // ── cancelEvent ───────────────────────────────────────────

    @Test
    @DisplayName("Should cancel an active event")
    void testCancelEvent_Success() {
        adminService.addEvent(sampleEvent);
        assertTrue(adminService.cancelEvent("e-admin-01"));
        assertEquals(Event.EventStatus.CANCELLED,
                adminService.getEvent("e-admin-01").getStatus());
    }

    @Test
    @DisplayName("Should also cancel linked reservations")
    void testCancelEvent_CancelsReservations() {
        adminService.addEvent(sampleEvent);

        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-001");
        ticket.setEventId("e-admin-01");

        Reservation r = new Reservation(
                "RES-001", "u-001", "e-admin-01", List.of(ticket), 25.00);
        adminService.addReservationToStore(r);

        adminService.cancelEvent("e-admin-01");

        assertEquals(Reservation.ReservationStatus.CANCELLED, r.getStatus());
    }

    @Test
    @DisplayName("Should return false for unknown event")
    void testCancelEvent_NotFound() {
        assertFalse(adminService.cancelEvent("e-fake"));
    }

    @Test
    @DisplayName("Should return false on double-cancel")
    void testCancelEvent_AlreadyCancelled() {
        adminService.addEvent(sampleEvent);
        adminService.cancelEvent("e-admin-01");
        assertFalse(adminService.cancelEvent("e-admin-01"));
    }
}
