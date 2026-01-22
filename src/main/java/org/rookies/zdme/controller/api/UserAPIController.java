package org.rookies.zdme.controller.api;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.dto.LoginRequest;
import org.rookies.zdme.dto.LoginResponse;
import org.rookies.zdme.dto.SignupResponse;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.security.JwtUtil;
import org.rookies.zdme.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * 사용자 관련 Controller
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserAPIController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 (대량 할당 및 SQL 인젝션 등 취약점을 포함)
     * @param requestData
     * @return
     */
    @PostMapping("/auth/signup")
    public ResponseEntity<?> vulnerableSignup(@RequestBody Map<String, Object> requestData) {
        try {
            User newUser = userService.vulnerableSignup(requestData);
            return ResponseEntity.ok(SignupResponse.fromEntity(newUser));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "회원가입 처리 중 심각한 오류가 발생했습니다."));
        }
    }

    /**
     * 일반 사용자 jwt 로그인
     * @param authenticationRequest
     * @return
     * @throws Exception
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest) throws Exception {

        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

        userService.checkUserRole(authenticationRequest.getUsername());

        final UserDetails userDetails = userService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    /**
     * 회원정보 조회
     * @param userId
     * @return
     */
    @GetMapping("/info/{user_id}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable("user_id") Long userId) {
        try {
            Map<String, Object> userInfo = userService.getUserInfo(userId);
            return new ResponseEntity<>(userInfo, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 아이디/비밀번호 검증
     */
    private void authenticate(String username, String password) throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
