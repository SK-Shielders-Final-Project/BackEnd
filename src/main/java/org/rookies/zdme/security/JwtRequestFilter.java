package org.rookies.zdme.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rookies.zdme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher; // Import AntPathMatcher

import java.io.IOException;

/**
 *  JWT 필터
 *  HTTP 요청 헤더에서 JWT 토큰을 추출하고 유효성을 검증하여 인증 정보를 설정
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // Use AntPathMatcher for path pattern matching
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    // Define public URLs that do not require JWT authentication, mirroring SecurityConfig
    // Now references SecurityConstants for PUBLIC_URLS
    private static final String[] PUBLIC_URLS = SecurityConstants.PUBLIC_URLS;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Check if the current request URI is a public URL
        String requestUri = request.getRequestURI();
        for (String publicUrl : PUBLIC_URLS) {
            if (antPathMatcher.match(publicUrl, requestUri)) {
                // If it's a public URL, skip JWT processing and proceed with the filter chain
                chain.doFilter(request, response);
                return;
            }
        }

        // 헤더에서 Authorization 항목 get
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null) {
            if (requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7); // "Bearer " 이후의 문자열 추출
                try {
                    // 토큰으로부터 사용자 이름을 추출
                    username = jwtUtil.getUsernameFromToken(jwtToken);
                } catch (IllegalArgumentException e) {
                    System.out.println("JWT 토큰을 가져올 수 없습니다.");
                } catch (ExpiredJwtException e) {
                    System.out.println("JWT 토큰이 만료되었습니다.");
                }
            } else {
                logger.warn("Authorization 헤더가 'Bearer '로 시작하지 않습니다.");
            }
        }

        // 토큰을 성공적으로 가져왔고, 현재 SecurityContext에 인증 정보가 없는 경우 검증 진행
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // DB에서 사용자 정보 로드
            UserDetails userDetails = this.userService.loadUserByUsername(username);

            // 토큰이 유효한 경우 Spring Security의 인증 컨텍스트 수동 설정
            if (jwtUtil.validateToken(jwtToken, userDetails)) {

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 객체 저장(인증 완료)
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        // 다음 필터 체인으로 요청과 응답 전달
        chain.doFilter(request, response);
    }
}