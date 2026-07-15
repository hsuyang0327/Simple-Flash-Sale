package com.flashsale.backend.mapper;

import com.flashsale.backend.dto.response.MemberResponse;
import com.flashsale.backend.entity.Member;
import org.mapstruct.Mapper;

/**
 * @description MapStruct mapper for Member entity to response DTO conversion
 * @author Yang-Hsu
 * @date 2026/7/15
 */
@Mapper(componentModel = "spring")
public interface MemberMapper {

    /**
     * @description Convert Member entity to client-facing response DTO
     * @author Yang-Hsu
     * @date 2026/7/15
     */
    MemberResponse toResponse(Member member);
}
