package com.flashsale.backend.controller;

import com.flashsale.backend.common.ApiResponse;
import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.MemberRegistRequest;
import com.flashsale.backend.dto.request.MemberUpdateRequest;
import com.flashsale.backend.dto.response.MemberResponse;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.security.SecurityUtils;
import com.flashsale.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Member Management", description = "APIs for member registration and profile management.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "Register New Member", description = "Allows a new user to register. This is a public endpoint.")
    @PostMapping("/api/client/open/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(@Valid @RequestBody MemberRegistRequest req) {
        log.info("API: Register member (Client)");
        Member saved = memberService.addMember(req);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(saved)));
    }

    @Operation(summary = "Get Member Profile", description = "Retrieves the profile of the currently authenticated user. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/api/client/member/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> profile(
            @Parameter(description = "ID of the member to retrieve") @PathVariable String id) {
        log.info("API: Get member profile (Client): {}", id);
        SecurityUtils.checkPermission(id);
        Member member = memberService.getMemberById(id);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(member)));
    }

    @Operation(summary = "Update Member Profile", description = "Updates the profile of the currently authenticated user. Requires JWT authentication.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/client/member/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> modify(
            @Parameter(description = "ID of the member to update") @PathVariable String id,
            @RequestBody MemberUpdateRequest req) {
        log.info("API: Modify member (Client): {}", id);
        SecurityUtils.checkPermission(id);
        Member updatedMember = memberService.updateMember(id, req);
        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, convertToResponse(updatedMember)));
    }

    @Operation(summary = "List All Members (Admin)", description = "Retrieves a paginated list of all members. Requires admin privileges.")
    @GetMapping("/api/admin/members")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> list(
            @Parameter(description = "Pagination information") @PageableDefault(page = 0, size = 10, sort = "memberId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API: List all members (Admin)");
        Page<Member> memberPage = memberService.getAllMembers(pageable);
        Page<MemberResponse> responsePage = memberPage.map(this::convertToResponse);

        return ResponseEntity.ok(new ApiResponse<>(ResultCode.SUCCESS, responsePage));
    }

    @Operation(summary = "Delete Member (Admin)", description = "Deletes a member by their ID. Requires admin privileges.")
    @DeleteMapping("/api/admin/members/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @Parameter(description = "ID of the member to delete") @PathVariable String id) {
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
