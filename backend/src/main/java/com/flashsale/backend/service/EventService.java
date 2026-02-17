package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.common.util.BeanCopyUtil;
import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yang-Hsu
 * @description EventService
 * @date 2026/2/17 下午9:13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ProductService productService;

    /**
     * @description get total event when search product detail
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:14
     */
    @Transactional(readOnly = true)
    public Page<Event> getEventsByProductId(String productId, Pageable pageable) {
        return eventRepository.findByProductId(productId, pageable);
    }

    /**
     * @description createEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:16
     */
    @Transactional
    public Event createEvent(EventRequest request) {
        log.info("Creating event for product: {}", request.getProductId());
        productService.getProductById(request.getProductId());
        Event event = new Event();
        BeanUtils.copyProperties(request, event);
        event.setStatus(0); //UPCOMING
        Event savedEvent = eventRepository.save(event);
        log.info("Event created successfully: {}", savedEvent.getEventId());
        return savedEvent;
    }

    /**
     * @description getEventById for event update
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:14
     */
    @Transactional(readOnly = true)
    public Event getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ResultCode.EVENT_NOT_FOUND));
    }

    /**
     * @description updateEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:16
     */
    @Transactional
    public Event updateEvent(String eventId, EventRequest request) {
        log.info("Updating event: {}", eventId);
        Event event = getEventById(eventId);
        productService.getProductById(request.getProductId());
        BeanUtils.copyProperties(request, event, BeanCopyUtil.getNullPropertyNames(request));
        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated successfully: {}", eventId);
        return updatedEvent;
    }


    /**
     * @description deleteEvent
     * @author Yang-Hsu
     * @date 2026/2/17 下午9:18
     */
    @Transactional
    public void deleteEvent(String eventId) {
        log.info("Deleting event: {}", eventId);
        Event event = getEventById(eventId);
        eventRepository.delete(event);
        log.info("Event deleted successfully: {}", eventId);
    }
}
