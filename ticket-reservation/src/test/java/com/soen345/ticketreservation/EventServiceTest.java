package com.soen345.ticketreservation;

import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService();

        Event concert = new Event("e1", "Jazz Night", "Live jazz music", Event.EventCategory.CONCERT,
                "Montreal", LocalDateTime.of(2026, 4, 10, 20, 0), 100, 45.00);
        Event movie = new Event("e2", "Dune Part 3", "Sci-fi epic", Event.EventCategory.MOVIE,
                "Toronto", LocalDateTime.of(2026, 4, 15, 18, 0), 200, 15.00);
        Event sports = new Event("e3", "Habs vs Leafs", "NHL game", Event.EventCategory.SPORTS,
                "Montreal", LocalDateTime.of(2026, 4, 10, 19, 0), 500, 120.00);

        eventService.addEvent(concert);
        eventService.addEvent(movie);
        eventService.addEvent(sports);
    }

    @Test
    @DisplayName("Get all available events returns active events")
    void testGetAllAvailableEvents() {
        List<Event> events = eventService.getAllAvailableEvents();
        assertEquals(3, events.size());
    }

    @Test
    @DisplayName("Filter by category - CONCERT")
    void testFilterByCategory() {
        List<Event> concerts = eventService.filterByCategory(Event.EventCategory.CONCERT);
        assertEquals(1, concerts.size());
        assertEquals("Jazz Night", concerts.get(0).getTitle());
    }

    @Test
    @DisplayName("Filter by location - Montreal returns 2 events")
    void testFilterByLocation() {
        List<Event> results = eventService.filterByLocation("Montreal");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Filter by location - case insensitive")
    void testFilterByLocation_caseInsensitive() {
        List<Event> results = eventService.filterByLocation("montreal");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Filter by date")
    void testFilterByDate() {
        List<Event> results = eventService.filterByDate(LocalDate.of(2026, 4, 10));
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Combined search - category + location")
    void testSearch_categoryAndLocation() {
        List<Event> results = eventService.search(Event.EventCategory.SPORTS, "Montreal", null);
        assertEquals(1, results.size());
        assertEquals("Habs vs Leafs", results.get(0).getTitle());
    }

    @Test
    @DisplayName("Cancelled event not returned in results")
    void testCancelledEventNotReturned() {
        Event cancelled = new Event("e4", "Cancelled Show", "test", Event.EventCategory.CONCERT,
                "Montreal", LocalDateTime.of(2026, 5, 1, 20, 0), 50, 20.00);
        cancelled.setStatus(Event.EventStatus.CANCELLED);
        eventService.addEvent(cancelled);

        List<Event> all = eventService.getAllAvailableEvents();
        assertTrue(all.stream().noneMatch(e -> e.getEventId().equals("e4")));
    }

    @Test
    @DisplayName("Get event by ID")
    void testGetEventById() {
        assertTrue(eventService.getEventById("e1").isPresent());
        assertFalse(eventService.getEventById("nonexistent").isPresent());
    }
}
