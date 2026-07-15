package com.flashsale.backend.mapper;

import com.flashsale.backend.dto.response.ProductAdminResponse;
import com.flashsale.backend.dto.response.ProductClientResponse;
import com.flashsale.backend.entity.Product;
import org.mapstruct.Mapper;

/**
 * @description MapStruct mapper for Product entity to response DTO conversion
 * @author Yang-Hsu
 * @date 2026/7/15
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * @description Convert Product entity to client-facing response DTO
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    ProductClientResponse toClientResponse(Product product);

    /**
     * @description Convert Product entity to admin-facing response DTO
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    ProductAdminResponse toAdminResponse(Product product);
}
