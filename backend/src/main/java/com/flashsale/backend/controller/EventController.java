package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.dto.response.EventResponse;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Event Management", description = "Admin APIs for managing flash sale events.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @Operation(summary = "List Events", description = "Retrieves a paginated list of events for a specific product.")
    @GetMapping("/api/admin/events")
    public ResponseEntity<ApiResponse<Page<EventResponse>>> listEventsAdmin(
            @Parameter(description = "ID of the product to filter events by") @RequestParam String productId,
            @Parameter(description = "Pagination information") @PageableDefault(page = 0, size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: List events for product (Admin): {}", productId);
        Page<Event> events = eventService.getEventsByProductId(productId, pageable);
        Page<EventResponse> response = events.map(this::convertToResponse);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, response));
    }

    @Operation(summary = "Get Event", description = "Retrieves detailed information about a specific event by its ID.")
    @GetMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventAdmin(
            @Parameter(description = "ID of the event to retrieve") @PathVariable String id) {
        log.info("API: Get event by ID (Admin): {}", id);
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(event)));
    }

    @Operation(summary = "Create Event", description = "Creates a new flash sale event.")
    @PostMapping("/api/admin/events")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@Valid @RequestBody EventRequest request) {
        log.info("API: Create event (Admin)");
        Event createdEvent = eventService.createEvent(request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(createdEvent)));
    }

    @Operation(summary = "Update Event", description = "Updates an existing flash sale event.")
    @PutMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @Parameter(description = "ID of the event to update") @PathVariable String id,
            @Valid @RequestBody EventRequest request) {
        log.info("API: Update event (Admin): {}", id);
        Event updatedEvent = eventService.updateEvent(id, request);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(updatedEvent)));
    }

    @Operation(summary = "Delete Event", description = "Deletes a flash sale event by its ID.")
    @DeleteMapping("/api/admin/events/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @Parameter(description = "ID of the event to delete") @PathVariable String id) {
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
