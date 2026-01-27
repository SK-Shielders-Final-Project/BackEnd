package org.rookies.zdme.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {
    // 현재 로그인한 사용자의 ID (또는 Username)를 반환하는 메서드
    public static String getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        // 기본 UserDetails를 쓴다면 username(email)이 반환됨
        return authentication.getName();
    }
}
