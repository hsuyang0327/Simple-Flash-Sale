package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.DashboardStockResponse;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @description DashboardServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplateDb0;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @SuppressWarnings("unchecked")
    private HashOperations<String, Object, Object> mockHashOperations() {
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplateDb0.opsForHash()).thenReturn(hashOperations);
        return hashOperations;
    }

    @Test
    @DisplayName("查無 Key 時回傳空清單")
    void getStocks_noKeys_returnsEmptyList() {
        when(redisTemplateDb0.keys("productId:*")).thenReturn(Set.of());

        List<DashboardStockResponse> result = dashboardService.getStocks();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("正常比對 Redis 庫存與 MySQL 庫存")
    void getStocks_validHash_returnsMatchedRedisAndDbStock() {
        String productId = UUID.randomUUID().toString();
        String eventId = UUID.randomUUID().toString();
        String key = "productId:" + productId;

        Map<Object, Object> hash = new HashMap<>();
        hash.put("productId", productId);
        hash.put("productName", "Product A");
        hash.put("eventId", eventId);
        hash.put("stock", "15");

        Event event = new Event();
        event.setEventId(eventId);
        event.setStock(20);

        when(redisTemplateDb0.keys("productId:*")).thenReturn(Set.of(key));
        HashOperations<String, Object, Object> hashOperations = mockHashOperations();
        when(hashOperations.entries(key)).thenReturn(hash);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        List<DashboardStockResponse> result = dashboardService.getStocks();

        assertEquals(1, result.size());
        DashboardStockResponse response = result.get(0);
        assertEquals(productId, response.getProductId());
        assertEquals("Product A", response.getProductName());
        assertEquals(15, response.getRedisStock());
        assertEquals(20, response.getDbStock());
    }

    @Test
    @DisplayName("缺少 productId 或 stock 欄位時跳過該筆")
    void getStocks_missingProductIdOrStock_skipsEntry() {
        String key = "productId:incomplete";
        Map<Object, Object> hash = new HashMap<>();
        hash.put("productName", "Incomplete Product");

        when(redisTemplateDb0.keys("productId:*")).thenReturn(Set.of(key));
        HashOperations<String, Object, Object> hashOperations = mockHashOperations();
        when(hashOperations.entries(key)).thenReturn(hash);

        List<DashboardStockResponse> result = dashboardService.getStocks();

        assertTrue(result.isEmpty());
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("stock 欄位格式錯誤時跳過該筆")
    void getStocks_invalidStockNumberFormat_skipsEntry() {
        String productId = UUID.randomUUID().toString();
        String key = "productId:" + productId;
        Map<Object, Object> hash = new HashMap<>();
        hash.put("productId", productId);
        hash.put("stock", "not-a-number");

        when(redisTemplateDb0.keys("productId:*")).thenReturn(Set.of(key));
        HashOperations<String, Object, Object> hashOperations = mockHashOperations();
        when(hashOperations.entries(key)).thenReturn(hash);

        List<DashboardStockResponse> result = dashboardService.getStocks();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("eventId 為 null 或查無事件時 dbStock 為 0")
    void getStocks_eventIdNullOrEventNotFound_dbStockZero() {
        String productId = UUID.randomUUID().toString();
        String key = "productId:" + productId;
        Map<Object, Object> hash = new HashMap<>();
        hash.put("productId", productId);
        hash.put("stock", "5");
        // no eventId

        when(redisTemplateDb0.keys("productId:*")).thenReturn(Set.of(key));
        HashOperations<String, Object, Object> hashOperations = mockHashOperations();
        when(hashOperations.entries(key)).thenReturn(hash);

        List<DashboardStockResponse> result = dashboardService.getStocks();

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getDbStock());
        verifyNoInteractions(eventRepository);
    }
}
