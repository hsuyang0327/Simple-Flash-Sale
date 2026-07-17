package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.request.MemberRegistRequest;
import com.flashsale.backend.dto.request.MemberUpdateRequest;
import com.flashsale.backend.entity.Member;
import com.flashsale.backend.exception.BusinessException;
import com.flashsale.backend.repository.MemberRepository;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @description MemberServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("註冊成功 - 新的 Email")
    void addMember_newEmail_savesAndReturnsMember() {
        MemberRegistRequest req = new MemberRegistRequest();
        req.setMemberEmail("new@example.com");
        req.setMemberPwd("rawPwd");
        req.setMemberName("New Member");

        when(memberRepository.existsByMemberEmail(req.getMemberEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getMemberPwd())).thenReturn("encodedPwd");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.addMember(req);

        assertNotNull(result);
        assertEquals("encodedPwd", result.getMemberPwd());
        assertEquals(req.getMemberEmail(), result.getMemberEmail());
    }

    @Test
    @DisplayName("註冊失敗 - Email 已存在")
    void addMember_emailAlreadyExists_throwsBusinessException() {
        MemberRegistRequest req = new MemberRegistRequest();
        req.setMemberEmail("exists@example.com");

        when(memberRepository.existsByMemberEmail(req.getMemberEmail())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.addMember(req));

        assertEquals(ResultCode.MEMBER_ALREADY_EXISTS, exception.getResultCode());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("查詢會員失敗 - 找不到會員")
    void getMemberById_notFound_throwsBusinessException() {
        String memberId = UUID.randomUUID().toString();
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.getMemberById(memberId));

        assertEquals(ResultCode.MEMBER_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("更新會員成功")
    void updateMember_validRequest_updatesAndReturnsMember() {
        String memberId = UUID.randomUUID().toString();
        Member existing = new Member();
        existing.setMemberId(memberId);
        existing.setMemberName("Old Name");

        MemberUpdateRequest req = new MemberUpdateRequest();
        req.setMemberName("New Name");
        req.setMemberPwd("newRawPwd");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newRawPwd")).thenReturn("newEncodedPwd");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member result = memberService.updateMember(memberId, req);

        assertEquals("New Name", result.getMemberName());
        assertEquals("newEncodedPwd", result.getMemberPwd());
    }

    @Test
    @DisplayName("更新會員失敗 - 樂觀鎖衝突")
    void updateMember_optimisticLockFailure_throwsMemberIsUpdatedByOthers() {
        String memberId = UUID.randomUUID().toString();
        Member existing = new Member();
        existing.setMemberId(memberId);

        MemberUpdateRequest req = new MemberUpdateRequest();
        req.setMemberName("New Name");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Member.class, memberId));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.updateMember(memberId, req));

        assertEquals(ResultCode.MEMBER_IS_UPDATED_BY_OTHERS, exception.getResultCode());
    }

    @Test
    @DisplayName("刪除會員失敗 - 找不到會員")
    void deleteMember_notFound_throwsBusinessException() {
        String memberId = UUID.randomUUID().toString();
        when(memberRepository.existsById(memberId)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.deleteMember(memberId));

        assertEquals(ResultCode.MEMBER_NOT_FOUND, exception.getResultCode());
        verify(memberRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("刪除會員成功")
    void deleteMember_exists_deletesMember() {
        String memberId = UUID.randomUUID().toString();
        when(memberRepository.existsById(memberId)).thenReturn(true);

        memberService.deleteMember(memberId);

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    @DisplayName("查詢所有會員分頁")
    void getAllMembers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Member member = new Member();
        member.setMemberId(UUID.randomUUID().toString());
        Page<Member> expectedPage = new PageImpl<>(List.of(member), pageable, 1);

        when(memberRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Member> result = memberService.getAllMembers(pageable);

        assertEquals(1, result.getTotalElements());
    }
}
