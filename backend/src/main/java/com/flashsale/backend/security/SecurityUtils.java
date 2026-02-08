package com.flashsale.backend.security;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @description Security Context Helper Tool
 * @author Yang-Hsu
 * @date 2026/2/6 下午2:51
 */
public class SecurityUtils {

    private SecurityUtils() {}

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof String) {
            return (String) details;
        }
        // If details is not a String (e.g., WebAuthenticationDetails), we can't get the UUID from it.
        return null;
    }

    public static void checkPermission(String targetId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(targetId)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }
}
