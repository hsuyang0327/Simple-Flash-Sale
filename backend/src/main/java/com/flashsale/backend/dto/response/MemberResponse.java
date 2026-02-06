package com.flashsale.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @description MemberResponse
 * @author Yang-Hsu
 * @date 2026/2/6 下午2:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponse {
    private String memberId;
    private String memberEmail;
    private String memberName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
