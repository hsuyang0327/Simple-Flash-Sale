package com.flashsale.backend.service;

import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {

    Page<Event> getEventsByProductId(String productId, Pageable pageable);

    Event createEvent(EventRequest request);

    Event getEventById(String eventId);

    Event updateEvent(String eventId, EventRequest request);

    void deleteEvent(String eventId);

    void preloadEventsForToday();

    void preloadEventsForTomorrow();
}
