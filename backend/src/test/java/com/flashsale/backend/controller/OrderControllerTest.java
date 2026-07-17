package com.flashsale.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.backend.dto.request.OrderRequest;
import com.flashsale.backend.dto.response.OrderClientDetailResponse;
import com.flashsale.backend.entity.Order;
import com.flashsale.backend.config.SecurityConfig;
import com.flashsale.backend.exception.GlobalExceptionHandler;
import com.flashsale.backend.mapper.OrderMapper;
import com.flashsale.backend.security.AuthJwtFilter;
import com.flashsale.backend.security.JwtUtils;
import com.flashsale.backend.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @description OrderControllerTest — verifies createOrder always overrides client-supplied
 * memberId with the JWT-authenticated member ID, preventing order spoofing
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    /**
     * Spring Boot's WebMvcTest resolves its context root via the nearest
     * {@code @SpringBootConfiguration} starting from this test's own package before
     * walking up to {@code BackendApplication}. Declaring one here keeps this slice from
     * pulling in {@code @EnableJpaAuditing} (which needs a full JPA metamodel that a
     * web-only slice doesn't have) — test-only, no production code touched.
     * Beans are wired via explicit @Import (not @ComponentScan) so unrelated controllers
     * like AuthController don't get pulled in alongside OrderController.
     */
    @SpringBootConfiguration
    @Import({OrderController.class, SecurityConfig.class, AuthJwtFilter.class, GlobalExceptionHandler.class})
    static class TestConfig {
    }

    private static final String AUTHENTICATED_MEMBER_ID = "member-real-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderMapper orderMapper;

    // AuthJwtFilter is a Filter, so @WebMvcTest pulls it into the slice and needs this
    // dependency satisfied even though addFilters = false skips running it.
    @MockitoBean
    private JwtUtils jwtUtils;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String memberId) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test-user", null, List.of());
        authentication.setDetails(memberId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Order stubOrder() {
        Order order = new Order();
        order.setOrderId("order-001");
        order.setMemberId(AUTHENTICATED_MEMBER_ID);
        order.setProductId("product-001");
        order.setEventId("event-001");
        order.setQuantity(1);
        order.setTotalPrice(BigDecimal.TEN);
        order.setStatus("PENDING");
        return order;
    }

    @Test
    void createOrder_clientSuppliedSpoofedMemberId_isOverriddenByAuthenticatedMemberId() throws Exception {
        authenticateAs(AUTHENTICATED_MEMBER_ID);
        OrderRequest request = new OrderRequest();
        request.setMemberId("attacker-victim-id"); // attempted spoofing via request body
        request.setEventId("event-001");
        request.setQuantity(1);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(stubOrder());
        when(orderService.convertToClientResponse(any(Order.class)))
                .thenReturn(OrderClientDetailResponse.builder().orderId("order-001").build());

        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<OrderRequest> captor = org.mockito.ArgumentCaptor.forClass(OrderRequest.class);
        verify(orderService).createOrder(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(AUTHENTICATED_MEMBER_ID, captor.getValue().getMemberId());
    }

    @Test
    void createOrder_requestBodyWithoutMemberId_setsAuthenticatedMemberId() throws Exception {
        authenticateAs(AUTHENTICATED_MEMBER_ID);
        OrderRequest request = new OrderRequest();
        request.setEventId("event-001");
        request.setQuantity(1);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(stubOrder());
        when(orderService.convertToClientResponse(any(Order.class)))
                .thenReturn(OrderClientDetailResponse.builder().orderId("order-001").build());

        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<OrderRequest> captor = org.mockito.ArgumentCaptor.forClass(OrderRequest.class);
        verify(orderService).createOrder(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(AUTHENTICATED_MEMBER_ID, captor.getValue().getMemberId());
    }

    @Test
    void createOrder_success_returnsOrderInResponseEnvelope() throws Exception {
        authenticateAs(AUTHENTICATED_MEMBER_ID);
        OrderRequest request = new OrderRequest();
        request.setEventId("event-001");
        request.setQuantity(2);

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(stubOrder());
        when(orderService.convertToClientResponse(any(Order.class)))
                .thenReturn(OrderClientDetailResponse.builder()
                        .orderId("order-001")
                        .productId("product-001")
                        .quantity(2)
                        .totalPrice(BigDecimal.TEN)
                        .status("PENDING")
                        .build());

        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value("order-001"))
                .andExpect(jsonPath("$.data.quantity").value(2));
    }

    @Test
    void createOrder_blankEventId_returns400ValidationError() throws Exception {
        authenticateAs(AUTHENTICATED_MEMBER_ID);
        OrderRequest request = new OrderRequest();
        request.setEventId("");
        request.setQuantity(1);

        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4617));
    }

    @Test
    void createOrder_quantityLessThanOne_returns400ValidationError() throws Exception {
        authenticateAs(AUTHENTICATED_MEMBER_ID);
        OrderRequest request = new OrderRequest();
        request.setEventId("event-001");
        request.setQuantity(0);

        mockMvc.perform(post("/api/client/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4616));
    }
}
