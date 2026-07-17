package com.flashsale.backend.integration.service;

import com.flashsale.backend.service.RedisStockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @description RedisStockServiceImpl concurrency correctness test against a real Redis instance (DB0) — verifies the Lua-script-based decrease/increase never oversells or loses updates under concurrent access
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@SpringBootTest
class RedisStockServiceTest {

    @Autowired
    private RedisStockService redisStockService;

    @Autowired
    @Qualifier("redisTemplateDb0")
    private RedisTemplate<String, Object> redisTemplate;

    private String productId;
    private String key;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID().toString();
        key = "productId:" + productId;
    }

    @AfterEach
    void tearDown() {
        redisTemplate.delete(key);
    }

    private void seedStock(int stock) {
        redisTemplate.opsForHash().put(key, "stock", String.valueOf(stock));
    }

    private String currentStock() {
        Object stock = redisTemplate.opsForHash().get(key, "stock");
        return stock == null ? null : stock.toString();
    }

    @Test
    @DisplayName("decreaseStock_singleThread_returnsRemainingStock")
    void decreaseStock_singleThread_returnsRemainingStock() {
        seedStock(10);

        long result = redisStockService.decreaseStock(productId, 3);

        assertEquals(7L, result);
        assertEquals("7", currentStock());
    }

    @Test
    @DisplayName("decreaseStock_concurrentRequestsExceedStock_neverOversells")
    void decreaseStock_concurrentRequestsExceedStock_neverOversells() throws Exception {
        int initialStock = 50;
        int threadCount = 150;
        seedStock(initialStock);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                tasks.add(() -> redisStockService.decreaseStock(productId, 1));
            }
            List<Future<Long>> futures = pool.invokeAll(tasks, 10, TimeUnit.SECONDS);

            long successCount = 0;
            long failCount = 0;
            for (Future<Long> future : futures) {
                long result = future.get();
                if (result >= 0) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            assertEquals(initialStock, successCount, "success count must equal the initial stock exactly");
            assertEquals(threadCount - initialStock, failCount);
            assertEquals("0", currentStock());
        } finally {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("decreaseStock_concurrentVariableQuantities_neverExceedsInitialStock")
    void decreaseStock_concurrentVariableQuantities_neverExceedsInitialStock() throws Exception {
        int initialStock = 25;
        int threadCount = 30;
        seedStock(initialStock);

        List<Integer> quantities = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            quantities.add((i % 3) + 1); // quantities cycle through 1, 2, 3
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int quantity : quantities) {
                tasks.add(() -> redisStockService.decreaseStock(productId, quantity));
            }
            List<Future<Long>> futures = pool.invokeAll(tasks, 10, TimeUnit.SECONDS);

            long successfullyDeducted = 0;
            for (int i = 0; i < futures.size(); i++) {
                long result = futures.get(i).get();
                if (result >= 0) {
                    successfullyDeducted += quantities.get(i);
                }
            }

            assertTrue(successfullyDeducted <= initialStock, "total deducted quantity must never exceed initial stock");
            assertEquals(String.valueOf(initialStock - successfullyDeducted), currentStock());
        } finally {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("decreaseStock_keyNotExist_returnsMinusOne")
    void decreaseStock_keyNotExist_returnsMinusOne() {
        long result = redisStockService.decreaseStock(productId, 1);

        assertEquals(-1L, result);
    }

    @Test
    @DisplayName("decreaseStock_stockAlreadyZero_returnsMinusThree")
    void decreaseStock_stockAlreadyZero_returnsMinusThree() {
        seedStock(0);

        long result = redisStockService.decreaseStock(productId, 1);

        assertEquals(-3L, result);
    }

    @Test
    @DisplayName("decreaseStock_requestedQuantityExceedsStock_returnsMinusTwo")
    void decreaseStock_requestedQuantityExceedsStock_returnsMinusTwo() {
        seedStock(5);

        long result = redisStockService.decreaseStock(productId, 10);

        assertEquals(-2L, result);
        assertEquals("5", currentStock(), "stock must remain unchanged when the reduction is rejected");
    }

    @Test
    @DisplayName("increaseStock_concurrentRequests_sumsWithoutLostUpdates")
    void increaseStock_concurrentRequests_sumsWithoutLostUpdates() throws Exception {
        int threadCount = 50;
        int quantityPerThread = 2;
        seedStock(0);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                tasks.add(() -> {
                    redisStockService.increaseStock(productId, quantityPerThread);
                    return null;
                });
            }
            List<Future<Void>> futures = pool.invokeAll(tasks, 10, TimeUnit.SECONDS);
            for (Future<Void> future : futures) {
                future.get();
            }

            assertEquals(String.valueOf(threadCount * quantityPerThread), currentStock());
        } finally {
            pool.shutdown();
        }
    }

    @Test
    @DisplayName("increaseStock_keyNotExist_doesNotThrowAndLeavesKeyAbsent")
    void increaseStock_keyNotExist_doesNotThrowAndLeavesKeyAbsent() {
        assertDoesNotThrow(() -> redisStockService.increaseStock(productId, 5));

        assertNull(currentStock(), "the Lua script must not create the key when it did not already exist");
    }
}
