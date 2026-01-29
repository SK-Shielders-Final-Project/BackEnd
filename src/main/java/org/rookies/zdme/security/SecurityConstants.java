package org.rookies.zdme.security;

public class SecurityConstants {

    public static final String[] PUBLIC_URLS = {
            "/api/user/auth/login",
            "/api/user/auth/signup",
            "/api/admin/auth/login",
            "/.well-known/jwks.json",
            "/api/auth/password-reset/**",
            "/api/auth/refresh",
            "/error"
    };
}
