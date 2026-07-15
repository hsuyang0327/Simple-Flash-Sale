package com.flashsale.backend.mapper;

import com.flashsale.backend.dto.response.EventResponse;
import com.flashsale.backend.entity.Event;
import org.mapstruct.Mapper;

/**
 * @description MapStruct mapper for Event entity to response DTO conversion
 * @author Yang-Hsu
 * @date 2026/7/15
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    /**
     * @description Convert Event entity to admin-facing response DTO
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    EventResponse toResponse(Event event);
}
