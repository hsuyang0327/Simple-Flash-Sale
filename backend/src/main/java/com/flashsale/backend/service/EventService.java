package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.common.util.BeanCopyUtil;
import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.dto.response.EventProductDTO;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREHEATED_PRODUCT_KEYS = "preheated_product_keys";

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

        if (LocalDateTime.now().isAfter(event.getEndTime())) {
            throw new BusinessException(ResultCode.EVENT_EXPIRED);
        }

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

    /**
     * @description
     * @author Yang-Hsu
     * @date 2026/2/18 下午7:26
     */
    @Transactional(readOnly = true)
    public void preloadEventsForTomorrow() {
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);
        List<EventProductDTO> preheatData = eventRepository.findPreheatEvents(startOfTomorrow, endOfTomorrow);
        redisTemplate.delete(PREHEATED_PRODUCT_KEYS);
        for (EventProductDTO dto : preheatData) {
            String productKey = "productId:" + dto.getProductId();
            redisTemplate.delete(productKey); //Delete First
            Map<String, String> eventDetails = convertToRedisMap(dto);
            redisTemplate.opsForHash().putAll(productKey, eventDetails);
            redisTemplate.expire(productKey, 2, TimeUnit.DAYS);
            redisTemplate.opsForList().rightPush(PREHEATED_PRODUCT_KEYS, productKey);
        }
        redisTemplate.expire(PREHEATED_PRODUCT_KEYS, 2, TimeUnit.DAYS);
    }

    private Map<String, String> convertToRedisMap(EventProductDTO dto) {
        Map<String, String> map = new HashMap<>();
        map.put("productId", dto.getProductId());
        map.put("eventId", dto.getEventId());
        map.put("productName", dto.getProductName());
        map.put("description", dto.getDescription());
        map.put("price", dto.getPrice() != null ? dto.getPrice().toString() : "0");
        map.put("stock", String.valueOf(dto.getStock()));
        map.put("startTime", dto.getStartTime().toString());
        map.put("endTime", dto.getEndTime().toString());
        return map;
    }
}
