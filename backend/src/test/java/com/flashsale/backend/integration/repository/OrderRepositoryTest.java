package com.flashsale.backend.integration.repository;

import com.flashsale.backend.entity.Event;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.MemberRepository;
import com.flashsale.backend.repository.OrderRepository;
import com.flashsale.backend.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @description OrderRepository.findByIdForUpdate test against real dev MySQL (AutoConfigureTestDatabase Replace.NONE, no embedded DB in this project) — covers the pessimistic-lock query used to guard the order row during the stock-restore/cancel critical path
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<String> createdOrderIds = new ArrayList<>();
    private final List<String> createdEventIds = new ArrayList<>();
    private final List<String> createdProductIds = new ArrayList<>();
    private final List<String> createdMemberIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (String orderId : createdOrderIds) {
            orderRepository.deleteById(orderId);
        }
        for (String eventId : createdEventIds) {
            eventRepository.deleteById(eventId);
        }
        for (String productId : createdProductIds) {
            productRepository.deleteById(productId);
        }
        for (String memberId : createdMemberIds) {
            memberRepository.deleteById(memberId);
        }
        createdOrderIds.clear();
        createdEventIds.clear();
        createdProductIds.clear();
        createdMemberIds.clear();
    }

    private Member createMember() {
        Member member = new Member();
        member.setMemberEmail(UUID.randomUUID() + "@test.local");
        member.setMemberPwd("hashed-pwd");
        member.setMemberName("test-member");
        Member saved = memberRepository.save(member);
        createdMemberIds.add(saved.getMemberId());
        return saved;
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

    private Event createEvent(Product product) {
        Event event = new Event();
        event.setProductId(product.getProductId());
        event.setPrice(BigDecimal.TEN);
        event.setStock(10);
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(1));
        event.setStatus(1);
        Event saved = eventRepository.save(event);
        createdEventIds.add(saved.getEventId());
        return saved;
    }

    private Order createOrder(Member member, Product product, Event event) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setMemberId(member.getMemberId());
        order.setProductId(product.getProductId());
        order.setEventId(event.getEventId());
        order.setQuantity(1);
        order.setTotalPrice(BigDecimal.TEN);
        order.setStatus("PENDING");
        Order saved = orderRepository.save(order);
        createdOrderIds.add(saved.getOrderId());
        return saved;
    }

    private Order createFullOrderFixture() {
        Member member = createMember();
        Product product = createProduct();
        Event event = createEvent(product);
        return createOrder(member, product, event);
    }

    @Test
    @DisplayName("findByIdForUpdate_existingOrder_returnsOrder")
    void findByIdForUpdate_existingOrder_returnsOrder() {
        Order order = createFullOrderFixture();

        Optional<Order> found = orderRepository.findByIdForUpdate(order.getOrderId());

        assertTrue(found.isPresent());
        assertEquals(order.getOrderId(), found.get().getOrderId());
        assertEquals("PENDING", found.get().getStatus());
    }

    @Test
    @DisplayName("findByIdForUpdate_nonExistentId_returnsEmpty")
    void findByIdForUpdate_nonExistentId_returnsEmpty() {
        Optional<Order> found = orderRepository.findByIdForUpdate(UUID.randomUUID().toString());

        assertTrue(found.isEmpty());
    }

    /**
     * Runs outside the @DataJpaTest-managed transaction (Propagation.NOT_SUPPORTED) so the lock held
     * by findByIdForUpdate's PESSIMISTIC_WRITE query is genuinely visible across two independent,
     * concurrently-running transactions. The first thread holds the row lock for holdMillis; the
     * second thread's call is timed to prove it blocked until the first transaction released the lock
     * — mirroring the real cancel/timeout flow where findByIdForUpdate serializes concurrent
     * stock-restore attempts on the same order.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DisplayName("findByIdForUpdate_concurrentAccess_secondCallerBlocksUntilFirstTransactionEnds")
    void findByIdForUpdate_concurrentAccess_secondCallerBlocksUntilFirstTransactionEnds() throws Exception {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        long holdMillis = 800;

        Member member = transactionTemplate.execute(status -> {
            Member m = new Member();
            m.setMemberEmail(UUID.randomUUID() + "@test.local");
            m.setMemberPwd("hashed-pwd");
            m.setMemberName("test-member");
            return memberRepository.save(m);
        });
        Product product = transactionTemplate.execute(status -> {
            Product p = new Product();
            p.setProductName("test-product-" + UUID.randomUUID());
            p.setDescription("repository test fixture");
            p.setStatus(1);
            return productRepository.save(p);
        });
        Event event = transactionTemplate.execute(status -> {
            Event e = new Event();
            e.setProductId(product.getProductId());
            e.setPrice(BigDecimal.TEN);
            e.setStock(10);
            e.setStartTime(LocalDateTime.now().minusHours(1));
            e.setEndTime(LocalDateTime.now().plusHours(1));
            e.setStatus(1);
            return eventRepository.save(e);
        });
        Order order = transactionTemplate.execute(status -> {
            Order o = new Order();
            o.setOrderId(UUID.randomUUID().toString());
            o.setMemberId(member.getMemberId());
            o.setProductId(product.getProductId());
            o.setEventId(event.getEventId());
            o.setQuantity(1);
            o.setTotalPrice(BigDecimal.TEN);
            o.setStatus("PENDING");
            return orderRepository.save(o);
        });
        String orderId = order.getOrderId();

        CountDownLatch firstLockAcquired = new CountDownLatch(1);
        AtomicLong secondCallStart = new AtomicLong();
        AtomicLong secondCallEnd = new AtomicLong();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = pool.submit(() -> transactionTemplate.execute(status -> {
                orderRepository.findByIdForUpdate(orderId);
                firstLockAcquired.countDown();
                try {
                    Thread.sleep(holdMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }));

            assertTrue(firstLockAcquired.await(5, TimeUnit.SECONDS), "first thread must acquire the lock");
            Thread.sleep(100); // small buffer so the first transaction is firmly holding the lock

            Future<?> second = pool.submit(() -> {
                secondCallStart.set(System.currentTimeMillis());
                transactionTemplate.execute(status -> orderRepository.findByIdForUpdate(orderId));
                secondCallEnd.set(System.currentTimeMillis());
            });

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);

            long secondCallDuration = secondCallEnd.get() - secondCallStart.get();
            assertTrue(secondCallDuration >= holdMillis - 150,
                    "second caller must block until the first transaction releases the row lock, took " + secondCallDuration + "ms");
        } finally {
            pool.shutdown();
            transactionTemplate.execute(status -> {
                orderRepository.deleteById(orderId);
                eventRepository.deleteById(event.getEventId());
                productRepository.deleteById(product.getProductId());
                memberRepository.deleteById(member.getMemberId());
                return null;
            });
        }
    }
}
