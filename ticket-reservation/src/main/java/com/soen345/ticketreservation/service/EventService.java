package com.soen345.ticketreservation.service;

import com.soen345.ticketreservation.model.Event;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles event browsing, searching, and filtering.
 * Brandon's task: list events, search/filter by date, location, category.
 */
public class EventService {

    // In-memory store — to be replaced with DB repository by Yan
    private final Map<String, Event> events = new HashMap<>();

    /**
     * Adds an event to the store (used by admin service / tests).
     */
    public void addEvent(Event event) {
        if (event == null || event.getEventId() == null) {
            throw new IllegalArgumentException("Event and eventId cannot be null.");
        }
        events.put(event.getEventId(), event);
    }

    /**
     * Returns all active (non-cancelled) events.
     */
    public List<Event> getAllAvailableEvents() {
        return events.values().stream()
                .filter(e -> e.getStatus() == Event.EventStatus.ACTIVE)
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());
    }

    /**
     * Returns an event by its ID.
     */
    public Optional<Event> getEventById(String eventId) {
        return Optional.ofNullable(events.get(eventId));
    }

    /**
     * Filters events by category.
     */
    public List<Event> filterByCategory(Event.EventCategory category) {
        return events.values().stream()
                .filter(e -> e.getStatus() == Event.EventStatus.ACTIVE)
                .filter(e -> e.getCategory() == category)
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());
    }

    /**
     * Filters events by location (case-insensitive partial match).
     */
    public List<Event> filterByLocation(String location) {
        if (location == null || location.trim().isEmpty()) return getAllAvailableEvents();
        String loc = location.trim().toLowerCase();
        return events.values().stream()
                .filter(e -> e.getStatus() == Event.EventStatus.ACTIVE)
                .filter(e -> e.getLocation().toLowerCase().contains(loc))
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());
    }

    /**
     * Filters events on a specific date.
     */
    public List<Event> filterByDate(LocalDate date) {
        if (date == null) return getAllAvailableEvents();
        return events.values().stream()
                .filter(e -> e.getStatus() == Event.EventStatus.ACTIVE)
                .filter(e -> e.getDateTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());
    }

    /**
     * Combined search: filter by any combination of category, location, and date.
     * Null parameters are ignored (acts as wildcard).
     */
    public List<Event> search(Event.EventCategory category, String location, LocalDate date) {
        return events.values().stream()
                .filter(e -> e.getStatus() == Event.EventStatus.ACTIVE)
                .filter(e -> category == null || e.getCategory() == category)
                .filter(e -> location == null || location.trim().isEmpty()
                        || e.getLocation().toLowerCase().contains(location.trim().toLowerCase()))
                .filter(e -> date == null || e.getDateTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Event::getDateTime))
                .collect(Collectors.toList());
    }
}