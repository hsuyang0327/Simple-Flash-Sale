package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.dto.response.EventResponse;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Yang-Hsu
 * @description EventController
 * @date 2026/2/17 下午8:57
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * @description listEventsAdmin
     * @author Yang-Hsu
     * @date 2026/2/17 下午8:57
     */
    @GetMapping("/api/admin/events")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> listEventsAdmin(
            @RequestParam String productId,
            @PageableDefault(page = 0, size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: List events for product (Admin): {}", productId);
        Page<Event> events = eventService.getEventsByProductId(productId, pageable);
        Page<EventResponse> response = events.map(this::convertToResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    /**
     * @description getEventAdmin
     * @author Yang-Hsu
     * @date 2026/2/17 下午8:57
     */
    @GetMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventAdmin(@PathVariable String id) {
        log.info("API: Get event by ID (Admin): {}", id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(event)));
    }

    /**
     * @description createEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午8:58
     */
    @PostMapping("/api/admin/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("API: Create event (Admin)");
        Event createdEvent = eventService.createEvent(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(createdEvent)));
    }

    /**
     * @description updateEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午8:58
     */
    @PutMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable String id, @Valid @RequestBody EventRequest request) {
        log.info("API: Update event (Admin): {}", id);
        Event updatedEvent = eventService.updateEvent(id, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(updatedEvent)));
    }

    /**
     * @description deleteEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午8:58
     */
    @DeleteMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable String id) {
        log.info("API: Delete event (Admin): {}", id);
        eventService.deleteEvent(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    private EventResponse convertToResponse(Event event) {
        return EventResponse.builder()
                .eventId(event.getEventId())
                .price(event.getPrice())
                .stock(event.getStock())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
