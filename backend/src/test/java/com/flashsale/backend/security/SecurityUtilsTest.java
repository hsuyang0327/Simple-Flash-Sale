package com.flashsale.backend.security;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @description SecurityUtilsTest(By using real SecurityContextHolder, no Spring context needed)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserId_noAuthentication_returnsNull() {
        SecurityContextHolder.clearContext();

        assertNull(SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_authenticationWithNullDetails_returnsNull() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getDetails()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertNull(SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_detailsIsString_returnsMemberId() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user1", null, java.util.List.of());
        authentication.setDetails("member-123");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("member-123", SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_detailsNotString_returnsNull() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user1", null, java.util.List.of());
        authentication.setDetails(new WebAuthenticationDetails(mock(jakarta.servlet.http.HttpServletRequest.class)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertNull(SecurityUtils.getCurrentUserId());
    }

    @Test
    void checkPermission_matchingId_doesNotThrow() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user1", null, java.util.List.of());
        authentication.setDetails("member-123");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertDoesNotThrow(() -> SecurityUtils.checkPermission("member-123"));
    }

    @Test
    void checkPermission_mismatchedId_throwsBusinessException() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user1", null, java.util.List.of());
        authentication.setDetails("member-123");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> SecurityUtils.checkPermission("someone-else"));
        assertEquals(ResultCode.TOKEN_INVALID, ex.getResultCode());
    }

    @Test
    void checkPermission_noCurrentUser_throwsBusinessException() {
        SecurityContextHolder.clearContext();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> SecurityUtils.checkPermission("member-123"));
        assertEquals(ResultCode.TOKEN_INVALID, ex.getResultCode());
    }
}
