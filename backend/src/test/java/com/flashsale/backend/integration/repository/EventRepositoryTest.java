package com.flashsale.backend.integration.repository;

import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 * @description EventRepository stock-mutation query test against real dev MySQL (AutoConfigureTestDatabase Replace.NONE, no embedded DB in this project) — covers the anti-oversell conditional UPDATE guard in decreaseStock and the increaseStock restore path
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TestEntityManager testEntityManager;

    private final List<String> createdEventIds = new ArrayList<>();
    private final List<String> createdProductIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (String eventId : createdEventIds) {
            eventRepository.deleteById(eventId);
        }
        for (String productId : createdProductIds) {
            productRepository.deleteById(productId);
        }
        createdEventIds.clear();
        createdProductIds.clear();
    }

    private Product createProduct() {
        Product product = new Product();
        product.setProductName("test-product-" + UUID.randomUUID());
        product.setDescription("repository test fixture");
        product.setStatus(1);
        Product saved = productRepository.save(product);
        createdProductIds.add(saved.getProductId());
        return saved;
    }

    private Event createEvent(int stock) {
        Product product = createProduct();
        Event event = new Event();
        event.setProductId(product.getProductId());
        event.setPrice(BigDecimal.TEN);
        event.setStock(stock);
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(1));
        event.setStatus(1);
        Event saved = eventRepository.save(event);
        createdEventIds.add(saved.getEventId());
        return saved;
    }

    @Test
    @DisplayName("decreaseStock_sufficientStock_updatesAndReturnsOne")
    void decreaseStock_sufficientStock_updatesAndReturnsOne() {
        Event event = createEvent(10);

        int affected = eventRepository.decreaseStock(event.getEventId(), 3);

        assertEquals(1, affected);
        testEntityManager.clear(); // decreaseStock is a JPQL bulk UPDATE; clear the L1 cache so the re-fetch below hits the DB
        assertEquals(7, eventRepository.findById(event.getEventId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("decreaseStock_exactStock_updatesToZeroAndReturnsOne")
    void decreaseStock_exactStock_updatesToZeroAndReturnsOne() {
        Event event = createEvent(5);

        int affected = eventRepository.decreaseStock(event.getEventId(), 5);

        assertEquals(1, affected);
        testEntityManager.clear();
        assertEquals(0, eventRepository.findById(event.getEventId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("decreaseStock_insufficientStock_returnsZeroAndStockUnchanged")
    void decreaseStock_insufficientStock_returnsZeroAndStockUnchanged() {
        Event event = createEvent(2);

        int affected = eventRepository.decreaseStock(event.getEventId(), 3);

        assertEquals(0, affected);
        testEntityManager.clear();
        assertEquals(2, eventRepository.findById(event.getEventId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("increaseStock_validId_incrementsStock")
    void increaseStock_validId_incrementsStock() {
        Event event = createEvent(5);

        eventRepository.increaseStock(event.getEventId(), 4);

        testEntityManager.clear();
        assertEquals(9, eventRepository.findById(event.getEventId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("increaseStock_nonExistentId_doesNotThrowAndAffectsNoRow")
    void increaseStock_nonExistentId_doesNotThrowAndAffectsNoRow() {
        String nonExistentId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> eventRepository.increaseStock(nonExistentId, 4));
    }

    /**
     * Runs outside the @DataJpaTest-managed transaction (Propagation.NOT_SUPPORTED) so each
     * concurrent decreaseStock call opens and commits its own real transaction against dev MySQL,
     * proving the "WHERE stock >= qty" conditional UPDATE is atomic at the database level and
     * cannot oversell even without relying on the Redis Lua-script layer. Fixture is created and
     * torn down manually within the test because it never joins a rolled-back transaction.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("decreaseStock_concurrentRequestsExceedStock_neverOversellsAtDatabaseLevel")
    void decreaseStock_concurrentRequestsExceedStock_neverOversellsAtDatabaseLevel() throws Exception {
        // Fixture is created/torn down locally (not via the shared createEvent()/tearDown() tracking
        // lists) because this test genuinely commits outside the @DataJpaTest-managed transaction.
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        int initialStock = 20;
        int threadCount = 60;

        Product product = transactionTemplate.execute(status -> {
            Product p = new Product();
            p.setProductName("test-product-" + UUID.randomUUID());
            p.setDescription("repository test fixture");
            p.setStatus(1);
            return productRepository.save(p);
        });
        Event seededEvent = transactionTemplate.execute(status -> {
            Event event = new Event();
            event.setProductId(product.getProductId());
            event.setPrice(BigDecimal.TEN);
            event.setStock(initialStock);
            event.setStartTime(LocalDateTime.now().minusHours(1));
            event.setEndTime(LocalDateTime.now().plusHours(1));
            event.setStatus(1);
            return eventRepository.save(event);
        });
        String eventId = seededEvent.getEventId();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                tasks.add(() -> transactionTemplate.execute(status -> eventRepository.decreaseStock(eventId, 1)));
            }
            List<Future<Integer>> futures = pool.invokeAll(tasks, 15, TimeUnit.SECONDS);

            long successCount = 0;
            for (Future<Integer> future : futures) {
                if (future.get() == 1) {
                    successCount++;
                }
            }

            assertEquals(initialStock, successCount, "success count must equal the initial stock exactly");
            Integer remainingStock = transactionTemplate.execute(status ->
                    eventRepository.findById(eventId).orElseThrow().getStock());
            assertEquals(0, remainingStock);
        } finally {
            pool.shutdown();
            transactionTemplate.execute(status -> {
                eventRepository.deleteById(eventId);
                productRepository.deleteById(product.getProductId());
                return null;
            });
        }
    }
}
