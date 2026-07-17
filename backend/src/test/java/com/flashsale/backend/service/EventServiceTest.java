package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.EventRequest;
import com.flashsale.backend.dto.response.EventProductDTO;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @description EventServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ProductService productService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    @DisplayName("依商品編號查詢活動分頁")
    void getEventsByProductId_returnsPage() {
        String productId = UUID.randomUUID().toString();
        Pageable pageable = PageRequest.of(0, 10);
        Event event = new Event();
        event.setEventId(UUID.randomUUID().toString());
        Page<Event> expectedPage = new PageImpl<>(List.of(event), pageable, 1);

        when(eventRepository.findByProductId(productId, pageable)).thenReturn(expectedPage);

        Page<Event> result = eventService.getEventsByProductId(productId, pageable);

        assertEquals(1, result.getTotalElements());
        verify(eventRepository, times(1)).findByProductId(productId, pageable);
    }

    @Test
    @DisplayName("建立活動成功")
    void createEvent_validRequest_savesAndReturnsEvent() {
        String productId = UUID.randomUUID().toString();
        EventRequest request = new EventRequest();
        request.setProductId(productId);
        request.setPrice(new BigDecimal("100"));
        request.setStock(10);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(2));
        request.setStatus(0);

        when(productService.getProductById(productId)).thenReturn(new Product());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(request);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(productService, times(1)).getProductById(productId);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("查詢活動失敗 - 找不到活動")
    void getEventById_notFound_throwsBusinessException() {
        String eventId = UUID.randomUUID().toString();
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eventService.getEventById(eventId));

        assertEquals(ResultCode.EVENT_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("更新活動失敗 - 活動已結束")
    void updateEvent_alreadyEnded_throwsEventExpired() {
        String eventId = UUID.randomUUID().toString();
        Event event = new Event();
        event.setEventId(eventId);
        event.setStartTime(LocalDateTime.now().minusDays(2));
        event.setEndTime(LocalDateTime.now().minusDays(1));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eventService.updateEvent(eventId, new EventRequest()));

        assertEquals(ResultCode.EVENT_EXPIRED, exception.getResultCode());
    }

    @Test
    @DisplayName("更新活動失敗 - 活動已開始")
    void updateEvent_alreadyStarted_throwsEventAlreadyStarted() {
        String eventId = UUID.randomUUID().toString();
        Event event = new Event();
        event.setEventId(eventId);
        event.setStartTime(LocalDateTime.now().minusMinutes(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eventService.updateEvent(eventId, new EventRequest()));

        assertEquals(ResultCode.EVENT_ALREADY_STARTED, exception.getResultCode());
    }

    @Test
    @DisplayName("更新活動成功")
    void updateEvent_validRequest_updatesAndReturnsEvent() {
        String eventId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        Event event = new Event();
        event.setEventId(eventId);
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(2));

        EventRequest request = new EventRequest();
        request.setProductId(productId);
        request.setPrice(new BigDecimal("200"));
        request.setStock(5);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(3));
        request.setStatus(0);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(productService.getProductById(productId)).thenReturn(new Product());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(eventId, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("200"), result.getPrice());
        assertEquals(5, result.getStock());
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    @DisplayName("刪除活動失敗 - 活動已開始")
    void deleteEvent_alreadyStarted_throwsEventAlreadyStarted() {
        String eventId = UUID.randomUUID().toString();
        Event event = new Event();
        event.setEventId(eventId);
        event.setStartTime(LocalDateTime.now().minusMinutes(1));
        event.setEndTime(LocalDateTime.now().plusDays(1));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> eventService.deleteEvent(eventId));

        assertEquals(ResultCode.EVENT_ALREADY_STARTED, exception.getResultCode());
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("刪除活動成功 - 尚未開始")
    void deleteEvent_notStarted_deletesEvent() {
        String eventId = UUID.randomUUID().toString();
        Event event = new Event();
        event.setEventId(eventId);
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(2));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEvent(eventId);

        verify(eventRepository, times(1)).delete(event);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("預熱今日活動 - 寫入 Redis Hash 與 List")
    void preloadEventsForToday_writesRedisHashAndList() {
        EventProductDTO dto = new EventProductDTO(
                UUID.randomUUID().toString(), "Product A", "desc",
                UUID.randomUUID().toString(), new BigDecimal("99.9"), 10,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        when(eventRepository.findPreheatEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(dto));

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        ListOperations<String, Object> listOperations = mock(ListOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        eventService.preloadEventsForToday();

        String expectedProductKey = "productId:" + dto.getProductId();
        verify(redisTemplate, times(1)).delete("preheated_product_keys");
        verify(redisTemplate, times(1)).delete(expectedProductKey);
        verify(hashOperations, times(1)).putAll(eq(expectedProductKey), anyMap());
        verify(redisTemplate, times(1)).expire(expectedProductKey, 2, TimeUnit.DAYS);
        verify(listOperations, times(1)).rightPush(eq("preheated_product_keys"), eq(expectedProductKey));
        verify(redisTemplate, times(1)).expire("preheated_product_keys", 2, TimeUnit.DAYS);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("預熱明日活動 - 寫入 Redis Hash 與 List")
    void preloadEventsForTomorrow_writesRedisHashAndList() {
        EventProductDTO dto = new EventProductDTO(
                UUID.randomUUID().toString(), "Product B", "desc",
                UUID.randomUUID().toString(), new BigDecimal("50"), 20,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        when(eventRepository.findPreheatEvents(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(dto));

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        ListOperations<String, Object> listOperations = mock(ListOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        eventService.preloadEventsForTomorrow();

        String expectedProductKey = "productId:" + dto.getProductId();
        verify(hashOperations, times(1)).putAll(eq(expectedProductKey), anyMap());
        verify(listOperations, times(1)).rightPush(eq("preheated_product_keys"), eq(expectedProductKey));
    }
}
