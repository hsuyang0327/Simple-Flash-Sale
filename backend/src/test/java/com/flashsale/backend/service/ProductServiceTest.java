package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.ProductRequest;
import com.flashsale.backend.entity.Product;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.EventRepository;
import com.flashsale.backend.repository.ProductRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @description ProductServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    @DisplayName("查詢所有商品分頁")
    void getAllProducts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setProductId(UUID.randomUUID().toString());
        Page<Product> expectedPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.getAllProducts(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("查詢商品失敗 - 找不到商品")
    void getProductById_notFound_throwsBusinessException() {
        String productId = UUID.randomUUID().toString();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.getProductById(productId));

        assertEquals(ResultCode.PRODUCT_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("建立商品成功")
    void createProduct_validRequest_savesAndReturnsProduct() {
        ProductRequest request = new ProductRequest();
        request.setProductName("New Product");
        request.setDescription("desc");
        request.setStatus(0);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("New Product", result.getProductName());
        assertEquals(0, result.getStatus());
    }

    @Test
    @DisplayName("上架失敗 - 沒有進行中活動")
    void updateProduct_toShelfNoActiveEvent_throwsProductNoEvent() {
        String productId = UUID.randomUUID().toString();
        ProductRequest request = new ProductRequest();
        request.setStatus(1);

        when(eventRepository.countActiveEventsByProductId(productId)).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId, request));

        assertEquals(ResultCode.PRODUCT_NO_EVENT, exception.getResultCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("上架失敗 - 進行中活動數量異常")
    void updateProduct_toShelfDuplicatedActiveEvents_throwsProductEventDuplicated() {
        String productId = UUID.randomUUID().toString();
        ProductRequest request = new ProductRequest();
        request.setStatus(1);

        when(eventRepository.countActiveEventsByProductId(productId)).thenReturn(2L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productService.updateProduct(productId, request));

        assertEquals(ResultCode.PRODUCT_EVENT_DUPLICATED, exception.getResultCode());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("上架成功 - 恰好一個進行中活動")
    void updateProduct_toShelfOneActiveEvent_updatesSuccessfully() {
        String productId = UUID.randomUUID().toString();
        Product existing = new Product();
        existing.setProductId(productId);
        existing.setProductName("Old Name");
        existing.setStatus(0);

        ProductRequest request = new ProductRequest();
        request.setStatus(1);
        request.setProductName("Updated Name");

        when(eventRepository.countActiveEventsByProductId(productId)).thenReturn(1L);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(productId, request);

        assertEquals(1, result.getStatus());
        assertEquals("Updated Name", result.getProductName());
    }

    @Test
    @DisplayName("刪除商品成功 - 先刪除關聯活動再刪除商品")
    void deleteProduct_deletesEventsThenProduct() {
        String productId = UUID.randomUUID().toString();
        Product product = new Product();
        product.setProductId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(eventRepository, times(1)).deleteByProductId(productId);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    @DisplayName("依名稱搜尋商品")
    void searchProducts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = new Product();
        product.setProductName("Keyword Product");
        Page<Product> expectedPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findByProductNameContaining("Keyword", pageable)).thenReturn(expectedPage);

        Page<Product> result = productService.searchProducts("Keyword", pageable);

        assertEquals(1, result.getTotalElements());
    }
}
