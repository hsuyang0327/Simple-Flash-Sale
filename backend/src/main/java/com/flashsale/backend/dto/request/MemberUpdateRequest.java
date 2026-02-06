package com.flashsale.backend.dto.request;


import lombok.Data;

/**
 * @description MemberUpdateRequest
 * @author Yang-Hsu
 * @date 2026/2/6 下午2:31
 */
@Data
public class MemberUpdateRequest {
    private String memberName;
    private String memberPwd;
}
