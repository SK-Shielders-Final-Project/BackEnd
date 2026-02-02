package org.rookies.zdme.security;

public class SecurityConstants {

    public static final String[] PUBLIC_URLS = {
            "/api/user/auth/login",
            "/api/user/auth/signup",
            "/api/admin/auth/login",
            "/api/.well-known/jwks.json",
            "/api/auth/password-reset/**",
            "/api/auth/refresh",
            "/api/map/bikes-nearby", // 자전거 위치 정보 API 허용
            "/error"
    };
}
