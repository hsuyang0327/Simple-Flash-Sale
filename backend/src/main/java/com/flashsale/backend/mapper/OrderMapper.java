package com.flashsale.backend.mapper;

import com.flashsale.backend.dto.response.OrderAdminResponse;
import com.flashsale.backend.dto.response.OrderClientDetailResponse;
import com.flashsale.backend.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @description MapStruct mapper for Order entity to response DTO conversion
 * @author Yang-Hsu
 * @date 2026/7/15
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * @description Convert Order entity to client-facing response DTO using the order's own denormalized productId only, without joining Product
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    OrderClientDetailResponse toClientResponse(Order order);

    /**
     * @description Convert Order entity to client-facing response DTO including productName resolved from the joined Product
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    @Mapping(target = "productId", expression = "java(order.getProduct() != null ? order.getProduct().getProductId() : null)")
    @Mapping(target = "productName", expression = "java(order.getProduct() != null ? order.getProduct().getProductName() : \"Unknown\")")
    OrderClientDetailResponse toClientDetailResponse(Order order);

    /**
     * @description Convert Order entity to admin-facing response DTO including memberName/productName resolved from the joined Member/Product
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    @Mapping(target = "memberId", expression = "java(order.getMember() != null ? order.getMember().getMemberId() : null)")
    @Mapping(target = "memberName", expression = "java(order.getMember() != null ? order.getMember().getMemberName() : \"Unknown\")")
    @Mapping(target = "productId", expression = "java(order.getProduct() != null ? order.getProduct().getProductId() : null)")
    @Mapping(target = "productName", expression = "java(order.getProduct() != null ? order.getProduct().getProductName() : \"Unknown\")")
    OrderAdminResponse toAdminResponse(Order order);
}
