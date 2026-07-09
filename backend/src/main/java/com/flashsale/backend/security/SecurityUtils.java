package com.flashsale.backend.security;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @description Security Context Helper Tool
 * @author Yang-Hsu
 * @date 2026/2/6
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * @description Retrieve the current authenticated member ID from SecurityContext
     * @author Yang-Hsu
     * @date 2026/7/9
     */
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

    /**
     * @description Throw exception if current user does not match target resource owner
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public static void checkPermission(String targetId) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(targetId)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }
}
