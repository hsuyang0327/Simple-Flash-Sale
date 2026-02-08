package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.MemberRegistRequest;
import com.flashsale.backend.dto.request.MemberUpdateRequest;
import com.flashsale.backend.dto.response.MemberResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.security.SecurityUtils;
import com.flashsale.backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @description MemberController RestFull
 * @author Yang-Hsu
 * @date 2026/2/6 下午2:18
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * @description Reist
     * @author Yang-Hsu
     * @date 2026/2/6 下午2:28
     */
    @PostMapping("/api/client/open/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody MemberRegistRequest req) {
        log.info("API: Register member (Client)");
        Member saved = memberService.addMember(req);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(saved)));
    }

    /**
     * @description Get member profile
     * @author Yang-Hsu
     * @date 2026/2/6 下午2:28
     */
    @GetMapping("/api/client/member/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> profile(@PathVariable String id) {
        log.info("API: Get member profile (Client): {}", id);
        SecurityUtils.checkPermission(id);
        Member member = memberService.getMemberById(id);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(member)));
    }

    /**
     * @description  Modify Member information
     * @author Yang-Hsu
     * @date 2026/2/6 下午2:29
     */
    @PutMapping("/api/client/member/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> modify(@PathVariable String id, @RequestBody MemberUpdateRequest req) {
        log.info("API: Modify member (Client): {}", id);
        SecurityUtils.checkPermission(id);
        Member updatedMember = memberService.updateMember(id, req);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(updatedMember)));
    }

    /**
     * @description Get all Member
     * @author Yang-Hsu
     * @date 2026/2/6 下午2:30
     */
    @GetMapping("/api/admin/members")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> list(@PageableDefault(page = 0, size = 10, sort = "memberId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: List all members (Admin)");
        Page<Member> memberPage = memberService.getAllMembers(pageable);
        Page<MemberResponse> responsePage = memberPage.map(this::convertToResponse);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, responsePage));
    }

    /**
     * @description Delete Member
     * @author Yang-Hsu
     * @date 2026/2/6 下午2:30
     */
    @DeleteMapping("/api/admin/members/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String id) {
        log.info("API: Delete member (Admin): {}", id);
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS));
    }

    private MemberResponse convertToResponse(Member member) {
        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .memberEmail(member.getMemberEmail())
                .memberName(member.getMemberName())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
