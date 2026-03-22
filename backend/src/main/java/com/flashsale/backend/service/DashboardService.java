package com.flashsale.backend.service;

import com.flashsale.backend.dto.response.DashboardStockResponse;
import com.flashsale.backend.entity.Event;
import com.flashsale.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @description DashboardService — reads Redis DB0 stock data for admin dashboard
 * @author Yang-Hsu
 * @date 2026/4/2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    @Qualifier("redisTemplateDb0")
    private final RedisTemplate<String, Object> redisTemplateDb0;

    private final EventRepository eventRepository;

    public List<DashboardStockResponse> getStocks() {
        Set<String> keys = redisTemplateDb0.keys("productId:*");
        List<DashboardStockResponse> result = new ArrayList<>();

        if (keys == null || keys.isEmpty()) {
            return result;
        }

        for (String key : keys) {
            Map<Object, Object> hash = redisTemplateDb0.opsForHash().entries(key);
            if (hash == null || hash.isEmpty()) continue;

            String productId   = (String) hash.get("productId");
            String productName = (String) hash.get("productName");
            String eventId     = (String) hash.get("eventId");
            String stockStr    = (String) hash.get("stock");

            if (productId == null || stockStr == null) continue;

            int redisStock;
            try {
                redisStock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse stock value '{}' for key {}", stockStr, key);
                continue;
            }

            int dbStock = 0;
            if (eventId != null) {
                Optional<Event> eventOpt = eventRepository.findById(eventId);
                if (eventOpt.isPresent()) {
                    dbStock = eventOpt.get().getStock();
                }
            }

            result.add(DashboardStockResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .eventId(eventId)
                    .redisStock(redisStock)
                    .dbStock(dbStock)
                    .build());
        }

        return result;
    }
}
